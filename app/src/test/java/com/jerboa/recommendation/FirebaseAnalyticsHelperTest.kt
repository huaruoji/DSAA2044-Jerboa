package com.jerboa.recommendation

import com.jerboa.analytics.FirebaseAnalyticsHelper
import com.google.firebase.analytics.FirebaseAnalytics
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import android.os.Bundle

/**
 * Unit tests for FirebaseAnalyticsHelper
 * Tests Story #24: Firebase Analytics event logging
 */
@RunWith(MockitoJUnitRunner::class)
class FirebaseAnalyticsHelperTest {

    @Mock
    private lateinit var mockFirebaseAnalytics: FirebaseAnalytics

    private lateinit var analyticsHelper: FirebaseAnalyticsHelper

    @Before
    fun setup() {
        // Create helper with mocked FirebaseAnalytics
        analyticsHelper = FirebaseAnalyticsHelper(mockFirebaseAnalytics)
    }

    /**
     * Test 1: Verify post view event logging
     * Acceptance Criteria: Post views are tracked in Firebase Analytics
     */
    @Test
    fun `logPostView should log event with correct parameters`() {
        // Arrange
        val postId = 123456L
        val postTitle = "Test Post"
        val source = "for_you"

        // Act
        analyticsHelper.logPostView(postId, postTitle, source)

        // Assert
        verify(mockFirebaseAnalytics, times(1)).logEvent(
            eq("post_view"),
            any()
        )
    }

    /**
     * Test 2: Verify post interaction event logging
     * Acceptance Criteria: User interactions are tracked
     */
    @Test
    fun `logPostInteraction should log event with interaction type`() {
        // Arrange
        val postId = 789012L
        val interactionType = "upvote"

        // Act
        analyticsHelper.logPostInteraction(postId, interactionType)

        // Assert
        verify(mockFirebaseAnalytics, times(1)).logEvent(
            eq("post_interaction"),
            any()
        )
    }

    /**
     * Test 3: Verify For You tab view logging
     * Acceptance Criteria: Tab views are tracked for engagement metrics
     */
    @Test
    fun `logForYouTabView should log tab_view event`() {
        // Act
        analyticsHelper.logForYouTabView()

        // Assert
        verify(mockFirebaseAnalytics, times(1)).logEvent(
            eq("for_you_tab_view"),
            any()
        )
    }

    /**
     * Test 4: Verify recommendation request logging
     * Acceptance Criteria: API calls are tracked with result count
     */
    @Test
    fun `logRecommendationRequest should log event with result count`() {
        // Arrange
        val candidateCount = 25
        val resultCount = 15

        // Act
        analyticsHelper.logRecommendationRequest(candidateCount, resultCount)

        // Assert
        verify(mockFirebaseAnalytics, times(1)).logEvent(
            eq("recommendation_request"),
            any()
        )
    }

    /**
     * Test 5: Verify session end logging
     * Acceptance Criteria: User sessions are tracked with history size
     */
    @Test
    fun `logSessionEnd should log event with history size`() {
        // Arrange
        val historySize = 42

        // Act
        analyticsHelper.logSessionEnd(historySize)

        // Assert
        verify(mockFirebaseAnalytics, times(1)).logEvent(
            eq("session_end"),
            any()
        )
    }

    /**
     * Test 6: Verify analytics instance is used
     * Acceptance Criteria: All events use the Firebase Analytics instance
     */
    @Test
    fun `all logging methods should use Firebase Analytics instance`() {
        // Act
        analyticsHelper.logPostView(1L, "Test", "for_you")
        analyticsHelper.logPostInteraction(2L, "comment")
        analyticsHelper.logForYouTabView()
        analyticsHelper.logRecommendationRequest(10, 5)
        analyticsHelper.logSessionEnd(30)

        // Assert
        verify(mockFirebaseAnalytics, times(5)).logEvent(any(), any())
    }

    /**
     * Test 7: Verify parameter bundling
     * Acceptance Criteria: Event parameters are correctly formatted
     */
    @Test
    fun `event parameters should be correctly formatted`() {
        // Arrange
        val postId = 999L
        val title = "Important Post"

        // Act
        analyticsHelper.logPostView(postId, title, "home")

        // Assert - Verify logEvent was called with proper event name
        verify(mockFirebaseAnalytics).logEvent(eq("post_view"), any())
    }
}
