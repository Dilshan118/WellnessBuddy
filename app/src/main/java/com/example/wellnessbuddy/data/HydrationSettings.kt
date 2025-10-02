package com.example.wellnessbuddy.data

/**
 * Data class representing hydration settings and tracking
 */
data class HydrationSettings(
    val dailyGoal: Int = 8, // Glasses of water per day
    val reminderInterval: Int = 2, // Hours between reminders
    val isEnabled: Boolean = true,
    val startTime: String = "08:00", // When to start reminders (24h format)
    val endTime: String = "22:00", // When to stop reminders (24h format)
    val glassesConsumed: Int = 0, // Glasses consumed today
    val lastReminderTime: Long = 0L // Last reminder timestamp
) {
    /**
     * Calculate completion percentage for daily goal
     */
    fun getCompletionPercentage(): Float {
        return if (dailyGoal > 0) {
            (glassesConsumed.toFloat() / dailyGoal.toFloat()).coerceAtMost(1.0f)
        } else 0f
    }
    
    /**
     * Check if daily goal is reached
     */
    fun isGoalReached(): Boolean {
        return glassesConsumed >= dailyGoal
    }
    
    /**
     * Get remaining glasses needed
     */
    fun getRemainingGlasses(): Int {
        return (dailyGoal - glassesConsumed).coerceAtLeast(0)
    }
}
