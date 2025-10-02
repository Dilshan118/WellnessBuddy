package com.example.wellnessbuddy.data

import java.util.Date

/**
 * Data class representing a mood entry
 */
data class MoodEntry(
    val id: String = "",
    val emoji: String = "", // Emoji representing the mood
    val moodName: String = "", // Name of the mood (e.g., "Happy", "Sad", "Excited")
    val notes: String = "", // Optional notes about the mood
    val date: Date = Date(),
    val intensity: Int = 5 // Scale from 1-10
) {
    companion object {
        // Predefined mood options with emojis
        val MOOD_OPTIONS = listOf(
            MoodOption("😊", "Happy", 8),
            MoodOption("😄", "Excited", 9),
            MoodOption("😌", "Calm", 6),
            MoodOption("😐", "Neutral", 5),
            MoodOption("😔", "Sad", 3),
            MoodOption("😰", "Anxious", 3),
            MoodOption("😴", "Tired", 4),
            MoodOption("🤗", "Grateful", 7),
            MoodOption("😤", "Frustrated", 3),
            MoodOption("😍", "Loved", 9)
        )
    }
}

/**
 * Data class for mood options in the selector
 */
data class MoodOption(
    val emoji: String,
    val name: String,
    val defaultIntensity: Int
)
