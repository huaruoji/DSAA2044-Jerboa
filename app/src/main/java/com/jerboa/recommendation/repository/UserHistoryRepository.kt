package com.jerboa.recommendation.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * User Reading History Manager
 * ============================
 * Manages local storage of user's reading history for personalized recommendations.
 * 
 * Features:
 * - Stores recent post titles/content that user has viewed
 * - FIFO queue with configurable max size
 * - Persists across app restarts using SharedPreferences
 * - Thread-safe operations
 * 
 * @author DSAA2044 Team
 * @date December 2025
 */
class UserHistoryRepository private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    
    private val gson = Gson()
    
    /**
     * Add a post to reading history.
     * Automatically maintains FIFO queue of max size.
     */
    fun addToHistory(postTitle: String, postText: String = "") {
        if (postTitle.isBlank()) return
        
        val history = getHistory().toMutableList()
        
        // Create combined content (title is more important, so we weight it)
        val content = if (postText.isNotBlank()) {
            "$postTitle $postTitle $postText" // Duplicate title to give it more weight
        } else {
            postTitle
        }
        
        // Remove if already exists (avoid duplicates)
        history.removeAll { it == content }
        
        // Add to front
        history.add(0, content)
        
        // Keep only last MAX_HISTORY_SIZE items
        val trimmedHistory = history.take(MAX_HISTORY_SIZE)
        
        // Save
        saveHistory(trimmedHistory)
    }
    
    /**
     * Get current reading history as list of strings.
     * Returns most recent first.
     */
    fun getHistory(): List<String> {
        val json = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson<List<String>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Get history contents for recommendation API.
     * Returns most recent items up to the specified count.
     */
    fun getHistoryForRecommendation(maxItems: Int = MAX_ITEMS_FOR_RECOMMENDATION): List<String> {
        return getHistory().take(maxItems)
    }
    
    /**
     * Check if history is empty.
     */
    fun isEmpty(): Boolean {
        return getHistory().isEmpty()
    }
    
    /**
     * Get number of items in history.
     */
    fun getCount(): Int {
        return getHistory().size
    }
    
    /**
     * Clear all history (both content and viewed IDs).
     */
    fun clearHistory() {
        prefs.edit()
            .remove(KEY_HISTORY)
            .remove(KEY_VIEWED_POST_IDS)
            .apply()
    }
    
    /**
     * Get default cold-start keywords when history is empty.
     * These are used for first-time users.
     */
    fun getColdStartKeywords(): List<String> {
        return DEFAULT_COLD_START_KEYWORDS
    }
    
    /**
     * Add a post ID to the viewed list.
     * Used to exclude already-viewed posts from recommendations.
     */
    fun addViewedPostId(postId: Long) {
        val viewedIds = getViewedPostIds().toMutableSet()
        viewedIds.add(postId)
        
        // Keep only the most recent MAX_VIEWED_IDS (convert to list for takeLast)
        val trimmedIds = if (viewedIds.size > MAX_VIEWED_IDS) {
            viewedIds.toList().takeLast(MAX_VIEWED_IDS).toSet()
        } else {
            viewedIds
        }
        
        val json = gson.toJson(trimmedIds)
        prefs.edit().putString(KEY_VIEWED_POST_IDS, json).apply()
    }
    
    /**
     * Get list of viewed post IDs.
     * Returns IDs that should be excluded from recommendations.
     */
    fun getViewedPostIds(): Set<Long> {
        val json = prefs.getString(KEY_VIEWED_POST_IDS, null) ?: return emptySet()
        
        return try {
            val type = object : TypeToken<Set<Long>>() {}.type
            gson.fromJson<Set<Long>>(json, type) ?: emptySet()
        } catch (e: Exception) {
            emptySet()
        }
    }
    
    /**
     * Clear viewed post IDs.
     */
    fun clearViewedPostIds() {
        prefs.edit().remove(KEY_VIEWED_POST_IDS).apply()
    }
    
    private fun saveHistory(history: List<String>) {
        val json = gson.toJson(history)
        prefs.edit().putString(KEY_HISTORY, json).apply()
    }
    
    companion object {
        private const val PREFS_NAME = "user_reading_history"
        private const val KEY_HISTORY = "history_contents"
        private const val KEY_VIEWED_POST_IDS = "viewed_post_ids"
        
        // Configuration
        private const val MAX_HISTORY_SIZE = 50  // Store up to 50 recent posts
        private const val MAX_ITEMS_FOR_RECOMMENDATION = 10  // Use 10 most recent for recommendations
        private const val MAX_VIEWED_IDS = 200  // Track up to 200 viewed post IDs
        
        // Cold start keywords for new users
        private val DEFAULT_COLD_START_KEYWORDS = listOf(
            "technology news updates",
            "science discoveries research",
            "interesting discussions community",
            "current events world news",
            "helpful advice tips"
        )
        
        @Volatile
        private var INSTANCE: UserHistoryRepository? = null
        
        fun getInstance(context: Context): UserHistoryRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserHistoryRepository(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}
