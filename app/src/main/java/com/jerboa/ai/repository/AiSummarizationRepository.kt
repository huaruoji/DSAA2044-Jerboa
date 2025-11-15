package com.jerboa.ai.repository

import com.jerboa.ai.api.SiliconFlowApiClient
import com.jerboa.ai.model.SelectedComment
import com.jerboa.ai.util.CommentSelectionStrategy
import com.jerboa.ui.components.post.CommentAnalysis
import it.vercruysse.lemmyapi.datatypes.CommentView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AiSummarizationRepository(
    private val siliconFlowClient: SiliconFlowApiClient = SiliconFlowApiClient(),
) {
    
    suspend fun generatePostSummary(postTitle: String, postBody: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Validate input
                if (postTitle.isBlank() && postBody.isBlank()) {
                    return@withContext Result.failure(Exception("Cannot generate summary: Post title and body are both empty"))
                }
                
                // Clean and prepare content
                val cleanTitle = postTitle.trim()
                val cleanBody = postBody.trim()
                
                // Use title as fallback if body is empty, or vice versa
                val effectiveTitle = cleanTitle.ifEmpty { "Post Summary" }
                val effectiveBody = cleanBody.ifEmpty { cleanTitle }
                
                siliconFlowClient.generateSummary(effectiveTitle, effectiveBody)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun analyzeComments(comments: List<CommentView>): Result<CommentAnalysis> {
        return withContext(Dispatchers.IO) {
            try {
                // Validate input
                if (comments.isEmpty()) {
                    return@withContext Result.failure(Exception("No comments available for analysis"))
                }
                
                // Select relevant comments using our strategy
                val selectedComments = CommentSelectionStrategy.selectRelevantComments(comments, maxComments = 10)
                
                if (selectedComments.isEmpty()) {
                    return@withContext Result.failure(Exception("No suitable comments found for analysis"))
                }
                
                // Call the AI API
                val apiResult = siliconFlowClient.analyzeComments(selectedComments)
                
                apiResult.fold(
                    onSuccess = { analysisResponse ->
                        // Convert API response to UI model
                        val commentAnalysis = CommentAnalysis(
                            mainThemes = analysisResponse.mainThemes,
                            agreements = analysisResponse.agreements,
                            disagreements = analysisResponse.disagreements
                        )
                        Result.success(commentAnalysis)
                    },
                    onFailure = { error ->
                        Result.failure(error)
                    }
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    fun cleanup() {
        siliconFlowClient.close()
    }
}