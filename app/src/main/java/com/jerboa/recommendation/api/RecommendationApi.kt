package com.jerboa.recommendation.api

import com.jerboa.recommendation.model.RecommendRequest
import com.jerboa.recommendation.model.RecommendResponse
import com.jerboa.recommendation.model.ScoreRequest
import com.jerboa.recommendation.model.ScoreResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Recommendation API interface.
 */
interface RecommendationApi {
    /**
     * Score candidate posts based on user history.
     * Used for ranking Lemmy posts from Global/Local feeds.
     */
    @POST("score")
    suspend fun scoreCandidates(
        @Body request: ScoreRequest,
    ): Response<ScoreResponse>
    
    /**
     * [LEGACY] Get recommendations from training dataset.
     */
    @POST("recommend")
    suspend fun getRecommendations(
        @Body request: RecommendRequest,
    ): Response<RecommendResponse>
}

