package com.jerboa.recommendation.api

import com.jerboa.recommendation.model.RecommendResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface RecommendationApi {
    @GET("recommend")
    suspend fun getRecommendations(
        @Query("q") query: String,
        @Query("top_k") topK: Int = 10,
        @Query("min_score") minScore: Double = 0.0,
    ): Response<RecommendResponse>
}

