package com.jerboa.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jerboa.recommendation.api.RecommendationClient
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
                        query = query,
                        topK = topK,
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
                            // Try to get error message from response body
                            val errorBody = try {
                                response.errorBody()?.string()
                            } catch (e: Exception) {
                                null
                            }
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = errorBody ?: "Unable to load recommendations. Response: ${body?.let { "success=${it.success}, count=${it.count}" } ?: "null body"}",
                                )
                            }
                        }
                    } else {
                        // Get error details from response
                        val errorBody = try {
                            response.errorBody()?.string()
                        } catch (e: Exception) {
                            null
                        }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Error ${response.code()}: ${response.message()}\n${errorBody ?: ""}",
                            )
                        }
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "${throwable.javaClass.simpleName}: ${throwable.message ?: throwable.localizedMessage ?: "Network error"}",
                        )
                    }
                },
            )
        }
    }

    private companion object {
        const val DEFAULT_QUERY = "macron"
        const val DEFAULT_TOP_K = 10
    }
}

