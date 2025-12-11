package com.jerboa.recommendation.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase

/**
 * Helper class for logging user behavior events to Firebase Analytics.
 * Used for collecting data to improve recommendation quality.
 */
class FirebaseAnalyticsHelper private constructor(context: Context) {
    
    private val firebaseAnalytics: FirebaseAnalytics = Firebase.analytics
    
    companion object {
        @Volatile
        private var INSTANCE: FirebaseAnalyticsHelper? = null
        
        fun getInstance(context: Context): FirebaseAnalyticsHelper {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FirebaseAnalyticsHelper(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
    
    /**
     * Log when user views a post.
     * @param postId The ID of the viewed post
     * @param postTitle The title of the post
     * @param communityName The community the post belongs to
     * @param contentLength The length of post content
     * @param source The source of the view (for_you, local, global, etc.)
     */
    fun logPostView(
        postId: Long,
        postTitle: String,
        communityName: String = "",
        contentLength: Int = 0,
        source: String = "unknown"
    ) {
        firebaseAnalytics.logEvent("post_view") {
            param("post_id", postId)
            param("post_title", postTitle.take(100)) // Limit to 100 chars
            param("community", communityName.take(50))
            param("content_length", contentLength.toLong())
            param("view_source", source)
            param("timestamp", System.currentTimeMillis())
        }
    }
    
    /**
     * Log user interaction with a post (upvote, downvote, save, share, etc.)
     * @param postId The ID of the post
     * @param actionType The type of action (upvote, downvote, save, share, comment)
     * @param source Where the action was performed (for_you, local, global)
     */
    fun logPostInteraction(
        postId: Long,
        actionType: String,
        source: String = "unknown"
    ) {
        firebaseAnalytics.logEvent("post_interaction") {
            param("post_id", postId)
            param("action_type", actionType)
            param("source", source)
            param("timestamp", System.currentTimeMillis())
        }
    }
    
    /**
     * Log when user clicks on For You tab
     */
    fun logForYouTabView() {
        firebaseAnalytics.logEvent("for_you_tab_view") {
            param("timestamp", System.currentTimeMillis())
        }
    }
    
    /**
     * Log recommendation request and response
     * @param historySize Number of items in user history
     * @param candidateCount Number of candidates scored
     * @param responseTime Time taken for API call in milliseconds
     * @param success Whether the request succeeded
     */
    fun logRecommendationRequest(
        historySize: Int,
        candidateCount: Int,
        responseTime: Long,
        success: Boolean
    ) {
        firebaseAnalytics.logEvent("recommendation_request") {
            param("history_size", historySize.toLong())
            param("candidate_count", candidateCount.toLong())
            param("response_time_ms", responseTime)
            param("success", if (success) 1L else 0L)
            param("timestamp", System.currentTimeMillis())
        }
    }
    
    /**
     * Log user session information
     * @param sessionDuration Duration of session in seconds
     * @param postsViewed Number of posts viewed in session
     * @param interactions Number of interactions (upvotes, comments, etc)
     */
    fun logSessionEnd(
        sessionDuration: Long,
        postsViewed: Int,
        interactions: Int
    ) {
        firebaseAnalytics.logEvent("session_end") {
            param("duration_seconds", sessionDuration)
            param("posts_viewed", postsViewed.toLong())
            param("interactions", interactions.toLong())
            param("timestamp", System.currentTimeMillis())
        }
    }
    
    /**
     * Set user properties for segmentation
     * @param userId The anonymous user ID (hashed)
     * @param userLevel User engagement level (casual, regular, power)
     */
    fun setUserProperties(userId: String?, userLevel: String = "casual") {
        userId?.let {
            firebaseAnalytics.setUserId(it.hashCode().toString()) // Use hash for privacy
        }
        firebaseAnalytics.setUserProperty("user_level", userLevel)
    }
    
    /**
     * Enable/disable analytics collection (for privacy settings)
     */
    fun setAnalyticsEnabled(enabled: Boolean) {
        firebaseAnalytics.setAnalyticsCollectionEnabled(enabled)
    }
}
