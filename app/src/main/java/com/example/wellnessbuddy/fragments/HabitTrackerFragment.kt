package com.example.wellnessbuddy.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnessbuddy.R
import com.example.wellnessbuddy.data.Habit
import com.example.wellnessbuddy.data.HabitManager
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * Fragment for managing daily habits - CRUD operations
 */
class HabitTrackerFragment : Fragment() {
    
    private lateinit var habitManager: HabitManager
    private lateinit var habitsRecyclerView: RecyclerView
    private lateinit var addHabitFab: FloatingActionButton
    private lateinit var habitsAdapter: HabitsAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_habit_tracker, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        habitManager = HabitManager(requireContext())
        
        habitsRecyclerView = view.findViewById(R.id.habits_recycler_view)
        addHabitFab = view.findViewById(R.id.add_habit_fab)
        
        setupRecyclerView()
        setupFab()
        loadHabits()
    }
    
    private fun setupRecyclerView() {
        habitsRecyclerView.layoutManager = LinearLayoutManager(context)
        habitsAdapter = HabitsAdapter(emptyList()) { habit ->
            habitManager.completeHabit(habit.id)
            loadHabits() // Refresh the list
        }
        habitsRecyclerView.adapter = habitsAdapter
    }
    
    private fun setupFab() {
        addHabitFab.setOnClickListener {
            showAddHabitDialog()
        }
    }
    
    private fun loadHabits() {
        val habits = habitManager.loadHabits()
        habitsAdapter = HabitsAdapter(habits) { habit ->
            habitManager.completeHabit(habit.id)
            loadHabits() // Refresh the list
        }
        habitsRecyclerView.adapter = habitsAdapter
    }
    
    private fun showAddHabitDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_habit, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.habit_name_edit)
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.habit_description_edit)
        val targetCountEditText = dialogView.findViewById<EditText>(R.id.habit_target_count_edit)
        val iconEditText = dialogView.findViewById<EditText>(R.id.habit_icon_edit)
        
        AlertDialog.Builder(requireContext())
            .setTitle("Add New Habit")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = nameEditText.text.toString().trim()
                val description = descriptionEditText.text.toString().trim()
                val targetCount = targetCountEditText.text.toString().toIntOrNull() ?: 1
                val icon = iconEditText.text.toString().trim()
                
                if (name.isNotEmpty()) {
                    val habit = Habit(
                        name = name,
                        description = description,
                        targetCount = targetCount,
                        icon = icon.ifEmpty { "📝" }
                    )
                    habitManager.addHabit(habit)
                    loadHabits()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showEditHabitDialog(habit: Habit) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_habit, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.habit_name_edit)
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.habit_description_edit)
        val targetCountEditText = dialogView.findViewById<EditText>(R.id.habit_target_count_edit)
        val iconEditText = dialogView.findViewById<EditText>(R.id.habit_icon_edit)
        
        // Pre-fill with existing values
        nameEditText.setText(habit.name)
        descriptionEditText.setText(habit.description)
        targetCountEditText.setText(habit.targetCount.toString())
        iconEditText.setText(habit.icon)
        
        AlertDialog.Builder(requireContext())
            .setTitle("Edit Habit")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = nameEditText.text.toString().trim()
                val description = descriptionEditText.text.toString().trim()
                val targetCount = targetCountEditText.text.toString().toIntOrNull() ?: 1
                val icon = iconEditText.text.toString().trim()
                
                if (name.isNotEmpty()) {
                    val updatedHabit = habit.copy(
                        name = name,
                        description = description,
                        targetCount = targetCount,
                        icon = icon.ifEmpty { "📝" }
                    )
                    habitManager.updateHabit(updatedHabit)
                    loadHabits()
                }
            }
            .setNegativeButton("Cancel", null)
            .setNeutralButton("Delete") { _, _ ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Habit")
                    .setMessage("Are you sure you want to delete this habit?")
                    .setPositiveButton("Delete") { _, _ ->
                        habitManager.deleteHabit(habit.id)
                        loadHabits()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            .show()
    }
    
    override fun onResume() {
        super.onResume()
        loadHabits() // Refresh when returning to this fragment
    }
}
