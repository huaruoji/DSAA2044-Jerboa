package com.jerboa.recommendation.model

import com.google.gson.annotations.SerializedName

data class RecommendRequest(
    @SerializedName("query") val query: String,
    @SerializedName("top_k") val topK: Int,
)

data class RecommendResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("recommendations") val recommendations: List<RecommendationItem> = emptyList(),
)

data class RecommendationItem(
    @SerializedName("title") val title: String,
    @SerializedName("text") val text: String,
    @SerializedName("url") val url: String,
    @SerializedName("subreddit") val subreddit: String,
    @SerializedName("score") val score: Int,
    @SerializedName("similarity_score") val similarityScore: Double,
    @SerializedName("created_utc") val createdUtc: Long,
)

