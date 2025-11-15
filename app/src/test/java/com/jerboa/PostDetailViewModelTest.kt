package com.jerboa

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import arrow.core.Either
import com.jerboa.ai.repository.AiSummarizationRepository
import com.jerboa.api.ApiState
import com.jerboa.model.PostViewModel
import com.jerboa.ui.components.post.CommentAnalysis
import it.vercruysse.lemmyapi.datatypes.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import java.util.*

@ExperimentalCoroutinesApi
class PostDetailViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mockAiRepository: AiSummarizationRepository

    private lateinit var viewModel: PostViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        
        // Create test post data
        val testPost = Post(
            id = PostId(1),
            name = "Test Post Title",
            body = "Test post content for summarization",
            creator_id = PersonId(1),
            community_id = CommunityId(1),
            published = Date().toString(),
            updated = null,
            deleted = false,
            removed = false,
            locked = false,
            nsfw = false,
            stickied = false,
            embed_title = null,
            embed_description = null,
            thumbnail_url = null,
            ap_id = "test-ap-id",
            local = true,
            embed_video_url = null,
            language_id = LanguageId(1),
            featured_community = false,
            featured_local = false
        )
        
        val testPostView = PostView(
            post = testPost,
            creator = PersonSafe(
                id = PersonId(1),
                name = "TestUser",
                display_name = null,
                avatar = null,
                banned = false,
                published = Date().toString(),
                updated = null,
                actor_id = "test-actor",
                bio = null,
                local = true,
                banner = null,
                deleted = false,
                matrix_user_id = null,
                bot_account = false,
                ban_expires = null,
                instance_id = InstanceId(1)
            ),
            community = CommunitySafe(
                id = CommunityId(1),
                name = "TestCommunity",
                title = "Test Community",
                description = null,
                removed = false,
                published = Date().toString(),
                updated = null,
                deleted = false,
                nsfw = false,
                actor_id = "test-community-actor",
                local = true,
                icon = null,
                banner = null,
                followers_url = "test-followers",
                inbox_url = "test-inbox",
                shared_inbox_url = null,
                hidden = false,
                posting_restricted_to_mods = false,
                instance_id = InstanceId(1)
            ),
            creator_banned_from_community = false,
            counts = PostAggregates(
                id = PostAggregatesId(1),
                post_id = PostId(1),
                comments = 0,
                score = 1,
                upvotes = 1,
                downvotes = 0,
                published = Date().toString(),
                newest_comment_time_necro = Date().toString(),
                newest_comment_time = Date().toString(),
                featured_community = false,
                featured_local = false,
                hot_rank = 0,
                hot_rank_active = 0
            ),
            subscribed = SubscribedType.NotSubscribed,
            saved = false,
            read = false,
            creator_blocked = false,
            my_vote = null,
            unread_comments = 0
        )
        
        // Initialize ViewModel with test data
        viewModel = PostViewModel(Either.Left(PostId(1)))
        
        // Use reflection to set the mock repository
        val repositoryField = PostViewModel::class.java.getDeclaredField("aiRepository")
        repositoryField.isAccessible = true
        repositoryField.set(viewModel, mockAiRepository)
        
        // Set up test post data
        viewModel.postRes = ApiState.Success(GetPostResponse(post_view = testPostView))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onGenerateSummaryClicked - when generation starts - sets isLoading to true`() = runTest {
        // Given
        whenever(mockAiRepository.generatePostSummary(any(), any())).thenReturn(
            Result.success("Test summary")
        )

        // When
        viewModel.onGenerateSummaryClicked()

        // Then
        assertTrue("Loading state should be true when generation starts", viewModel.isLoadingSummary)
        assertFalse("Summary should not be visible during loading", viewModel.isSummaryVisible)
        assertNull("Error should be cleared when starting", viewModel.summaryError)
    }

    @Test
    fun `onGenerateSummaryClicked - when repository returns success - updates uiState correctly`() = runTest {
        // Given
        val expectedSummary = "This is a test summary of the post content."
        whenever(mockAiRepository.generatePostSummary(any(), any())).thenReturn(
            Result.success(expectedSummary)
        )

        // When
        viewModel.onGenerateSummaryClicked()
        advanceUntilIdle() // Wait for coroutine to complete

        // Then
        assertFalse("Loading should be false after completion", viewModel.isLoadingSummary)
        assertTrue("Summary should be visible after success", viewModel.isSummaryVisible)
        assertEquals("Summary text should match expected", expectedSummary, viewModel.summaryText)
        assertNull("Error should be null on success", viewModel.summaryError)
    }

    @Test
    fun `onGenerateSummaryClicked - when repository returns error - sets errorState correctly`() = runTest {
        // Given
        val errorMessage = "Network error occurred"
        whenever(mockAiRepository.generatePostSummary(any(), any())).thenReturn(
            Result.failure(Exception(errorMessage))
        )

        // When
        viewModel.onGenerateSummaryClicked()
        advanceUntilIdle()

        // Then
        assertFalse("Loading should be false after error", viewModel.isLoadingSummary)
        assertFalse("Summary should not be visible on error", viewModel.isSummaryVisible)
        assertEquals("Error message should match", errorMessage, viewModel.summaryError)
    }

    @Test
    fun `onAnalyzeCommentsClicked - when analysis starts - sets isLoading to true`() = runTest {
        // Given
        val mockComments = createMockComments()
        viewModel.commentsRes = ApiState.Success(GetCommentsResponse(comments = mockComments))
        
        whenever(mockAiRepository.analyzeComments(any())).thenReturn(
            Result.success(CommentAnalysis(listOf(), listOf(), ""))
        )

        // When
        viewModel.onAnalyzeCommentsClicked()

        // Then
        assertTrue("Analysis loading state should be true", viewModel.isLoadingAnalysis)
        assertFalse("Analysis should not be visible during loading", viewModel.isAnalysisVisible)
        assertNull("Analysis error should be cleared", viewModel.analysisError)
    }

    @Test
    fun `onAnalyzeCommentsClicked - when repository returns success - updates analysis state`() = runTest {
        // Given
        val mockComments = createMockComments()
        val expectedAnalysis = CommentAnalysis(
            mainThemes = listOf("Theme 1", "Theme 2"),
            keyOpinions = listOf("Opinion 1", "Opinion 2"),
            overallSentiment = "Positive"
        )
        
        viewModel.commentsRes = ApiState.Success(GetCommentsResponse(comments = mockComments))
        whenever(mockAiRepository.analyzeComments(any())).thenReturn(
            Result.success(expectedAnalysis)
        )

        // When
        viewModel.onAnalyzeCommentsClicked()
        advanceUntilIdle()

        // Then
        assertFalse("Analysis loading should be false", viewModel.isLoadingAnalysis)
        assertTrue("Analysis should be visible", viewModel.isAnalysisVisible)
        assertEquals("Analysis should match expected", expectedAnalysis, viewModel.commentAnalysis)
        assertNull("Analysis error should be null", viewModel.analysisError)
    }

    @Test
    fun `onAnalyzeCommentsClicked - when repository returns error - sets error state`() = runTest {
        // Given
        val mockComments = createMockComments()
        val errorMessage = "Analysis failed"
        
        viewModel.commentsRes = ApiState.Success(GetCommentsResponse(comments = mockComments))
        whenever(mockAiRepository.analyzeComments(any())).thenReturn(
            Result.failure(Exception(errorMessage))
        )

        // When
        viewModel.onAnalyzeCommentsClicked()
        advanceUntilIdle()

        // Then
        assertFalse("Analysis loading should be false", viewModel.isLoadingAnalysis)
        assertFalse("Analysis should not be visible", viewModel.isAnalysisVisible)
        assertEquals("Error message should match", errorMessage, viewModel.analysisError)
    }

    @Test
    fun `onGenerateSummaryClicked - when post not loaded - sets appropriate error`() = runTest {
        // Given
        viewModel.postRes = ApiState.Loading

        // When
        viewModel.onGenerateSummaryClicked()
        advanceUntilIdle()

        // Then
        assertFalse("Loading should be false", viewModel.isLoadingSummary)
        assertEquals("Should show post loading error", 
            "Unable to load post content for summarization", 
            viewModel.summaryError)
    }

    @Test
    fun `onAnalyzeCommentsClicked - when no comments available - sets appropriate error`() = runTest {
        // Given
        viewModel.commentsRes = ApiState.Success(GetCommentsResponse(comments = emptyList()))

        // When
        viewModel.onAnalyzeCommentsClicked()
        advanceUntilIdle()

        // Then
        assertFalse("Analysis loading should be false", viewModel.isLoadingAnalysis)
        assertEquals("Should show no comments error", 
            "No comments available to analyze", 
            viewModel.analysisError)
    }

    private fun createMockComments(): List<CommentView> {
        val comment = Comment(
            id = CommentId(1),
            creator_id = PersonId(1),
            post_id = PostId(1),
            content = "Test comment content",
            removed = false,
            published = Date().toString(),
            updated = null,
            deleted = false,
            ap_id = "test-comment-ap-id",
            local = true,
            path = "0.1",
            distinguished = false,
            language_id = LanguageId(1)
        )
        
        return listOf(
            CommentView(
                comment = comment,
                creator = PersonSafe(
                    id = PersonId(1),
                    name = "TestCommenter",
                    display_name = null,
                    avatar = null,
                    banned = false,
                    published = Date().toString(),
                    updated = null,
                    actor_id = "test-commenter-actor",
                    bio = null,
                    local = true,
                    banner = null,
                    deleted = false,
                    matrix_user_id = null,
                    bot_account = false,
                    ban_expires = null,
                    instance_id = InstanceId(1)
                ),
                post = Post(
                    id = PostId(1),
                    name = "Test Post",
                    body = null,
                    creator_id = PersonId(1),
                    community_id = CommunityId(1),
                    published = Date().toString(),
                    updated = null,
                    deleted = false,
                    removed = false,
                    locked = false,
                    nsfw = false,
                    stickied = false,
                    embed_title = null,
                    embed_description = null,
                    thumbnail_url = null,
                    ap_id = "test-post-ap-id",
                    local = true,
                    embed_video_url = null,
                    language_id = LanguageId(1),
                    featured_community = false,
                    featured_local = false
                ),
                community = CommunitySafe(
                    id = CommunityId(1),
                    name = "TestCommunity",
                    title = "Test Community",
                    description = null,
                    removed = false,
                    published = Date().toString(),
                    updated = null,
                    deleted = false,
                    nsfw = false,
                    actor_id = "test-community-actor",
                    local = true,
                    icon = null,
                    banner = null,
                    followers_url = "test-followers",
                    inbox_url = "test-inbox",
                    shared_inbox_url = null,
                    hidden = false,
                    posting_restricted_to_mods = false,
                    instance_id = InstanceId(1)
                ),
                counts = CommentAggregates(
                    id = CommentAggregatesId(1),
                    comment_id = CommentId(1),
                    score = 1,
                    upvotes = 1,
                    downvotes = 0,
                    published = Date().toString(),
                    child_count = 0
                ),
                creator_banned_from_community = false,
                subscribed = SubscribedType.NotSubscribed,
                saved = false,
                creator_blocked = false,
                my_vote = null
            )
        )
    }
}