package com.jerboa.ai.api

import com.jerboa.BuildConfig
import com.jerboa.ai.model.Message
import com.jerboa.ai.model.SiliconFlowError
import com.jerboa.ai.model.SiliconFlowRequest
import com.jerboa.ai.model.SiliconFlowResponse
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
    
    private fun buildSummaryPrompt(title: String, body: String): String {
        return """
            Please provide a concise summary of the following post in 2-3 sentences. 
            Focus on the main points and key information.
            
            Title: $title
            
            Content: $body
            
            Summary:
        """.trimIndent()
    }
    
    fun close() {
        httpClient.close()
    }
}