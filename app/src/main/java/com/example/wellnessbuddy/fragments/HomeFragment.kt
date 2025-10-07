package com.example.wellnessbuddy.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnessbuddy.R
import com.example.wellnessbuddy.data.Habit
import com.example.wellnessbuddy.data.HabitManager
import com.example.wellnessbuddy.data.MoodEntry
import com.example.wellnessbuddy.data.MoodManager
import com.example.wellnessbuddy.data.HydrationSettings
import com.example.wellnessbuddy.data.HydrationManager
import com.example.wellnessbuddy.sensors.WellnessSensorManager
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textview.MaterialTextView
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*
import java.text.DecimalFormat

/**
 * Home Dashboard Fragment - Overview of habits, moods, and hydration
 */
class HomeFragment : Fragment() {
    
    private lateinit var habitManager: HabitManager
    private lateinit var moodManager: MoodManager
    private lateinit var hydrationManager: HydrationManager
    private lateinit var sensorManager: WellnessSensorManager
    
    private lateinit var habitsRecyclerView: RecyclerView
    private lateinit var moodCard: MaterialCardView
    private lateinit var hydrationCard: MaterialCardView
    private lateinit var hydrationProgress: CircularProgressIndicator
    private lateinit var hydrationText: MaterialTextView
    private lateinit var addHabitBtn: MaterialButton
    private lateinit var addMoodBtn: MaterialButton
    
    // Sensor UI elements
    private lateinit var stepsCount: MaterialTextView
    private lateinit var sensorStatus: MaterialTextView
    private lateinit var accelerationValue: MaterialTextView
    private lateinit var resetStepsBtn: MaterialButton
    private lateinit var sensorCard: MaterialCardView
    
    private val decimalFormat = DecimalFormat("#0.0")
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize managers
        habitManager = HabitManager(requireContext())
        moodManager = MoodManager(requireContext())
        hydrationManager = HydrationManager(requireContext())
        sensorManager = WellnessSensorManager(requireContext())
        
        // Initialize views
        habitsRecyclerView = view.findViewById(R.id.habits_recycler_view)
        moodCard = view.findViewById(R.id.mood_card)
        hydrationCard = view.findViewById(R.id.hydration_card)
        hydrationProgress = view.findViewById(R.id.hydration_progress)
        hydrationText = view.findViewById(R.id.hydration_text)
        addHabitBtn = view.findViewById<MaterialButton>(R.id.add_habit_btn)
        addMoodBtn = view.findViewById<MaterialButton>(R.id.add_mood_btn)
        
        // Initialize sensor views
        stepsCount = view.findViewById(R.id.steps_count)
        sensorStatus = view.findViewById(R.id.sensor_status)
        accelerationValue = view.findViewById(R.id.acceleration_value)
        resetStepsBtn = view.findViewById(R.id.reset_steps_btn)
        sensorCard = view.findViewById(R.id.sensor_card)
        
        setupViews()
        loadData()
    }
    
    private fun setupViews() {
        // Setup habits recycler view
        habitsRecyclerView.layoutManager = LinearLayoutManager(context)
        
        // Setup mood card click listener
        moodCard.setOnClickListener {
            // Navigate to mood journal
            (activity as? com.example.wellnessbuddy.MainActivity)?.let { mainActivity ->
                mainActivity.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
                    .selectedItemId = R.id.nav_mood
            }
        }
        
        // Setup hydration card click listener
        hydrationCard.setOnClickListener {
            // Navigate to hydration tracker
            (activity as? com.example.wellnessbuddy.MainActivity)?.let { mainActivity ->
                mainActivity.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
                    .selectedItemId = R.id.nav_hydration
            }
        }
        
        // Setup quick action buttons
        addHabitBtn.setOnClickListener {
            (activity as? com.example.wellnessbuddy.MainActivity)?.let { mainActivity ->
                mainActivity.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
                    .selectedItemId = R.id.nav_habits
            }
        }
        
        addMoodBtn.setOnClickListener {
            (activity as? com.example.wellnessbuddy.MainActivity)?.let { mainActivity ->
                mainActivity.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
                    .selectedItemId = R.id.nav_mood
            }
        }
        
        // Setup sensor UI
        setupSensorUI()
    }
    
    private fun loadData() {
        // Reset daily progress
        habitManager.resetDailyProgress()
        hydrationManager.resetDailyConsumption()
        
        // Initialize sample data if no habits exist
        val habits = habitManager.loadHabits()
        if (habits.isEmpty()) {
            initializeSampleData()
        }
        
        // Load habits
        val updatedHabits = habitManager.loadHabits()
        val habitsAdapter = HabitsAdapter(
            habits = updatedHabits,
            onCompleteClick = { habit ->
                habitManager.completeHabit(habit.id)
                loadData() // Refresh data
            },
            onEditClick = { habit ->
                // Could navigate to edit or show toast
            },
            onDeleteClick = { habit ->
                habitManager.deleteHabit(habit.id)
                loadData() // Refresh data
            }
        )
        habitsRecyclerView.adapter = habitsAdapter
        
        // Load today's mood
        val todayMood = moodManager.getMoodsForDate(Date()).firstOrNull()
        updateMoodCard(todayMood)
        
        // Load hydration progress
        val hydrationSettings = hydrationManager.loadSettings()
        updateHydrationCard(hydrationSettings)
    }
    
    private fun initializeSampleData() {
        // Add sample habits
        val sampleHabits = listOf(
            Habit(name = "Morning Exercise", description = "30 minutes of physical activity", icon = "🏃", targetCount = 1),
            Habit(name = "Drink Water", description = "Stay hydrated throughout the day", icon = "💧", targetCount = 8),
            Habit(name = "Read Books", description = "Read for at least 30 minutes", icon = "📚", targetCount = 1),
            Habit(name = "Meditation", description = "10 minutes of mindfulness", icon = "🧘", targetCount = 1)
        )
        
        sampleHabits.forEach { habit ->
            habitManager.addHabit(habit)
        }
    }
    
    private fun updateMoodCard(mood: MoodEntry?) {
        val moodEmoji = view?.findViewById<MaterialTextView>(R.id.mood_emoji)
        val moodText = view?.findViewById<MaterialTextView>(R.id.mood_text)
        
        if (mood != null) {
            moodEmoji?.text = mood.emoji
            moodText?.text = "${mood.moodName} (${mood.intensity}/10)"
        } else {
            moodEmoji?.text = "😊"
            moodText?.text = "Log your mood today"
        }
    }
    
    private fun updateHydrationCard(settings: HydrationSettings) {
        val progress = settings.getCompletionPercentage()
        hydrationProgress.progress = (progress * 100).toInt()
        hydrationText.text = "${settings.glassesConsumed}/${settings.dailyGoal} glasses"
    }
    
    private fun setupSensorUI() {
        // Setup sensor callbacks
        sensorManager.setOnStepDetected { stepCount ->
            updateStepCount(stepCount)
        }
        
        sensorManager.setOnSensorStatusChanged { isActive ->
            updateSensorStatus(isActive)
        }
        
        sensorManager.setOnAccelerationChanged { acceleration ->
            updateAccelerationDisplay(acceleration)
        }
        
        sensorManager.setOnShakeDetected {
            // Shake detected - this will be handled by MainActivity
            // Just show a visual feedback here
            showShakeFeedback()
        }
        
        // Setup reset steps button
        resetStepsBtn.setOnClickListener {
            sensorManager.resetStepCount()
        }
        
        // Initialize sensor display
        updateStepCount(sensorManager.getStepCount())
        updateSensorStatus(sensorManager.isCurrentlyListening())
        updateAccelerationDisplay(sensorManager.getLastAcceleration())
        
        // Start sensor listening if available
        if (sensorManager.isSensorAvailable()) {
            sensorManager.startListening()
        } else {
            updateSensorStatus(false)
            sensorStatus.text = "❌"
            sensorStatus.contentDescription = "Sensor not available"
        }
    }
    
    private fun updateStepCount(count: Int) {
        stepsCount.text = count.toString()
    }
    
    private fun updateSensorStatus(isActive: Boolean) {
        sensorStatus.text = if (isActive) "🟢" else "🔴"
        sensorStatus.contentDescription = if (isActive) "Sensor active" else "Sensor inactive"
        
        // Update sensor card background based on status
        sensorCard.setCardBackgroundColor(
            if (isActive) {
                requireContext().getColor(R.color.modern_card)
            } else {
                requireContext().getColor(R.color.modern_surface)
            }
        )
    }
    
    private fun updateAccelerationDisplay(acceleration: Float) {
        accelerationValue.text = "${decimalFormat.format(acceleration)} m/s²"
        
        // Change color based on acceleration level
        val color = when {
            acceleration > sensorManager.getShakeThreshold() -> R.color.status_error
            acceleration > sensorManager.getStepThreshold() -> R.color.neon_warning
            else -> R.color.neon_primary
        }
        accelerationValue.setTextColor(requireContext().getColor(color))
    }
    
    private fun showShakeFeedback() {
        // Add visual feedback for shake detection
        sensorCard.animate()
            .scaleX(1.05f)
            .scaleY(1.05f)
            .setDuration(100)
            .withEndAction {
                sensorCard.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(100)
                    .start()
            }
            .start()
    }
    
    override fun onResume() {
        super.onResume()
        loadData() // Refresh data when returning to this fragment
        
        // Restart sensor listening
        if (sensorManager.isSensorAvailable()) {
            sensorManager.startListening()
        }
    }
    
    override fun onPause() {
        super.onPause()
        // Stop sensor listening to save battery
        sensorManager.stopListening()
    }
}
