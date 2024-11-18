package com.ece454.watchapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.wearable.*
import java.text.SimpleDateFormat
import java.util.*
import org.json.JSONObject

import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.ColorTemplate


class MainActivity : AppCompatActivity(), DataClient.OnDataChangedListener {

    private lateinit var sensorDataText: TextView // TextView to display sensor data
    private lateinit var lastUpdateText: TextView // TextView to display last update time

    private val dataClient by lazy { Wearable.getDataClient(this) }// DataClient to receive sensor data
    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())// Date format for timestamp

    private lateinit var heartRateChart: LineChart
    private val heartRateEntries = mutableListOf<Entry>()
    private var timeIndex = 0f
    private val maxEntries = 50

    companion object {
        private const val TAG = "MobileActivity"
        private const val SENSOR_DATA_PATH = "/sensor_data"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorDataText = findViewById(R.id.sensorDataText)
        lastUpdateText = findViewById(R.id.lastUpdateText)

        heartRateChart = findViewById(R.id.heartRateChart)

        setupChart()

        val personalActivityButton = findViewById<Button>(R.id.PersonalInfoButton)
        personalActivityButton.setOnClickListener {
            val intent = Intent(this@MainActivity, PersonalInfoActivity::class.java)
            startActivity(intent)
        }
    }



    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED) {
                val uri = event.dataItem.uri
                if (uri.path == SENSOR_DATA_PATH) {
                    try {
                        // Get the raw data bytes
                        val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                        val rawData = dataMap.getByteArray("sensor_data")

                        // Parse the JSON data
                        val jsonData = JSONObject(String(rawData ?: ByteArray(0)))
                        val sensorData = SensorData(
                            heartRate = jsonData.getInt("heartRate"),
                            steps = jsonData.getInt("steps"),
                            temperature = jsonData.getDouble("temperature").toFloat(),
                            timestamp = jsonData.getLong("timestamp")
                        )


                        updateHeartRateData(sensorData.heartRate.toFloat())


                        // Update the UI
                        updateSensorDisplay(sensorData)

                        Log.d(TAG, "Received sensor data: $sensorData")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing sensor data", e)
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateSensorDisplay(data: SensorData) {
        runOnUiThread {
            sensorDataText.text = """
                Heart Rate: ${data.heartRate} BPM
                Steps: ${data.steps}
                Temperature: %.1f°C
            """.trimIndent().format(data.temperature)

            lastUpdateText.text = "Last Updated: ${dateFormat.format(Date(data.timestamp))}"
        }
    }

    override fun onResume() {
        super.onResume()
        dataClient.addListener(this)
    }

    override fun onPause() {
        super.onPause()
        dataClient.removeListener(this)
    }

    private fun setupChart() {
        // Configure the chart
        heartRateChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            setPinchZoom(true)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            axisRight.isEnabled = false
            data = LineData() // Initialize with empty data to avoid null issues
        }
    }

    private fun updateHeartRateData(newHeartRate: Float) {
        // Check for valid heart rate data
        if (newHeartRate <= 0) return

        // Add a new entry for the heart rate
        heartRateEntries.add(Entry(timeIndex, newHeartRate))
        timeIndex += 1

        // Ensure that heartRateEntries do not exceed maxEntries for smooth performance
        if (heartRateEntries.size > maxEntries) {
            heartRateEntries.removeAt(0)  // Remove the oldest entry
            for (i in heartRateEntries.indices) {
                heartRateEntries[i].x = i.toFloat() // Reindex entries for smooth scrolling
            }
        }

        // Update dataset
        val dataSet = LineDataSet(heartRateEntries, "Heart Rate").apply {
            color = ColorTemplate.getHoloBlue()
            setDrawValues(false)
            lineWidth = 2f
            setDrawCircles(false)
            setDrawFilled(true)
        }

        // Set or update chart data
        heartRateChart.data = LineData(dataSet)
        heartRateChart.data.notifyDataChanged()
        heartRateChart.notifyDataSetChanged()
        heartRateChart.invalidate()

        // Set visible range and move view for real-time effect
        heartRateChart.setVisibleXRangeMaximum(maxEntries.toFloat())
        heartRateChart.moveViewToX(timeIndex)
    }
}

