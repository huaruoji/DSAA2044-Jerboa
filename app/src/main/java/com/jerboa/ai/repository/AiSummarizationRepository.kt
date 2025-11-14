package com.jerboa.ai.repository

import com.jerboa.ai.api.SiliconFlowApiClient
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
    
    fun cleanup() {
        siliconFlowClient.close()
    }
}