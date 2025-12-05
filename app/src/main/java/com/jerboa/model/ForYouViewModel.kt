package com.jerboa.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jerboa.recommendation.api.RecommendationClient
import com.jerboa.recommendation.model.RecommendRequest
import com.jerboa.recommendation.model.RecommendationItem
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
    val posts: List<RecommendationItem> = emptyList(),
)

class ForYouViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ForYouUiState())
    val uiState: StateFlow<ForYouUiState> = _uiState.asStateFlow()

    fun loadRecommendations(
        query: String = DEFAULT_QUERY,
        topK: Int = DEFAULT_TOP_K,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = withContext(Dispatchers.IO) {
                runCatching {
                    RecommendationClient.api.getRecommendations(
                        RecommendRequest(
                            query = query,
                            topK = topK,
                        ),
                    )
                }
            }

            result.fold(
                onSuccess = { response ->
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null && body.success) {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    posts = body.recommendations,
                                    error = null,
                                )
                            }
                        } else {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = "Unable to load recommendations",
                                )
                            }
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Error ${response.code()}: ${response.message()}",
                            )
                        }
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.localizedMessage ?: "Network error",
                        )
                    }
                },
            )
        }
    }

    private companion object {
        const val DEFAULT_QUERY = "programming rust android"
        const val DEFAULT_TOP_K = 10
    }
}

