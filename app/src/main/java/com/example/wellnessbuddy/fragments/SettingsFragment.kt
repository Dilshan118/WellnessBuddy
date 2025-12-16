package com.example.wellnessbuddy.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.wellnessbuddy.R
import com.example.wellnessbuddy.data.HydrationManager
import com.example.wellnessbuddy.data.AuthManager
import com.example.wellnessbuddy.data.OnboardingManager
import com.example.wellnessbuddy.data.HydrationSettings
import com.example.wellnessbuddy.data.ReminderIntervalType
import com.example.wellnessbuddy.notifications.HydrationReminderManager
import com.example.wellnessbuddy.theme.ThemeManager
import com.example.wellnessbuddy.dialogs.ThemeSelectorDialog
import com.example.wellnessbuddy.sensors.WellnessSensorManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView

/**
 * Fragment for app settings and preferences
 */
class SettingsFragment : Fragment() {
    
    private lateinit var hydrationManager: HydrationManager
    private lateinit var authManager: AuthManager
    private lateinit var onboardingManager: OnboardingManager
    private lateinit var themeManager: ThemeManager
    private lateinit var sensorManager: WellnessSensorManager
    private lateinit var reminderManager: HydrationReminderManager
    private lateinit var hydrationGoalText: MaterialTextView
    private lateinit var sensorStatusText: MaterialTextView
    private lateinit var notificationSettingsCard: MaterialCardView
    private lateinit var themeSettingsCard: MaterialCardView
    private lateinit var profileSettingsCard: MaterialCardView
    private lateinit var sensorSettingsCard: MaterialCardView
    private lateinit var aboutCard: MaterialCardView
    private lateinit var shareDataBtn: MaterialButton
    private lateinit var profilePreviewText: MaterialTextView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        hydrationManager = HydrationManager(requireContext())
        authManager = AuthManager(requireContext())
        onboardingManager = OnboardingManager(requireContext())
        themeManager = ThemeManager(requireContext())
        sensorManager = WellnessSensorManager(requireContext())
        reminderManager = HydrationReminderManager(requireContext())
        
        hydrationGoalText = view.findViewById(R.id.hydration_goal_text)
        sensorStatusText = view.findViewById(R.id.sensor_status_text)
        profilePreviewText = view.findViewById(R.id.profile_preview_text)
        notificationSettingsCard = view.findViewById(R.id.notification_settings_card)
        themeSettingsCard = view.findViewById(R.id.theme_settings_card)
        profileSettingsCard = view.findViewById(R.id.profile_settings_card)
        sensorSettingsCard = view.findViewById(R.id.sensor_settings_card)
        aboutCard = view.findViewById(R.id.about_card)
        shareDataBtn = view.findViewById(R.id.share_data_btn)
        
        setupViews()
        loadSettings()
    }
    
    private fun setupViews() {
        notificationSettingsCard.setOnClickListener {
            showNotificationSettingsDialog()
        }
        
        themeSettingsCard.setOnClickListener {
            showThemeSelectorDialog()
        }
        
        profileSettingsCard.setOnClickListener {
            navigateToProfile()
        }
        
        sensorSettingsCard.setOnClickListener {
            showSensorSettingsDialog()
        }
        
        aboutCard.setOnClickListener {
            showAboutDialog()
        }
        
        shareDataBtn.setOnClickListener {
            shareWellnessData()
        }
        
        // Add logout functionality
        view?.findViewById<MaterialCardView>(R.id.logout_card)?.setOnClickListener {
            showLogoutDialog()
        }
        
        // Add view onboarding functionality
        view?.findViewById<MaterialCardView>(R.id.view_onboarding_card)?.setOnClickListener {
            showViewOnboardingDialog()
        }
    }
    
    private fun loadSettings() {
        val hydrationSettings = hydrationManager.loadSettings()
        hydrationGoalText.text = "${hydrationSettings.dailyGoal} glasses • ${hydrationSettings.getFormattedReminderInterval()}"
        
        // Update sensor status
        updateSensorStatusDisplay()
        
        // Update profile preview
        updateProfilePreview()
    }
    
    private fun updateSensorStatusDisplay() {
        val isAvailable = sensorManager.isSensorAvailable()
        val isActive = sensorManager.isCurrentlyListening()
        
        sensorStatusText.text = when {
            !isAvailable -> "Not Available"
            isActive -> "Active"
            else -> "Inactive"
        }
        
        // Update text color based on status
        val color = when {
            !isAvailable -> R.color.status_error
            isActive -> R.color.neon_success
            else -> R.color.neon_warning
        }
        sensorStatusText.setTextColor(requireContext().getColor(color))
    }
    
    private fun updateProfilePreview() {
        val currentUser = authManager.getCurrentUser()
        if (currentUser != null) {
            profilePreviewText.text = currentUser.username
        } else {
            profilePreviewText.text = "View & Edit"
        }
    }
    
    private fun navigateToProfile() {
        // Use Navigation Component to navigate to profile
        findNavController().navigate(R.id.action_nav_settings_to_profileFragment)
    }
    
    private fun showNotificationSettingsDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_notification_settings, null)
        val currentSettings = hydrationManager.loadSettings()
        
        // Initialize views
        val hydrationGoalEdit = dialogView.findViewById<EditText>(R.id.hydration_goal_edit)
        val minutesBtn = dialogView.findViewById<MaterialButton>(R.id.minutes_btn)
        val hoursBtn = dialogView.findViewById<MaterialButton>(R.id.hours_btn)
        val reminderIntervalEdit = dialogView.findViewById<EditText>(R.id.reminder_interval_edit)
        val intervalSuffixLayout = dialogView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.interval_suffix_layout)
        val startTimeBtn = dialogView.findViewById<MaterialButton>(R.id.start_time_btn)
        val endTimeBtn = dialogView.findViewById<MaterialButton>(R.id.end_time_btn)
        val reminderEnabledSwitch = dialogView.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.reminder_enabled_switch)
        
        // Quick preset buttons
        val preset15minBtn = dialogView.findViewById<MaterialButton>(R.id.preset_15min_btn)
        val preset30minBtn = dialogView.findViewById<MaterialButton>(R.id.preset_30min_btn)
        val preset1hrBtn = dialogView.findViewById<MaterialButton>(R.id.preset_1hr_btn)
        val preset2hrBtn = dialogView.findViewById<MaterialButton>(R.id.preset_2hr_btn)
        
        // Test notification button
        val testNotificationBtn = dialogView.findViewById<MaterialButton>(R.id.test_notification_btn)
        
        // Load current settings
        hydrationGoalEdit.setText(currentSettings.dailyGoal.toString())
        reminderIntervalEdit.setText(currentSettings.getReminderIntervalValue().toString())
        startTimeBtn.text = currentSettings.startTime
        endTimeBtn.text = currentSettings.endTime
        reminderEnabledSwitch.isChecked = currentSettings.isEnabled
        
        // Set initial interval type
        updateIntervalTypeUI(currentSettings.reminderIntervalType, minutesBtn, hoursBtn, intervalSuffixLayout)
        
        // Interval type button listeners
        minutesBtn.setOnClickListener {
            updateIntervalTypeUI(ReminderIntervalType.MINUTES, minutesBtn, hoursBtn, intervalSuffixLayout)
        }
        
        hoursBtn.setOnClickListener {
            updateIntervalTypeUI(ReminderIntervalType.HOURS, minutesBtn, hoursBtn, intervalSuffixLayout)
        }
        
        // Quick preset button listeners
        preset15minBtn.setOnClickListener {
            updateIntervalTypeUI(ReminderIntervalType.MINUTES, minutesBtn, hoursBtn, intervalSuffixLayout)
            reminderIntervalEdit.setText("15")
        }
        
        preset30minBtn.setOnClickListener {
            updateIntervalTypeUI(ReminderIntervalType.MINUTES, minutesBtn, hoursBtn, intervalSuffixLayout)
            reminderIntervalEdit.setText("30")
        }
        
        preset1hrBtn.setOnClickListener {
            updateIntervalTypeUI(ReminderIntervalType.HOURS, minutesBtn, hoursBtn, intervalSuffixLayout)
            reminderIntervalEdit.setText("1")
        }
        
        preset2hrBtn.setOnClickListener {
            updateIntervalTypeUI(ReminderIntervalType.HOURS, minutesBtn, hoursBtn, intervalSuffixLayout)
            reminderIntervalEdit.setText("2")
        }
        
        // Time picker listeners
        startTimeBtn.setOnClickListener {
            showTimePickerDialog("Start Time") { time ->
                startTimeBtn.text = time
            }
        }
        
        endTimeBtn.setOnClickListener {
            showTimePickerDialog("End Time") { time ->
                endTimeBtn.text = time
            }
        }
        
        // Test notification button
        testNotificationBtn.setOnClickListener {
            try {
                if (::reminderManager.isInitialized) {
                    // Check and request notification permissions for Android 13+
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        if (androidx.core.content.ContextCompat.checkSelfPermission(
                                requireContext(),
                                android.Manifest.permission.POST_NOTIFICATIONS
                            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
                        ) {
                            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
                            android.widget.Toast.makeText(requireContext(), "Please grant notification permission and try again", android.widget.Toast.LENGTH_LONG).show()
                            return@setOnClickListener
                        }
                    }
                    reminderManager.showHydrationNotification()
                    
                    // Show debug info
                    val status = reminderManager.checkNotificationStatus()
                    android.util.Log.d("SettingsFragment", "Notification Status:\n$status")
                    
                    android.widget.Toast.makeText(requireContext(), "Test notification sent! Check your notification panel", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    android.widget.Toast.makeText(requireContext(), "Reminder manager not ready", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.util.Log.e("SettingsFragment", "Error showing test notification", e)
                android.widget.Toast.makeText(requireContext(), "Error showing notification", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("💧 Hydration Reminder Settings")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val goal = hydrationGoalEdit.text.toString().toIntOrNull() ?: 8
                val interval = reminderIntervalEdit.text.toString().toIntOrNull() ?: 120
                val intervalType = if (minutesBtn.backgroundTintList == requireContext().getColorStateList(R.color.neon_primary)) {
                    ReminderIntervalType.MINUTES
                } else {
                    ReminderIntervalType.HOURS
                }
                
                val updatedSettings = currentSettings.copy(
                    dailyGoal = goal,
                    reminderInterval = interval,
                    reminderIntervalType = intervalType,
                    startTime = startTimeBtn.text.toString(),
                    endTime = endTimeBtn.text.toString(),
                    isEnabled = reminderEnabledSwitch.isChecked
                )
                
                hydrationManager.saveSettings(updatedSettings)
                
                // Schedule or cancel reminders based on settings
                try {
                    if (::reminderManager.isInitialized) {
                        if (updatedSettings.isEnabled) {
                            reminderManager.scheduleReminders()
                            // For testing: show a notification immediately if interval is very short
                            if (updatedSettings.reminderIntervalType == ReminderIntervalType.MINUTES && 
                                updatedSettings.reminderInterval <= 5) {
                                reminderManager.showHydrationNotification()
                            }
                        } else {
                            reminderManager.cancelReminders()
                        }
                    } else {
                        android.util.Log.w("SettingsFragment", "ReminderManager not initialized")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("SettingsFragment", "Error managing reminders", e)
                    // Show a simple toast to user
                    android.widget.Toast.makeText(requireContext(), "Settings saved, but there was an issue with reminders", android.widget.Toast.LENGTH_SHORT).show()
                }
                
                loadSettings()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showThemeSelectorDialog() {
        val dialog = ThemeSelectorDialog.newInstance { selectedTheme ->
            themeManager.setTheme(selectedTheme)
            // Restart activity to apply theme changes
            activity?.recreate()
        }
        dialog.show(parentFragmentManager, "theme_selector")
    }
    
    private fun showSensorSettingsDialog() {
        val isAvailable = sensorManager.isSensorAvailable()
        val isActive = sensorManager.isCurrentlyListening()
        val stepCount = sensorManager.getStepCount()
        val lastAcceleration = sensorManager.getLastAcceleration()
        
        val message = """
            📱 Sensor Information
            
            Status: ${if (isAvailable) "Available" else "Not Available"}
            Active: ${if (isActive) "Yes" else "No"}
            
            📊 Current Data:
            • Steps Today: $stepCount
            • Last Acceleration: ${String.format("%.1f", lastAcceleration)} m/s²
            
            ⚙️ Settings:
            • Shake Threshold: ${sensorManager.getShakeThreshold()} m/s²
            • Step Threshold: ${sensorManager.getStepThreshold()} m/s²
            
            💡 Tips:
            • Shake your device to log moods quickly
            • Walk with your device to count steps
            • Sensors help track your activity automatically
        """.trimIndent()
        
        AlertDialog.Builder(requireContext())
            .setTitle("📱 Sensor Settings")
            .setMessage(message)
            .setPositiveButton("Reset Steps") { _, _ ->
                sensorManager.resetStepCount()
                updateSensorStatusDisplay()
            }
            .setNegativeButton("Close", null)
            .show()
    }
    
    private fun showAboutDialog() {
        val message = """
            🧘‍♀️ WellnessBuddy v1.0
            
            A modern wellness tracking app to help you:
            • Track daily habits
            • Monitor your mood
            • Stay hydrated
            • Smart sensor features (shake to log moods, auto step counting)
            • Build healthy routines
            
            Built with ❤️ for your wellness journey.
            
            💡 Tip: Try the "View Onboarding" option to see all features including the new sensor capabilities!
        """.trimIndent()
        
        AlertDialog.Builder(requireContext())
            .setTitle("About WellnessBuddy")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun shareWellnessData() {
        val hydrationSettings = hydrationManager.loadSettings()
        val shareText = """
            My Wellness Summary 📊
            
            💧 Hydration Progress: ${hydrationSettings.glassesConsumed}/${hydrationSettings.dailyGoal} glasses today
            
            🌟 Keep up the great work on your wellness journey!
            
            #WellnessBuddy #HealthyLiving
        """.trimIndent()
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        
        startActivity(Intent.createChooser(shareIntent, "Share Wellness Data"))
    }
    
    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout? You'll need to sign in again to access your data.")
            .setPositiveButton("Logout") { _, _ ->
                authManager.logout()
                // Navigate to auth activity
                val intent = Intent(requireContext(), com.example.wellnessbuddy.AuthActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                activity?.finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showViewOnboardingDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("View Onboarding")
            .setMessage("Would you like to view the onboarding screens again? This will take you through the app introduction.")
            .setPositiveButton("View Onboarding") { _, _ ->
                // Reset onboarding state and navigate to AuthActivity
                onboardingManager.resetOnboarding()
                val intent = Intent(requireContext(), com.example.wellnessbuddy.AuthActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                activity?.finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onResume() {
        super.onResume()
        loadSettings()
        updateSensorStatusDisplay()
    }
    
    private fun updateIntervalTypeUI(
        type: ReminderIntervalType,
        minutesBtn: MaterialButton,
        hoursBtn: MaterialButton,
        intervalSuffixLayout: com.google.android.material.textfield.TextInputLayout
    ) {
        when (type) {
            ReminderIntervalType.MINUTES -> {
                minutesBtn.backgroundTintList = requireContext().getColorStateList(R.color.neon_primary)
                minutesBtn.setTextColor(requireContext().getColor(R.color.text_primary))
                hoursBtn.backgroundTintList = requireContext().getColorStateList(R.color.modern_surface)
                hoursBtn.setTextColor(requireContext().getColor(R.color.text_secondary))
                intervalSuffixLayout.suffixText = " min"
            }
            ReminderIntervalType.HOURS -> {
                hoursBtn.backgroundTintList = requireContext().getColorStateList(R.color.neon_primary)
                hoursBtn.setTextColor(requireContext().getColor(R.color.text_primary))
                minutesBtn.backgroundTintList = requireContext().getColorStateList(R.color.modern_surface)
                minutesBtn.setTextColor(requireContext().getColor(R.color.text_secondary))
                intervalSuffixLayout.suffixText = " hr"
            }
        }
    }
    
    private fun showTimePickerDialog(title: String, onTimeSelected: (String) -> Unit) {
        val timePickerDialog = android.app.TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val timeString = String.format("%02d:%02d", hourOfDay, minute)
                onTimeSelected(timeString)
            },
            8, 0, true
        )
        timePickerDialog.setTitle(title)
        timePickerDialog.show()
    }
}
