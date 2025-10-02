package com.example.wellnessbuddy.data

import java.util.Date

/**
 * Data class representing a daily habit
 */
data class Habit(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val icon: String = "", // Emoji or icon identifier
    val targetCount: Int = 1, // How many times per day
    val completedCount: Int = 0, // How many times completed today
    val isCompleted: Boolean = false,
    val createdAt: Date = Date(),
    val lastUpdated: Date = Date()
) {
    /**
     * Calculate completion percentage for progress bar
     */
    fun getCompletionPercentage(): Float {
        return if (targetCount > 0) {
            (completedCount.toFloat() / targetCount.toFloat()).coerceAtMost(1.0f)
        } else 0f
    }
    
    /**
     * Check if habit is fully completed for today
     */
    fun isFullyCompleted(): Boolean {
        return completedCount >= targetCount
    }
}
