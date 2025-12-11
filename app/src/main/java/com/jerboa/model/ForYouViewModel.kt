package com.jerboa.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jerboa.api.API
import com.jerboa.api.ApiState
import com.jerboa.api.toApiState
import com.jerboa.recommendation.analytics.FirebaseAnalyticsHelper
import com.jerboa.recommendation.api.RecommendationClient
import com.jerboa.recommendation.model.CandidatePost
import com.jerboa.recommendation.model.ScoreRequest
import com.jerboa.recommendation.repository.UserHistoryRepository
import it.vercruysse.lemmyapi.datatypes.GetPosts
import it.vercruysse.lemmyapi.datatypes.GetPostsResponse
import it.vercruysse.lemmyapi.datatypes.PostView
import it.vercruysse.lemmyapi.dto.ListingType
import it.vercruysse.lemmyapi.dto.SortType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ForYouUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val posts: List<PostView> = emptyList(),
    val algorithm: String? = null,
    val isUsingHistory: Boolean = false,
)

/**
 * ViewModel for "For You" personalized feed.
 * 
 * Architecture:
 * 1. Fetch candidate posts from Lemmy's Global/Local feed
 * 2. Send candidates + user history to recommendation backend
 * 3. Backend scores each candidate based on similarity to user's history
 * 4. Display posts sorted by similarity score
 */
class ForYouViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ForYouUiState())
    val uiState: StateFlow<ForYouUiState> = _uiState.asStateFlow()
    
    private val historyRepository = UserHistoryRepository.getInstance(application)
    private val analyticsHelper = FirebaseAnalyticsHelper.getInstance(application)
    
    private var recommendationStartTime = 0L

    /**
     * Load personalized recommendations.
     * 
     * Workflow:
     * 1. Fetch posts from Lemmy (Global feed by default)
     * 2. Extract candidates (id, title, body)
     * 3. Get user history
     * 4. Send to backend for scoring
     * 5. Reorder posts by score
     */
    fun loadRecommendations(
        topK: Int = DEFAULT_TOP_K,
        listingType: ListingType = ListingType.All,
        sortType: SortType = SortType.Active
    ) {
        viewModelScope.launch {
            recommendationStartTime = System.currentTimeMillis()
            _uiState.update { it.copy(isLoading = true, error = null) }

            android.util.Log.d("ForYou", "=== Loading Personalized Feed ===")
            
            // Log For You tab view to Firebase
            analyticsHelper.logForYouTabView()
            
            // Step 1: Fetch candidate posts from Lemmy
            val lemmyResult = withContext(Dispatchers.IO) {
                runCatching {
                    API.getInstance().getPosts(
                        GetPosts(
                            type_ = listingType,
                            sort = sortType,
                            page = 1,
                            limit = CANDIDATE_FETCH_LIMIT.toLong()
                        )
                    ).toApiState()
                }
            }

            lemmyResult.fold(
                onSuccess = { apiState ->
                    when (apiState) {
                        is ApiState.Success -> {
                            val lemmyPosts = apiState.data.posts
                            android.util.Log.d("ForYou", "Fetched ${lemmyPosts.size} candidate posts from Lemmy")
                            
                            // Step 2: Filter out already-viewed posts
                            val viewedPostIds = historyRepository.getViewedPostIds()
                            val unseenPosts = lemmyPosts.filter { postView ->
                                postView.post.id !in viewedPostIds
                            }
                            android.util.Log.d("ForYou", "Filtered to ${unseenPosts.size} unseen posts (excluded ${lemmyPosts.size - unseenPosts.size} viewed)")
                            
                            if (unseenPosts.isEmpty()) {
                                android.util.Log.d("ForYou", "All posts have been viewed - showing empty state")
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        posts = emptyList(),
                                        algorithm = "none",
                                        isUsingHistory = false,
                                        error = null
                                    )
                                }
                                return@fold
                            }
                            
                            // Step 3: Get user history
                            val historyContents = historyRepository.getHistoryForRecommendation()
                            android.util.Log.d("ForYou", "User history count: ${historyContents.size}")
                            
                            val isUsingHistory = historyContents.isNotEmpty()
                            
                            // If no history, show unseen posts in original order
                            if (!isUsingHistory) {
                                android.util.Log.d("ForYou", "No history - showing unseen posts in default order")
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        posts = unseenPosts,
                                        algorithm = "none",
                                        isUsingHistory = false,
                                        error = null
                                    )
                                }
                                return@fold
                            }
                            
                            // Step 4: Prepare candidates for scoring (only unseen posts)
                            val candidates = unseenPosts.map { postView ->
                                CandidatePost(
                                    id = postView.post.id.toString(),
                                    title = postView.post.name,
                                    body = postView.post.body ?: ""
                                )
                            }
                            
                            android.util.Log.d("ForYou", "Sending ${candidates.size} unseen candidates to backend...")
                            
                            // Step 4: Score candidates
                            val scoreResult = withContext(Dispatchers.IO) {
                                runCatching {
                                    RecommendationClient.api.scoreCandidates(
                                        request = ScoreRequest(
                                            historyContents = historyContents,
                                            candidates = candidates,
                                            topK = topK
                                        )
                                    )
                                }
                            }
                            
                            scoreResult.fold(
                                onSuccess = { response ->
                                    val responseTime = System.currentTimeMillis() - recommendationStartTime
                                    
                                    if (response.isSuccessful) {
                                        val body = response.body()
                                        if (body != null && body.success) {
                                            android.util.Log.d("ForYou", "Received ${body.scoredCandidates.size} scored results")
                                            
                                            // Log recommendation request to Firebase
                                            analyticsHelper.logRecommendationRequest(
                                                historySize = historyContents.size,
                                                candidateCount = candidates.size,
                                                responseTime = responseTime,
                                                success = true
                                            )
                                            
                                            // Step 5: Reorder unseen posts by similarity score
                                            val scoreMap = body.scoredCandidates.associate { 
                                                it.id to it.similarityScore 
                                            }
                                            
                                            val rankedPosts = unseenPosts.sortedByDescending { postView ->
                                                scoreMap[postView.post.id.toString()] ?: 0.0
                                            }
                                            
                            // Log top recommendations
                            rankedPosts.take(5).forEachIndexed { i, post ->
                                val score = scoreMap[post.post.id.toString()] ?: 0.0
                                android.util.Log.d("ForYou", "  [$i] Score: ${"%.3f".format(score)} - ${post.post.name.take(50)}...")
                            }                                            _uiState.update {
                                                it.copy(
                                                    isLoading = false,
                                                    posts = rankedPosts,
                                                    algorithm = body.algorithm,
                                                    isUsingHistory = isUsingHistory,
                                                    error = null
                                                )
                                            }
                                        } else {
                                            val errorMsg = body?.error ?: "Unknown error from backend"
                                            android.util.Log.e("ForYou", "Backend error: $errorMsg")
                                            
                                            // Log failed request
                                            analyticsHelper.logRecommendationRequest(
                                                historySize = historyContents.size,
                                                candidateCount = candidates.size,
                                                responseTime = responseTime,
                                                success = false
                                            )
                                            
                                            _uiState.update {
                                                it.copy(
                                                    isLoading = false,
                                                    error = "Scoring error: $errorMsg"
                                                )
                                            }
                                        }
                                    } else {
                                        val errorBody = try {
                                            response.errorBody()?.string()
                                        } catch (e: Exception) {
                                            null
                                        }
                                        android.util.Log.e("ForYou", "HTTP ${response.code()}: ${response.message()}")
                                        _uiState.update {
                                            it.copy(
                                                isLoading = false,
                                                error = "Backend error ${response.code()}: ${response.message()}\n${errorBody ?: ""}"
                                            )
                                        }
                                    }
                                },
                                onFailure = { throwable ->
                                    android.util.Log.e("ForYou", "Network error: ${throwable.message}", throwable)
                                    _uiState.update {
                                        it.copy(
                                            isLoading = false,
                                            error = "Cannot reach recommendation backend: ${throwable.message}"
                                        )
                                    }
                                }
                            )
                        }
                        is ApiState.Failure -> {
                            android.util.Log.e("ForYou", "Lemmy API error: ${apiState.msg}")
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = "Failed to load posts from Lemmy: ${apiState.msg}"
                                )
                            }
                        }
                        else -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = "Unexpected API state"
                                )
                            }
                        }
                    }
                },
                onFailure = { throwable ->
                    android.util.Log.e("ForYou", "Lemmy fetch error: ${throwable.message}", throwable)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Cannot connect to Lemmy: ${throwable.message}"
                        )
                    }
                }
            )
        }
    }
    
    /**
     * Record that user viewed a post.
     * Automatically adds to reading history for future recommendations.
     */
    fun onPostViewed(postId: Long, postTitle: String, postText: String = "", source: String = "for_you") {
        // Record post ID to prevent future recommendations
        historyRepository.addViewedPostId(postId)
        // Add content to history for similarity scoring
        historyRepository.addToHistory(postTitle, postText)
        
        // Log to Firebase Analytics
        analyticsHelper.logPostView(
            postId = postId,
            postTitle = postTitle,
            communityName = "", // Can be extracted from PostView if needed
            contentLength = postText.length,
            source = source
        )
        
        android.util.Log.d("ForYou", "✓ Recorded view (ID: $postId): ${postTitle.take(50)}...")
        android.util.Log.d("ForYou", "  Total history: ${historyRepository.getCount()}, Viewed IDs: ${historyRepository.getViewedPostIds().size}")
    }
    
    /**
     * Get current history count (for debugging/UI).
     */
    fun getHistoryCount(): Int {
        return historyRepository.getCount()
    }
    
    /**
     * Clear reading history (for privacy/testing).
     */
    fun clearHistory() {
        historyRepository.clearHistory()
        // Reload with default order
        loadRecommendations()
    }

    private companion object {
        const val DEFAULT_TOP_K = 20
        const val CANDIDATE_FETCH_LIMIT = 50  // Fetch more candidates to rank
    }
}

