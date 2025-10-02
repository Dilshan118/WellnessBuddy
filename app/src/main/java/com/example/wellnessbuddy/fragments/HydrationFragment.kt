package com.example.wellnessbuddy.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.example.wellnessbuddy.R
import com.example.wellnessbuddy.data.HydrationManager
import com.example.wellnessbuddy.data.HydrationSettings
import com.example.wellnessbuddy.notifications.HydrationReminderManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textview.MaterialTextView

/**
 * Fragment for hydration tracking and reminders
 */
class HydrationFragment : Fragment() {
    
    private lateinit var hydrationManager: HydrationManager
    private lateinit var reminderManager: HydrationReminderManager
    private lateinit var hydrationProgress: CircularProgressIndicator
    private lateinit var hydrationText: MaterialTextView
    private lateinit var addGlassBtn: MaterialButton
    private lateinit var removeGlassBtn: MaterialButton
    private lateinit var settingsBtn: MaterialButton
    private lateinit var hydrationCard: MaterialCardView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_hydration, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        hydrationManager = HydrationManager(requireContext())
        reminderManager = HydrationReminderManager(requireContext())
        
        hydrationProgress = view.findViewById(R.id.hydration_progress)
        hydrationText = view.findViewById(R.id.hydration_text)
        addGlassBtn = view.findViewById(R.id.add_glass_btn)
        removeGlassBtn = view.findViewById(R.id.remove_glass_btn)
        settingsBtn = view.findViewById(R.id.settings_btn)
        hydrationCard = view.findViewById(R.id.hydration_card)
        
        setupViews()
        loadData()
    }
    
    private fun setupViews() {
        addGlassBtn.setOnClickListener {
            hydrationManager.addGlass()
            loadData()
        }
        
        removeGlassBtn.setOnClickListener {
            hydrationManager.removeGlass()
            loadData()
        }
        
        settingsBtn.setOnClickListener {
            showSettingsDialog()
        }
        
        hydrationCard.setOnClickListener {
            showSettingsDialog()
        }
    }
    
    private fun loadData() {
        val settings = hydrationManager.loadSettings()
        updateHydrationDisplay(settings)
    }
    
    private fun updateHydrationDisplay(settings: HydrationSettings) {
        val progress = settings.getCompletionPercentage()
        hydrationProgress.progress = (progress * 100).toInt()
        hydrationText.text = "${settings.glassesConsumed}/${settings.dailyGoal} glasses"
        
        // Update button states
        removeGlassBtn.isEnabled = settings.glassesConsumed > 0
        
        // Show completion message
        if (settings.isGoalReached()) {
            hydrationText.text = "🎉 Goal reached! ${settings.glassesConsumed}/${settings.dailyGoal} glasses"
        } else {
            val remaining = settings.getRemainingGlasses()
            hydrationText.text = "${settings.glassesConsumed}/${settings.dailyGoal} glasses (${remaining} remaining)"
        }
    }
    
    private fun showSettingsDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_hydration_settings, null)
        val dailyGoalEditText = dialogView.findViewById<EditText>(R.id.daily_goal_edit)
        val reminderIntervalSeekBar = dialogView.findViewById<SeekBar>(R.id.reminder_interval_seekbar)
        val reminderIntervalText = dialogView.findViewById<MaterialTextView>(R.id.reminder_interval_text)
        val enableRemindersCheckbox = dialogView.findViewById<android.widget.CheckBox>(R.id.enable_reminders_checkbox)
        
        val currentSettings = hydrationManager.loadSettings()
        
        // Pre-fill with current settings
        dailyGoalEditText.setText(currentSettings.dailyGoal.toString())
        reminderIntervalSeekBar.max = 5 // 1-6 hours
        reminderIntervalSeekBar.progress = currentSettings.reminderInterval - 1
        reminderIntervalText.text = "Reminder every ${currentSettings.reminderInterval} hours"
        enableRemindersCheckbox.isChecked = currentSettings.isEnabled
        
        reminderIntervalSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val hours = progress + 1
                reminderIntervalText.text = "Reminder every $hours hours"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        AlertDialog.Builder(requireContext())
            .setTitle("Hydration Settings")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val dailyGoal = dailyGoalEditText.text.toString().toIntOrNull() ?: 8
                val reminderInterval = reminderIntervalSeekBar.progress + 1
                val isEnabled = enableRemindersCheckbox.isChecked
                
                val newSettings = currentSettings.copy(
                    dailyGoal = dailyGoal,
                    reminderInterval = reminderInterval,
                    isEnabled = isEnabled
                )
                hydrationManager.saveSettings(newSettings)
                
                // Update reminder schedule
                if (isEnabled) {
                    reminderManager.scheduleReminders()
                } else {
                    reminderManager.cancelReminders()
                }
                
                loadData()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onResume() {
        super.onResume()
        loadData()
    }
}
