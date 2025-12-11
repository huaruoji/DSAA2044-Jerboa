package com.jerboa.recommendation.model

import com.google.gson.annotations.SerializedName

/**
 * Candidate post to be scored.
 */
data class CandidatePost(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("body") val body: String = "",
)

/**
 * Request body for scoring candidates API.
 */
data class ScoreRequest(
    @SerializedName("history_contents") val historyContents: List<String>,
    @SerializedName("candidates") val candidates: List<CandidatePost>,
    @SerializedName("top_k") val topK: Int? = null,
)

/**
 * Scored candidate response.
 */
data class ScoredCandidate(
    @SerializedName("id") val id: String,
    @SerializedName("similarity_score") val similarityScore: Double,
)

/**
 * Response from scoring candidates API.
 */
data class ScoreResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("algorithm") val algorithm: String? = null,
    @SerializedName("count") val count: Int? = null,
    @SerializedName("scored_candidates") val scoredCandidates: List<ScoredCandidate> = emptyList(),
    @SerializedName("error") val error: String? = null,
    @SerializedName("note") val note: String? = null,
)

/**
 * [LEGACY] Request body for recommendations API.
 * Supports history-based recommendations.
 */
data class RecommendRequest(
    @SerializedName("history_contents") val historyContents: List<String>,
    @SerializedName("top_k") val topK: Int = 10,
    @SerializedName("min_score") val minScore: Double = 0.0,
    @SerializedName("exclude_ids") val excludeIds: List<String> = emptyList(),
)

/**
 * Response from recommendations API.
 */
data class RecommendResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("algorithm") val algorithm: String? = null,
    @SerializedName("count") val count: Int? = null,
    @SerializedName("recommendations") val recommendations: List<RecommendationItem> = emptyList(),
    @SerializedName("error") val error: String? = null,
)

/**
 * Individual recommendation item.
 */
data class RecommendationItem(
    @SerializedName("id") val id: String? = null,
    @SerializedName("title") val title: String,
    @SerializedName("text") val text: String,
    @SerializedName("url") val url: String,
    @SerializedName("subreddit") val subreddit: String,
    @SerializedName("score") val score: Int,
    @SerializedName("similarity_score") val similarityScore: Double,
    @SerializedName("created_utc") val createdUtc: Long,
)

