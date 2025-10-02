package com.example.wellnessbuddy.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnessbuddy.R
import com.example.wellnessbuddy.data.Habit
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textview.MaterialTextView

/**
 * Adapter for displaying habits in RecyclerView
 */
class HabitsAdapter(
    private val habits: List<Habit>,
    private val onCompleteClick: (Habit) -> Unit,
    private val onEditClick: (Habit) -> Unit,
    private val onDeleteClick: (Habit) -> Unit
) : RecyclerView.Adapter<HabitsAdapter.HabitViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        holder.bind(habits[position])
    }

    override fun getItemCount(): Int = habits.size

    inner class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val habitIcon: MaterialTextView = itemView.findViewById(R.id.habit_icon)
        private val habitName: MaterialTextView = itemView.findViewById(R.id.habit_name)
        private val habitProgress: MaterialTextView = itemView.findViewById(R.id.habit_progress)
        private val habitProgressBar: LinearProgressIndicator = itemView.findViewById(R.id.habit_progress_bar)
        private val completeBtn: MaterialButton = itemView.findViewById(R.id.complete_btn)

        fun bind(habit: Habit) {
            habitIcon.text = habit.icon.ifEmpty { "📝" }
            habitName.text = habit.name
            habitProgress.text = "${habit.completedCount}/${habit.targetCount} completed"
            
            // Update progress bar
            val progress = habit.getCompletionPercentage()
            habitProgressBar.progress = (progress * 100).toInt()
            
            // Update complete button state
            if (habit.isFullyCompleted()) {
                completeBtn.text = "✓ Done"
                completeBtn.isEnabled = false
                completeBtn.setBackgroundColor(itemView.context.getColor(R.color.neon_success))
            } else {
                completeBtn.text = "Complete"
                completeBtn.isEnabled = true
                completeBtn.setBackgroundColor(itemView.context.getColor(R.color.neon_primary))
                completeBtn.setOnClickListener {
                    onCompleteClick(habit)
                }
            }
            
            // Add long click for edit/delete options
            itemView.setOnLongClickListener {
                showHabitOptions(habit)
                true
            }
        }
        
        private fun showHabitOptions(habit: Habit) {
            val options = arrayOf("Edit", "Delete")
            androidx.appcompat.app.AlertDialog.Builder(itemView.context)
                .setTitle("Habit Options")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> onEditClick(habit)
                        1 -> onDeleteClick(habit)
                    }
                }
                .show()
        }
    }
}
