package com.ece454.watchapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.ai.client.generativeai.GenerativeModel
import com.google.android.gms.wearable.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity(), DataClient.OnDataChangedListener {

    private lateinit var sensorDataText: TextView // TextView to display sensor data
    private lateinit var lastUpdateText: TextView // TextView to display last update time

    private val dataClient by lazy { Wearable.getDataClient(this) }// DataClient to receive sensor data
    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())// Date format for timestamp

    private lateinit var heartRateChart: LineChart
    private val heartRateEntries = mutableListOf<Entry>()
    private var timeIndex = 0f
    private val maxEntries = 50

    private lateinit var prompt: String
    private lateinit var replacedPrompt: String

    private val generativeModel = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = "AIzaSyAKAfd_CcsMUkPkz9aEoKakGcW2Yr32I20")


    companion object {
        private const val TAG = "MobileActivity"
        private const val SENSOR_DATA_PATH = "/sensor_data"
    }

    private lateinit var peakTextView: TextView
    private lateinit var lowTextView: TextView

    private var peakHeartRate: Float? = null
    private var lowHeartRate: Float? = null

    private lateinit var heartRateDisplay: TextView

    private val handler = Handler(Looper.getMainLooper())
     private val updateGraphRunnable = object : Runnable {
      override fun run() {
        val randomHeartRate = (100..200).random() // Generate a random integer between 100 and 200 // Generate random number [100-200]
       //updateHeartRateData(randomHeartRate.toFloat())
      handler.postDelayed(this, 800) // Schedule next update after 1 second
     }
     }

    private lateinit var targetHeartRateTextView: TextView
    private var selectedActivity: String = "Resting"
    private lateinit var currentActivityTextView: TextView

    // Register for Activity Result
    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val targetHeartRate = result.data?.getStringExtra("targetHeartRate") ?: "Unknown"
            updateTargetHeartRate(targetHeartRate)
        }
    }

    private lateinit var startWorkoutButton: Button
    private lateinit var timerTextView: TextView
    private var workoutStarted = false
    private var timerStartMs = 0L
    private lateinit var timerJob: Job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //sensorDataText = findViewById(R.id.sensorDataText)
        //lastUpdateText = findViewById(R.id.lastUpdateText)

        heartRateChart = findViewById(R.id.heartRateChart)
        heartRateDisplay = findViewById(R.id.heartRateDisplay)
        peakTextView = findViewById(R.id.peakTextView)
        lowTextView = findViewById(R.id.lowTextView)

        setupChart()

        val historyButton: Button = findViewById(R.id.historyButton)
        historyButton.setOnClickListener {
            // Navigate to the Heart Rate History page
            val intent = Intent(this, HeartRateHistoryActivity::class.java)
            startActivity(intent)
        }

        currentActivityTextView = findViewById(R.id.currentActivityTextView)
        targetHeartRateTextView = findViewById(R.id.targetHeartRateTextView)


        val setActivityButton: Button = findViewById(R.id.setActivityButton)
        setActivityButton.setOnClickListener {
            showActivityDialog()
        }

        updateCurrentActivity(selectedActivity)
        openTargetHeartRateActivity(selectedActivity)


        val button = findViewById<Button>(R.id.generateButton)
        val textView = findViewById<TextView>(R.id.responseText)

        button.setOnClickListener {
            // Show loading text with dots animation
            textView.text = "Analyzing..."

            // Optional: Make text center aligned while loading
            textView.gravity = Gravity.CENTER

            lifecycleScope.launch {
                try {
                    val response = generativeModel.generateContent(replacedPrompt)
                    // Reset text alignment to default
                    textView.gravity = Gravity.START
                    textView.text = response.text
                } catch (e: Exception) {
                    // Reset text alignment to default
                    textView.gravity = Gravity.START
                    textView.text = "Error: ${e.message}"
                }
            }
        }

        // Navigate to the Chat page
        val chatButton: Button = findViewById(R.id.chatButton)
        chatButton.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }

        // workout tracking
        startWorkoutButton = findViewById<Button>(R.id.startWorkoutButton)
        timerTextView = findViewById<TextView>(R.id.timerTextView)
        startWorkoutButton.setOnClickListener {
            if (workoutStarted) {
                startWorkoutButton.text = "Start Workout"
                timerJob.cancel()
                workoutStarted = false
            } else {
                startWorkoutButton.text = "Stop Workout"
                timerStartMs = System.currentTimeMillis()
                workoutStarted = true
                timerJob = lifecycleScope.launch {
                    while (workoutStarted) timerLoop()
                }
            }
        }
    }

    suspend fun timerLoop() {
        delay(1)
        val timerMs = System.currentTimeMillis() - timerStartMs
        val hours = timerMs / 1000 / 60 / 60
        val minutes = timerMs / 1000 / 60 % 60
        val seconds = timerMs / 1000 % 60
        val ms = timerMs % 1000
        timerTextView.text = String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, ms)
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
        val sharedPref = getSharedPreferences("PersonalInfo", MODE_PRIVATE)

        val isMale = sharedPref.getBoolean("Gender", false)
        val gender = if (isMale) "man" else "woman"
        // Get the data from shared preferences
        val age = sharedPref.getInt("Age", 0)
        val weight = sharedPref.getInt("Weight", 0)
        val height = sharedPref.getInt("Height", 0)

        // Now you can use these values in your prompt
        prompt = "Give me the suggestions for a person's heart rate range, whose age is data_age years old, weight is " +
                "data_weight and height is data_height."
        replacedPrompt = prompt
            .replace("person", gender)
            .replace("data_age", age.toString())
            .replace("data_weight", weight.toString())
            .replace("data_height", height.toString())
        dataClient.addListener(this)
        //handler.post(updateGraphRunnable)
    }

    override fun onPause() {
        super.onPause()
        dataClient.removeListener(this)
        //handler.removeCallbacks(updateGraphRunnable)
    }

    private fun setupChart() {
        heartRateChart.apply {
            // Chart background and interactions
            setBackgroundColor(Color.BLACK) // Set chart background color (optional)
            description.isEnabled = false // Remove the description label
            setTouchEnabled(true)
            setPinchZoom(true)

            // Configure X-Axis
            xAxis.apply {
                isEnabled = false // Completely remove the X-axis, including labels and grid lines
            }



            // Configure Y-Axis (Left)
            axisLeft.apply {
                gridColor = Color.LTGRAY // Subtle dark grid lines
                textColor = Color.LTGRAY // Light gray text for futuristic styling
                setDrawAxisLine(false)
                setDrawGridLines(true) // Keep or remove grid lines (optional)
                granularity = 10f // Interval between grid lines
                axisMinimum = 0f // Minimum value on the Y-axis
                axisMaximum = 250f // Maximum value on the Y-axis
                setDrawLabels(true) // Ensure labels are shown for the Y-axis
            }

            // Disable Right Y-Axis (Optional)
            axisRight.isEnabled = false

            // Remove the legend (Heart Rate label box)
            legend.isEnabled = false

            setTouchEnabled(true) // Enable touch gestures
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)


            // Initialize chart data
            data = LineData()
        }



    }

    private fun updateHeartRateData(newHeartRate: Float) {
        // Check for valid heart rate data
        if (newHeartRate <= 0) return

        // Initialize Peak and Low values on the first reading
        if (peakHeartRate == null || lowHeartRate == null) {
            peakHeartRate = newHeartRate
            lowHeartRate = newHeartRate
        }

        // Update Peak and Low values
        if (newHeartRate > peakHeartRate!!) {
            peakHeartRate = newHeartRate
            peakTextView.text = "Peak: ${peakHeartRate!!.toInt()}"
        }

        if (newHeartRate < lowHeartRate!!) {
            lowHeartRate = newHeartRate
            lowTextView.text = "Low: ${lowHeartRate!!.toInt()}"
        }

        // Parse the target heart rate range
        val targetRange = parseTargetHeartRateRange()
        val (targetLow, targetHigh) = targetRange


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

        // Update the text display above the graph
        heartRateDisplay.text = "Heart Rate: ${newHeartRate.toInt()}"
        if (newHeartRate in targetLow..targetHigh) {
            heartRateDisplay.setTextColor(Color.parseColor("#39FF14")) // Neon green
        } else {
            heartRateDisplay.setTextColor(Color.parseColor("#FF073A")) // Neon red
        }

        // Determine the line color and gradient based on the heart rate value
        val lineColor: Int
        val gradientDrawable: Int

        if (newHeartRate in targetLow..targetHigh) {
            lineColor = Color.parseColor("#39FF14")
            gradientDrawable = R.drawable.gradient_fill_green
        } else {
            lineColor = Color.parseColor("#FF073A") // Neon red
            gradientDrawable = R.drawable.gradient_fill_red // Red gradient
        }



        val dataSet = LineDataSet(heartRateEntries, "Heart Rate").apply {
            color = lineColor
            setDrawCircles(false)
            setDrawValues(false)
            setDrawFilled(true)
            fillDrawable = ContextCompat.getDrawable(this@MainActivity, gradientDrawable) // Set gradient
            lineWidth = 2f // Set line thickness for visibility
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

    private fun parseTargetHeartRateRange(): Pair<Float, Float> {
        // Get the text from targetHeartRateTextView
        val targetHeartRateText = targetHeartRateTextView.text.toString()

        // Extract the range (e.g., "Target Heart Rate: 100-175")
        val rangeRegex = Regex("(\\d+)-(\\d+)")
        val match = rangeRegex.find(targetHeartRateText)

        return if (match != null) {
            val (low, high) = match.destructured
            Pair(low.toFloat(), high.toFloat())
        } else {
            // Default range if parsing fails
            Pair(100f, 175f)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.edit -> {
                val intent = Intent(this@MainActivity, PersonalInfoActivity::class.java)
                intent.putExtra("FirstTime", false)
                startActivity(intent)
                true
            }

            R.id.delete -> {
                val sharedPref = getSharedPreferences("PersonalInfo", MODE_PRIVATE)
                val editor = sharedPref.edit()
                editor.remove("Gender")
                editor.remove("Age")
                editor.remove("Weight")
                editor.remove("Height")
                editor.apply()
                val intent = Intent(this@MainActivity, PersonalInfoActivity::class.java)
                intent.putExtra("FirstTime", false)
                startActivity(intent)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showActivityDialog() {
        val activities = arrayOf("Running", "Swimming", "Sleeping", "Resting")

        AlertDialog.Builder(this)
            .setTitle("Choose Activity")
            .setItems(activities) { _, which ->
                selectedActivity = activities[which]
                updateCurrentActivity(selectedActivity)
                openTargetHeartRateActivity(selectedActivity)
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    private fun updateCurrentActivity(activity: String) {
        currentActivityTextView.text = "Current Activity: $activity"
    }

    private fun openTargetHeartRateActivity(activity: String) {
        val intent = Intent(this, TargetHeartRateActivity::class.java).apply {
            putExtra("selectedActivity", activity)
        }
        activityResultLauncher.launch(intent)
    }


    private fun updateTargetHeartRate(targetHeartRate: String) {
        targetHeartRateTextView.text = "Target Heart Rate: $targetHeartRate"
    }
}






