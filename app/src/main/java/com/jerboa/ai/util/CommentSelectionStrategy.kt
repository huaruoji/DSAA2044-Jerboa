package com.jerboa.ai.util

import com.jerboa.ai.model.SelectedComment
import it.vercruysse.lemmyapi.datatypes.CommentView

object CommentSelectionStrategy {
    
    /**
     * Selects the most relevant comments for AI analysis based on multiple criteria
     */
    fun selectRelevantComments(
        comments: List<CommentView>,
        maxComments: Int = 10
    ): List<SelectedComment> {
        if (comments.isEmpty()) return emptyList()
        
        // Convert to SelectedComment with additional metadata
        val selectedComments = comments.mapNotNull { commentView ->
            val comment = commentView.comment
            val body = comment.content?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            
            // Skip very short comments (likely not meaningful)
            if (body.length < 20) return@mapNotNull null
            
            SelectedComment(
                id = comment.id.toString(),
                content = body,
                author = commentView.creator.name ?: "Unknown",
                score = commentView.counts.score.toInt(),
                isTopLevel = comment.path.count { it == '.' } <= 1 // Simple depth check
            )
        }
        
        // Selection strategy: Prioritize based on multiple factors
        val prioritizedComments = selectedComments.sortedWith(
            compareBy<SelectedComment> { !it.isTopLevel } // Top-level comments first
                .thenByDescending { it.score } // Higher score better
                .thenByDescending { it.content.length } // Longer content preferred (more substance)
        )
        
        // Take the top comments, ensuring we have a good mix
        val result = mutableListOf<SelectedComment>()
        val topLevelComments = prioritizedComments.filter { it.isTopLevel }
        val replyComments = prioritizedComments.filter { !it.isTopLevel }
        
        // Take up to 70% from top-level comments
        val topLevelLimit = (maxComments * 0.7).toInt()
        result.addAll(topLevelComments.take(topLevelLimit))
        
        // Fill remaining slots with replies if needed
        val remaining = maxComments - result.size
        if (remaining > 0) {
            result.addAll(replyComments.take(remaining))
        }
        
        return result
    }
    
    /**
     * Formats selected comments into a readable text for AI analysis
     */
    fun formatCommentsForAnalysis(comments: List<SelectedComment>): String {
        if (comments.isEmpty()) return "No comments available for analysis."
        
        return comments.mapIndexed { index, comment ->
            val level = if (comment.isTopLevel) "Top-level" else "Reply"
            val scoreText = if (comment.score > 0) "+${comment.score}" else "${comment.score}"
            
            """
            Comment ${index + 1} ($level, Score: $scoreText, Author: ${comment.author}):
            ${comment.content.trim()}
            """.trimIndent()
        }.joinToString("\n\n---\n\n")
    }
}