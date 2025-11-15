package com.jerboa.ai.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommentAnalysisRequest(
    val model: String,
    val messages: List<Message>
)

@Serializable
data class CommentAnalysisResponse(
    @SerialName("main_themes") val mainThemes: List<String>,
    val agreements: List<String>,
    val disagreements: List<String>
)

data class SelectedComment(
    val id: String,
    val content: String,
    val author: String,
    val score: Int,
    val isTopLevel: Boolean
)