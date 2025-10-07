package com.example.wellnessbuddy.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Data class representing an onboarding screen
 */
data class OnboardingPage(
    val id: Int,
    val title: String,
    val description: String,
    val icon: String, // Emoji or icon identifier
    val backgroundColor: String = "gradient_1", // Gradient identifier
    val isLastPage: Boolean = false
)

/**
 * Manager class for handling onboarding state
 */
class OnboardingManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("onboarding", Context.MODE_PRIVATE)
    
    companion object {
        private const val ONBOARDING_COMPLETED_KEY = "onboarding_completed"
        private const val ONBOARDING_VERSION_KEY = "onboarding_version"
        private const val CURRENT_ONBOARDING_VERSION = 1
    }
    
    /**
     * Check if user has completed onboarding
     */
    fun isOnboardingCompleted(): Boolean {
        val version = prefs.getInt(ONBOARDING_VERSION_KEY, 0)
        return prefs.getBoolean(ONBOARDING_COMPLETED_KEY, false) && version >= CURRENT_ONBOARDING_VERSION
    }
    
    /**
     * Mark onboarding as completed
     */
    fun completeOnboarding() {
        prefs.edit()
            .putBoolean(ONBOARDING_COMPLETED_KEY, true)
            .putInt(ONBOARDING_VERSION_KEY, CURRENT_ONBOARDING_VERSION)
            .apply()
    }
    
    /**
     * Reset onboarding (for testing or if onboarding is updated)
     */
    fun resetOnboarding() {
        prefs.edit()
            .putBoolean(ONBOARDING_COMPLETED_KEY, false)
            .putInt(ONBOARDING_VERSION_KEY, 0)
            .apply()
    }
    
    /**
     * Get onboarding pages
     */
    fun getOnboardingPages(): List<OnboardingPage> {
        return listOf(
            OnboardingPage(
                id = 1,
                title = "🌟 Welcome to WellnessBuddy",
                description = "Your personal companion for building healthy habits and tracking your wellness journey.",
                icon = "🧘‍♀️",
                backgroundColor = "gradient_1"
            ),
            OnboardingPage(
                id = 2,
                title = "Track Your Habits",
                description = "Build and maintain daily habits that matter to you. Track your progress and celebrate your achievements.",
                icon = "📈",
                backgroundColor = "gradient_2"
            ),
            OnboardingPage(
                id = 3,
                title = "Monitor Your Mood",
                description = "Log your daily mood and emotions to understand patterns and improve your mental wellbeing.",
                icon = "😊",
                backgroundColor = "gradient_3"
            ),
            OnboardingPage(
                id = 4,
                title = "Stay Hydrated",
                description = "Set hydration goals and get gentle reminders to keep your body properly hydrated throughout the day.",
                icon = "💧",
                backgroundColor = "gradient_1"
            ),
            OnboardingPage(
                id = 5,
                title = "Ready to Begin?",
                description = "You're all set! Create your account and start your wellness journey with personalized tracking and insights.",
                icon = "🚀",
                backgroundColor = "gradient_2",
                isLastPage = true
            )
        )
    }
}
