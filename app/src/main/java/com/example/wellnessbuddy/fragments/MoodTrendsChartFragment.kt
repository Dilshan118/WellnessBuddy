package com.example.wellnessbuddy.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.wellnessbuddy.R
import com.example.wellnessbuddy.data.MoodManager
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Fragment for displaying mood trends chart
 */
class MoodTrendsChartFragment : Fragment() {
    
    private lateinit var moodManager: MoodManager
    private lateinit var moodChart: LineChart
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mood_trends_chart, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        moodManager = MoodManager(requireContext())
        moodChart = view.findViewById(R.id.mood_chart)
        
        setupChart()
        loadMoodData()
    }
    
    private fun setupChart() {
        // Configure chart appearance
        moodChart.description.isEnabled = false
        moodChart.setTouchEnabled(true)
        moodChart.isDragEnabled = true
        moodChart.setScaleEnabled(true)
        moodChart.setPinchZoom(true)
        moodChart.setBackgroundColor(Color.TRANSPARENT)
        
        // Configure X-axis
        val xAxis = moodChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)
        xAxis.textColor = Color.WHITE
        xAxis.textSize = 12f
        
        // Configure Y-axis
        val leftAxis = moodChart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.gridColor = Color.parseColor("#333333")
        leftAxis.textColor = Color.WHITE
        leftAxis.textSize = 12f
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 10f
        
        val rightAxis = moodChart.axisRight
        rightAxis.isEnabled = false
        
        // Configure legend
        val legend = moodChart.legend
        legend.textColor = Color.WHITE
        legend.textSize = 14f
        legend.isEnabled = true
    }
    
    private fun loadMoodData() {
        val moods = moodManager.getMoodsForLastDays(7)
        
        if (moods.isEmpty()) {
            // Show empty state
            return
        }
        
        // Group moods by date and calculate average intensity
        val moodData = mutableMapOf<String, MutableList<Int>>()
        val dateFormatter = SimpleDateFormat("MMM dd", Locale.getDefault())
        
        moods.forEach { mood ->
            val dateKey = dateFormatter.format(mood.date)
            moodData.getOrPut(dateKey) { mutableListOf() }.add(mood.intensity)
        }
        
        // Create chart entries
        val entries = mutableListOf<Entry>()
        val labels = mutableListOf<String>()
        
        moodData.keys.sorted().forEachIndexed { index, date ->
            val intensities = moodData[date]!!
            val averageIntensity = intensities.average().toFloat()
            entries.add(Entry(index.toFloat(), averageIntensity))
            labels.add(date)
        }
        
        // Create dataset
        val dataSet = LineDataSet(entries, "Mood Intensity").apply {
            color = Color.parseColor("#00D4FF") // Neon cyan
            setCircleColor(Color.parseColor("#00D4FF"))
            lineWidth = 3f
            circleRadius = 6f
            setDrawCircleHole(true)
            circleHoleColor = Color.parseColor("#00D4FF")
            setDrawValues(true)
            valueTextColor = Color.WHITE
            valueTextSize = 12f
            setDrawFilled(true)
            fillColor = Color.parseColor("#1A00D4FF")
            fillAlpha = 100
        }
        
        // Set up chart data
        val lineData = LineData(dataSet)
        moodChart.data = lineData
        
        // Set X-axis labels
        val xAxis = moodChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.labelCount = labels.size
        xAxis.setAvoidFirstLastClipping(true)
        
        // Refresh chart
        moodChart.invalidate()
    }
}
