package com.jerboa.ai.api

import com.jerboa.BuildConfig
import com.jerboa.ai.model.CommentAnalysisResponse
import com.jerboa.ai.model.Message
import com.jerboa.ai.model.SelectedComment
import com.jerboa.ai.model.SiliconFlowError
import com.jerboa.ai.model.SiliconFlowRequest
import com.jerboa.ai.model.SiliconFlowResponse
import com.jerboa.ai.util.CommentSelectionStrategy
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.util.Properties

class SiliconFlowApiClient {
    
    private val apiKey: String by lazy {
        BuildConfig.SILICONFLOW_API_KEY.takeIf { it.isNotEmpty() }
            ?: throw IllegalStateException("SiliconFlow API key not configured. Please add it to local.properties.")
    }
    
    private val httpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
        install(Logging) {
            level = LogLevel.INFO
        }
    }
    

    
    suspend fun generateSummary(postTitle: String, postBody: String): Result<String> {
        return try {
            val prompt = buildSummaryPrompt(postTitle, postBody)
            val request = SiliconFlowRequest(
                model = "deepseek-ai/DeepSeek-V3.2-Exp",
                messages = listOf(
                    Message(
                        role = "user",
                        content = prompt
                    )
                )
            )
            
            val response: HttpResponse = httpClient.post("https://api.siliconflow.cn/v1/chat/completions") {
                header(HttpHeaders.Authorization, "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            
            if (response.status.isSuccess()) {
                val siliconFlowResponse: SiliconFlowResponse = response.body()
                val summary = siliconFlowResponse.choices.firstOrNull()?.message?.content
                    ?: return Result.failure(Exception("No summary generated"))
                Result.success(summary.trim())
            } else {
                val errorBody = try {
                    response.body<SiliconFlowError>()
                } catch (e: Exception) {
                    null
                }
                val errorMessage = errorBody?.error?.message ?: "API request failed with status: ${response.status.value}"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun analyzeComments(comments: List<SelectedComment>): Result<CommentAnalysisResponse> {
        return try {
            if (comments.isEmpty()) {
                return Result.failure(Exception("No comments provided for analysis"))
            }
            
            val formattedComments = CommentSelectionStrategy.formatCommentsForAnalysis(comments)
            val prompt = buildCommentAnalysisPrompt(formattedComments)
            
            val request = SiliconFlowRequest(
                model = "deepseek-ai/DeepSeek-V3.2-Exp",
                messages = listOf(
                    Message(
                        role = "user",
                        content = prompt
                    )
                )
            )
            
            val response: HttpResponse = httpClient.post("https://api.siliconflow.cn/v1/chat/completions") {
                header(HttpHeaders.Authorization, "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            
            if (response.status.isSuccess()) {
                val siliconFlowResponse: SiliconFlowResponse = response.body()
                val analysisText = siliconFlowResponse.choices.firstOrNull()?.message?.content
                    ?: return Result.failure(Exception("No analysis generated"))
                
                val parsedAnalysis = parseCommentAnalysis(analysisText.trim())
                Result.success(parsedAnalysis)
            } else {
                val errorBody = try {
                    response.body<SiliconFlowError>()
                } catch (e: Exception) {
                    null
                }
                val errorMessage = errorBody?.error?.message ?: "API request failed with status: ${response.status.value}"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildSummaryPrompt(title: String, body: String): String {
        return """
            Please provide a concise summary of the following post in 2-3 sentences. 
            Focus on the main points and key information.
            
            Title: $title
            
            Content: $body
            
            Summary:
        """.trimIndent()
    }
    
    private fun buildCommentAnalysisPrompt(formattedComments: String): String {
        return """
            Please analyze the following comments from a discussion thread and provide a structured analysis. 
            I need you to identify:
            
            1. Main Themes: The primary topics or subjects being discussed
            2. Agreements: Points where multiple commenters seem to agree or share similar views
            3. Disagreements: Points where commenters have conflicting opinions or debates
            
            Please respond in the following JSON format:
            {
                "main_themes": ["theme1", "theme2", "theme3"],
                "agreements": ["agreement1", "agreement2", "agreement3"],
                "disagreements": ["disagreement1", "disagreement2", "disagreement3"]
            }
            
            Comments to analyze:
            
            $formattedComments
            
            Analysis (JSON format only):
        """.trimIndent()
    }
    
    private fun parseCommentAnalysis(responseText: String): CommentAnalysisResponse {
        return try {
            // Try to extract JSON from the response
            val jsonStart = responseText.indexOf("{")
            val jsonEnd = responseText.lastIndexOf("}") + 1
            
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                val jsonText = responseText.substring(jsonStart, jsonEnd)
                kotlinx.serialization.json.Json {
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                }.decodeFromString<CommentAnalysisResponse>(jsonText)
            } else {
                // Fallback: parse manually if JSON extraction fails
                parseAnalysisManually(responseText)
            }
        } catch (e: Exception) {
            // Final fallback: return empty analysis with error message
            CommentAnalysisResponse(
                mainThemes = listOf("Unable to parse analysis due to format error"),
                agreements = listOf("Analysis parsing failed"),
                disagreements = listOf("Please try again")
            )
        }
    }
    
    private fun parseAnalysisManually(text: String): CommentAnalysisResponse {
        val themes = mutableListOf<String>()
        val agreements = mutableListOf<String>()
        val disagreements = mutableListOf<String>()
        
        val lines = text.split("\n")
        var currentSection = ""
        
        for (line in lines) {
            val trimmed = line.trim()
            when {
                trimmed.contains("main themes", ignoreCase = true) || 
                trimmed.contains("themes", ignoreCase = true) -> currentSection = "themes"
                trimmed.contains("agreements", ignoreCase = true) -> currentSection = "agreements"
                trimmed.contains("disagreements", ignoreCase = true) -> currentSection = "disagreements"
                trimmed.startsWith("-") || trimmed.startsWith("•") || trimmed.matches(Regex("\\d+\\..*")) -> {
                    val cleanItem = trimmed.removePrefix("-").removePrefix("•").replaceFirst(Regex("^\\d+\\."), "").trim()
                    if (cleanItem.isNotEmpty()) {
                        when (currentSection) {
                            "themes" -> themes.add(cleanItem)
                            "agreements" -> agreements.add(cleanItem)
                            "disagreements" -> disagreements.add(cleanItem)
                        }
                    }
                }
            }
        }
        
        return CommentAnalysisResponse(
            mainThemes = themes.ifEmpty { listOf("No clear themes identified") },
            agreements = agreements.ifEmpty { listOf("No clear agreements found") },
            disagreements = disagreements.ifEmpty { listOf("No clear disagreements found") }
        )
    }
    
    fun close() {
        httpClient.close()
    }
}