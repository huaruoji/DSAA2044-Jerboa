package com.jerboa.recommendation.api

import com.jerboa.recommendation.model.RecommendRequest
import com.jerboa.recommendation.model.RecommendResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface RecommendationApi {
    @POST("recommend")
    suspend fun getRecommendations(
        @Body request: RecommendRequest,
    ): Response<RecommendResponse>
}

