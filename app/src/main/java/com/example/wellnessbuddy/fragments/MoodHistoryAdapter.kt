package com.example.wellnessbuddy.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnessbuddy.R
import com.example.wellnessbuddy.data.MoodEntry
import com.google.android.material.textview.MaterialTextView
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter for displaying mood history in RecyclerView
 */
class MoodHistoryAdapter(
    private val moods: List<MoodEntry>,
    private val onMoodClick: (MoodEntry) -> Unit
) : RecyclerView.Adapter<MoodHistoryAdapter.MoodViewHolder>() {

    private val dateFormat = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood_history, parent, false)
        return MoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        holder.bind(moods[position])
    }

    override fun getItemCount(): Int = moods.size

    inner class MoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val moodEmoji: MaterialTextView = itemView.findViewById(R.id.mood_emoji)
        private val moodName: MaterialTextView = itemView.findViewById(R.id.mood_name)
        private val moodDate: MaterialTextView = itemView.findViewById(R.id.mood_date)
        private val moodNotes: MaterialTextView = itemView.findViewById(R.id.mood_notes)
        private val moodIntensity: MaterialTextView = itemView.findViewById(R.id.mood_intensity)

        fun bind(mood: MoodEntry) {
            moodEmoji.text = mood.emoji
            moodName.text = mood.moodName
            moodDate.text = dateFormat.format(mood.date)
            moodIntensity.text = "${mood.intensity}/10"
            
            if (mood.notes.isNotEmpty()) {
                moodNotes.text = mood.notes
                moodNotes.visibility = View.VISIBLE
            } else {
                moodNotes.visibility = View.GONE
            }
            
            itemView.setOnClickListener {
                onMoodClick(mood)
            }
        }
    }
}
