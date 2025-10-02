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
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textview.MaterialTextView
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

/**
 * Home Dashboard Fragment - Overview of habits, moods, and hydration
 */
class HomeFragment : Fragment() {
    
    private lateinit var habitManager: HabitManager
    private lateinit var moodManager: MoodManager
    private lateinit var hydrationManager: HydrationManager
    
    private lateinit var habitsRecyclerView: RecyclerView
    private lateinit var moodCard: MaterialCardView
    private lateinit var hydrationCard: MaterialCardView
    private lateinit var hydrationProgress: CircularProgressIndicator
    private lateinit var hydrationText: MaterialTextView
    private lateinit var addHabitBtn: MaterialButton
    private lateinit var addMoodBtn: MaterialButton
    
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
        
        // Initialize views
        habitsRecyclerView = view.findViewById(R.id.habits_recycler_view)
        moodCard = view.findViewById(R.id.mood_card)
        hydrationCard = view.findViewById(R.id.hydration_card)
        hydrationProgress = view.findViewById(R.id.hydration_progress)
        hydrationText = view.findViewById(R.id.hydration_text)
        addHabitBtn = view.findViewById<MaterialButton>(R.id.add_habit_btn)
        addMoodBtn = view.findViewById<MaterialButton>(R.id.add_mood_btn)
        
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
        val habitsAdapter = HabitsAdapter(updatedHabits) { habit ->
            habitManager.completeHabit(habit.id)
            loadData() // Refresh data
        }
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
    
    override fun onResume() {
        super.onResume()
        loadData() // Refresh data when returning to this fragment
    }
}
