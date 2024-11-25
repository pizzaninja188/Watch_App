package com.ece454.watchapp

import android.os.Bundle
import android.graphics.Color
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class HeartRateHistoryActivity : AppCompatActivity() {

    private lateinit var heartRateHistoryChart: LineChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_heart_rate_history)

        heartRateHistoryChart = findViewById(R.id.heartRateHistoryChart)

        // Simulate past week's data
        val heartRateData = getHeartRateDataForPastWeek()

        // Display the data in the chart
        displayHeartRateHistory(heartRateData)

        // Back button logic
        findViewById<Button>(R.id.backButton).setOnClickListener {
            finish() // Go back to the previous activity
        }
    }

    private fun getHeartRateDataForPastWeek(): List<Pair<String, Float>> {
        // Simulate data (replace with actual database or API data)
        return listOf(
            "Monday" to 72f,
            "Tuesday" to 75f,
            "Wednesday" to 80f,
            "Thursday" to 76f,
            "Friday" to 78f,
            "Saturday" to 70f,
            "Sunday" to 74f
        )
    }

    private fun displayHeartRateHistory(data: List<Pair<String, Float>>) {
        val entries = data.mapIndexed { index, pair ->
            Entry(index.toFloat(), pair.second) // Create chart entries
        }

        val dataSet = LineDataSet(entries, "Heart Rate (BPM)").apply {
            color = Color.LTGRAY // Customize line color
            lineWidth = 2f
            setDrawCircles(true)
            setDrawValues(false)
        }

        val lineData = LineData(dataSet)
        heartRateHistoryChart.data = lineData
        heartRateHistoryChart.invalidate()

        // Customize the X-axis
        val xAxis = heartRateHistoryChart.xAxis
        xAxis.granularity = 1f
        xAxis.valueFormatter = IndexAxisValueFormatter(data.map { it.first }) // Days of the week
    }
}