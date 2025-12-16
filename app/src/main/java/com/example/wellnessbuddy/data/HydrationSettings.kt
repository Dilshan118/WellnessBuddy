package com.example.wellnessbuddy.data

/**
 * Enum representing the type of reminder interval
 */
enum class ReminderIntervalType(val displayName: String, val unit: String) {
    MINUTES("Minutes", "min"),
    HOURS("Hours", "hr")
}

/**
 * Data class representing hydration settings and tracking
 */
data class HydrationSettings(
    val dailyGoal: Int = 8, // Glasses of water per day
    val reminderInterval: Int = 120, // Minutes between reminders (default 2 hours = 120 minutes)
    val reminderIntervalType: ReminderIntervalType = ReminderIntervalType.MINUTES, // Type of interval
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
    
    /**
     * Get reminder interval in milliseconds
     */
    fun getReminderIntervalMs(): Long {
        return when (reminderIntervalType) {
            ReminderIntervalType.MINUTES -> reminderInterval * 60 * 1000L
            ReminderIntervalType.HOURS -> reminderInterval * 60 * 60 * 1000L
        }
    }
    
    /**
     * Get formatted reminder interval string
     */
    fun getFormattedReminderInterval(): String {
        return when (reminderIntervalType) {
            ReminderIntervalType.MINUTES -> {
                if (reminderInterval >= 60) {
                    val hours = reminderInterval / 60
                    val minutes = reminderInterval % 60
                    if (minutes == 0) {
                        "$hours ${if (hours == 1) "hour" else "hours"}"
                    } else {
                        "$hours ${if (hours == 1) "hour" else "hours"} $minutes ${if (minutes == 1) "minute" else "minutes"}"
                    }
                } else {
                    "$reminderInterval ${if (reminderInterval == 1) "minute" else "minutes"}"
                }
            }
            ReminderIntervalType.HOURS -> {
                "$reminderInterval ${if (reminderInterval == 1) "hour" else "hours"}"
            }
        }
    }
    
    /**
     * Get reminder interval value for display
     */
    fun getReminderIntervalValue(): Int {
        return when (reminderIntervalType) {
            ReminderIntervalType.MINUTES -> reminderInterval
            ReminderIntervalType.HOURS -> reminderInterval
        }
    }
    
    /**
     * Create a new settings object with updated interval
     */
    fun updateReminderInterval(value: Int, type: ReminderIntervalType): HydrationSettings {
        return copy(
            reminderInterval = value,
            reminderIntervalType = type
        )
    }
}
