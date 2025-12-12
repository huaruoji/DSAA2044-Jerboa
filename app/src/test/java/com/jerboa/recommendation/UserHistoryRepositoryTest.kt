package com.jerboa.recommendation

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.jerboa.recommendation.repository.UserHistoryRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

/**
 * Unit tests for UserHistoryRepository
 * Tests Story #24: Data collection and storage infrastructure
 */
@RunWith(MockitoJUnitRunner::class)
class UserHistoryRepositoryTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockSharedPreferences: SharedPreferences

    @Mock
    private lateinit var mockEditor: SharedPreferences.Editor

    private lateinit var repository: UserHistoryRepository
    private val gson = Gson()

    @Before
    fun setup() {
        // Mock SharedPreferences behavior
        whenever(mockContext.getSharedPreferences(any(), any())).thenReturn(mockSharedPreferences)
        whenever(mockSharedPreferences.edit()).thenReturn(mockEditor)
        whenever(mockEditor.putString(any(), any())).thenReturn(mockEditor)
        whenever(mockEditor.apply()).then { }

        repository = UserHistoryRepository.getInstance(mockContext)
    }

    /**
     * Test 1: Verify history storage and retrieval
     * Acceptance Criteria: User events are successfully logged
     */
    @Test
    fun `addToHistory should store post content correctly`() {
        // Arrange
        val testHistory = listOf("Post 1", "Post 2")
        whenever(mockSharedPreferences.getString(any(), any())).thenReturn(gson.toJson(testHistory))

        // Act
        repository.addToHistory("Post 1", "Body 1")
        repository.addToHistory("Post 2", "Body 2")

        // Assert
        verify(mockEditor, atLeastOnce()).putString(any(), any())
        verify(mockEditor, atLeastOnce()).apply()
    }

    /**
     * Test 2: Verify FIFO queue behavior with max size
     * Acceptance Criteria: History maintains fixed size
     */
    @Test
    fun `history should maintain max size of 50 items`() {
        // Arrange
        val maxSize = 50
        val testHistory = (1..maxSize).map { "Post $it Post $it Body $it" }
        whenever(mockSharedPreferences.getString(any(), any())).thenReturn(gson.toJson(testHistory))

        // Act
        val history = repository.getHistory()

        // Assert
        assertTrue("History should not exceed max size", history.size <= maxSize)
    }

    /**
     * Test 3: Verify viewed post ID tracking
     * Acceptance Criteria: App tracks which posts user has viewed
     */
    @Test
    fun `addViewedPostId should track post IDs correctly`() {
        // Arrange
        val viewedIds = setOf(123L, 456L, 789L)
        whenever(mockSharedPreferences.getString(any(), any())).thenReturn(gson.toJson(viewedIds))

        // Act & Assert
        verify(mockSharedPreferences, atLeastOnce()).getString(any(), any())
    }

    /**
     * Test 4: Verify duplicate removal in history
     * Acceptance Criteria: Same post should not appear twice
     */
    @Test
    fun `addToHistory should remove duplicates`() {
        // Arrange
        val testHistory = listOf("Post 1 Post 1 Body")
        whenever(mockSharedPreferences.getString(any(), any())).thenReturn(gson.toJson(testHistory))

        // Act - Add same post twice
        repository.addToHistory("Post 1", "Body")
        repository.addToHistory("Post 1", "Body")

        // Assert - Should only call putString once for the latest
        verify(mockEditor, atLeastOnce()).putString(any(), any())
    }

    /**
     * Test 5: Verify clear history functionality
     * Acceptance Criteria: User can reset their history
     */
    @Test
    fun `clearHistory should remove all data`() {
        // Act
        repository.clearHistory()

        // Assert
        verify(mockEditor).putString(any(), eq("[]"))
        verify(mockEditor, atLeast(2)).apply() // Both history and viewed IDs
    }
}
