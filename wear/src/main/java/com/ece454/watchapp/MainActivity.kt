package com.ece454.watchapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancelAndJoin
import android.widget.TextView
import androidx.wear.widget.BoxInsetLayout
import android.view.View
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorGenerator: SensorDataGenerator
    private var dataCollectionJob: Job? = null

    // UI elements (optional - for debugging/visualization)
    private lateinit var statusText: TextView
    private lateinit var dataText: TextView
    private lateinit var toggleButton: Button
    private lateinit var mSensorManager: SensorManager
    private lateinit var mHeartRateSensor: Sensor
    private var heartRate = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // get permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BODY_SENSORS), 1)
        }

        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)!!

        // Initialize the sensor generator
        sensorGenerator = SensorDataGenerator(this)

        // Set up the UI
        setContentView(R.layout.activity_main)
        setupUI()

        // Start data collection automatically
        startDataCollection()
    }

    private fun setupUI() {
        statusText = findViewById(R.id.statusText)
        dataText = findViewById(R.id.dataText)
        toggleButton = findViewById(R.id.toggleButton)

        toggleButton.setOnClickListener {
            if (dataCollectionJob?.isActive == true) {
                stopDataCollection()
                toggleButton.text = "Start Collection"
                statusText.text = "Status: Stopped"
            } else {
                startDataCollection()
                toggleButton.text = "Stop Collection"
                statusText.text = "Status: Running"
            }
        }
    }

    private fun startDataCollection() {
        dataCollectionJob = lifecycleScope.launch {
            try {
                sensorGenerator.generateSensorData()
                    .collect { data ->
                        // Update UI with the latest data (optional)
                        updateDataDisplay(data)
                    }
            } catch (e: Exception) {
                // Handle any errors
                statusText.text = "Status: Error - ${e.message}"
                e.printStackTrace()
            }
        }
    }

    private fun updateDataDisplay(data: SensorData) {
        dataText.text = """
            Heart Rate: ${data.heartRate} bpm
            Steps: ${data.steps}
            Temperature: ${String.format("%.1f", data.temperature)}°C
        """.trimIndent()
    }

    private fun stopDataCollection() {
        lifecycleScope.launch {
            dataCollectionJob?.cancelAndJoin()
            dataCollectionJob = null
        }
    }

    override fun onResume() {
        super.onResume()
        mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_FASTEST)
        if (dataCollectionJob?.isActive != true) {
            startDataCollection()
        }
    }

    override fun onPause() {
        super.onPause()
        mSensorManager.unregisterListener(this)
        stopDataCollection()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopDataCollection()
    }

    override fun onSensorChanged(event: SensorEvent) {
        heartRate = event.values[0].toInt()
        sensorGenerator.updateHeartRate(heartRate)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }
}