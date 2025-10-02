package com.example.wellnessbuddy.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnessbuddy.R
import com.example.wellnessbuddy.data.MoodEntry
import com.example.wellnessbuddy.data.MoodManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import java.text.SimpleDateFormat
import java.util.*

/**
 * Fragment for mood journaling with emoji selector and history
 */
class MoodJournalFragment : Fragment() {
    
    private lateinit var moodManager: MoodManager
    private lateinit var moodHistoryRecyclerView: RecyclerView
    private lateinit var addMoodBtn: MaterialButton
    private lateinit var moodTrendsBtn: MaterialButton
    private lateinit var todayMoodCard: MaterialCardView
    private lateinit var todayMoodEmoji: MaterialTextView
    private lateinit var todayMoodText: MaterialTextView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mood_journal, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        moodManager = MoodManager(requireContext())
        
        moodHistoryRecyclerView = view.findViewById(R.id.mood_history_recycler_view)
        addMoodBtn = view.findViewById(R.id.add_mood_btn)
        moodTrendsBtn = view.findViewById(R.id.mood_trends_btn)
        todayMoodCard = view.findViewById(R.id.today_mood_card)
        todayMoodEmoji = view.findViewById(R.id.today_mood_emoji)
        todayMoodText = view.findViewById(R.id.today_mood_text)
        
        setupViews()
        loadData()
    }
    
    private fun setupViews() {
        addMoodBtn.setOnClickListener {
            showMoodSelectorDialog()
        }
        
        moodTrendsBtn.setOnClickListener {
            // Navigate to mood trends chart
            showMoodTrendsDialog()
        }
        
        todayMoodCard.setOnClickListener {
            showMoodSelectorDialog()
        }
        
        moodHistoryRecyclerView.layoutManager = LinearLayoutManager(context)
    }
    
    private fun loadData() {
        // Load today's mood
        val todayMood = moodManager.getMoodsForDate(Date()).firstOrNull()
        updateTodayMoodCard(todayMood)
        
        // Load mood history
        val moodHistory = moodManager.getMoodsForLastDays(7)
        val moodHistoryAdapter = MoodHistoryAdapter(moodHistory) { mood ->
            showEditMoodDialog(mood)
        }
        moodHistoryRecyclerView.adapter = moodHistoryAdapter
    }
    
    private fun updateTodayMoodCard(mood: MoodEntry?) {
        if (mood != null) {
            todayMoodEmoji.text = mood.emoji
            todayMoodText.text = "${mood.moodName} (${mood.intensity}/10)"
        } else {
            todayMoodEmoji.text = "😊"
            todayMoodText.text = "How are you feeling today?"
        }
    }
    
    private fun showMoodSelectorDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_mood_selector, null)
        val notesEditText = dialogView.findViewById<EditText>(R.id.mood_notes_edit)
        val intensitySeekBar = dialogView.findViewById<SeekBar>(R.id.mood_intensity_seekbar)
        val intensityText = dialogView.findViewById<MaterialTextView>(R.id.mood_intensity_text)
        
        // Setup intensity seekbar
        intensitySeekBar.max = 9 // 1-10 scale
        intensitySeekBar.progress = 4 // Default to 5
        intensityText.text = "Intensity: 5"
        
        intensitySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                intensityText.text = "Intensity: ${progress + 1}"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        // Create mood option buttons
        val moodOptionsContainer = dialogView.findViewById<ViewGroup>(R.id.mood_options_container)
        var selectedMood: com.example.wellnessbuddy.data.MoodOption? = null
        
        com.example.wellnessbuddy.data.MoodEntry.MOOD_OPTIONS.forEach { moodOption ->
            val button = MaterialButton(requireContext()).apply {
                text = "${moodOption.emoji} ${moodOption.name}"
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setOnClickListener {
                    selectedMood = moodOption
                    intensitySeekBar.progress = moodOption.defaultIntensity - 1
                    intensityText.text = "Intensity: ${moodOption.defaultIntensity}"
                }
            }
            moodOptionsContainer.addView(button)
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("How are you feeling?")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                if (selectedMood != null) {
                    val moodOption = selectedMood!!
                    val mood = MoodEntry(
                        emoji = moodOption.emoji,
                        moodName = moodOption.name,
                        notes = notesEditText.text.toString().trim(),
                        intensity = intensitySeekBar.progress + 1,
                        date = Date()
                    )
                    moodManager.addMood(mood)
                    loadData()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showEditMoodDialog(mood: MoodEntry) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_mood_selector, null)
        val notesEditText = dialogView.findViewById<EditText>(R.id.mood_notes_edit)
        val intensitySeekBar = dialogView.findViewById<SeekBar>(R.id.mood_intensity_seekbar)
        val intensityText = dialogView.findViewById<MaterialTextView>(R.id.mood_intensity_text)
        
        // Pre-fill with existing values
        notesEditText.setText(mood.notes)
        intensitySeekBar.max = 9
        intensitySeekBar.progress = mood.intensity - 1
        intensityText.text = "Intensity: ${mood.intensity}"
        
        AlertDialog.Builder(requireContext())
            .setTitle("Edit Mood Entry")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val updatedMood = mood.copy(
                    notes = notesEditText.text.toString().trim(),
                    intensity = intensitySeekBar.progress + 1
                )
                moodManager.updateMood(updatedMood)
                loadData()
            }
            .setNegativeButton("Cancel", null)
            .setNeutralButton("Delete") { _, _ ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Mood Entry")
                    .setMessage("Are you sure you want to delete this mood entry?")
                    .setPositiveButton("Delete") { _, _ ->
                        moodManager.deleteMood(mood.id)
                        loadData()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            .show()
    }
    
    private fun showMoodTrendsDialog() {
        val moods = moodManager.getMoodsForLastDays(7)
        
        if (moods.isEmpty()) {
            AlertDialog.Builder(requireContext())
                .setTitle("Mood Trends")
                .setMessage("No mood entries found for the last 7 days.")
                .setPositiveButton("OK", null)
                .show()
            return
        }
        
        // Simple mood trends without chart for now
        val avgIntensity = moods.map { it.intensity }.average()
        val message = """
            Mood Trends (Last 7 Days)
            
            Average mood intensity: ${String.format("%.1f", avgIntensity)}/10
            Total entries: ${moods.size}
            
            Recent moods:
            ${moods.takeLast(5).joinToString("\n") { mood ->
                "${SimpleDateFormat("MMM dd", Locale.getDefault()).format(mood.date)}: ${mood.emoji} ${mood.moodName} (${mood.intensity}/10)"
            }}
        """.trimIndent()
        
        AlertDialog.Builder(requireContext())
            .setTitle("Mood Trends")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
    
    
    override fun onResume() {
        super.onResume()
        loadData()
    }
}
