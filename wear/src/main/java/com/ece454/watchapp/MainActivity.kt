package com.ece454.watchapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancelAndJoin
import android.widget.TextView
import android.view.View
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.health.services.client.HealthServices
import androidx.health.services.client.MeasureCallback
import androidx.health.services.client.MeasureClient
import androidx.health.services.client.PassiveListenerService
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DataTypeAvailability
import androidx.health.services.client.data.DeltaDataType
import androidx.health.services.client.data.PassiveListenerConfig
import androidx.health.services.client.data.SampleDataPoint
import androidx.health.services.client.getCapabilities
import androidx.health.services.client.unregisterMeasureCallback
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {

    private lateinit var sensorGenerator: SensorDataGenerator
    private var dataCollectionJob: Job? = null

    // UI elements (optional - for debugging/visualization)
    private lateinit var statusText: TextView
    private lateinit var dataText: TextView
    private lateinit var toggleButton: Button

    private lateinit var heartRateCallback: MeasureCallback
    private lateinit var measureClient: MeasureClient
    private var heartRate = 0

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissionsToAsk = arrayListOf<String>()
        // ask for permissions
        if (
            ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) !=
            PackageManager.PERMISSION_GRANTED
        ) permissionsToAsk.add(Manifest.permission.BODY_SENSORS)
        else {/* start heart rate listening */}

        if (
            ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS_BACKGROUND) !=
            PackageManager.PERMISSION_GRANTED
        ) permissionsToAsk.add(Manifest.permission.BODY_SENSORS_BACKGROUND)
        else {/* start background sensor reading */}

        if (
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) !=
            PackageManager.PERMISSION_GRANTED
        ) permissionsToAsk.add(Manifest.permission.ACTIVITY_RECOGNITION)
        else {/* start activity detection */}

        if (permissionsToAsk.isNotEmpty()) ActivityCompat.requestPermissions(this, permissionsToAsk.toTypedArray(), 1)

        val healthClient = HealthServices.getClient(this /*context*/)
        measureClient = healthClient.measureClient
        val passiveMonitoringClient = healthClient.passiveMonitoringClient
        var supportsHeartRate: Boolean
        lifecycleScope.launch {
            val capabilities = measureClient.getCapabilities()
            // Supported types for passive data collection
            supportsHeartRate =
                DataType.HEART_RATE_BPM in capabilities.supportedDataTypesMeasure
        }

        heartRateCallback = object : MeasureCallback {
            override fun onAvailabilityChanged(dataType: DeltaDataType<*, *>, availability: Availability) {
                if (availability is DataTypeAvailability) {
                    // Handle availability change.
                }
            }

            override fun onDataReceived(data: DataPointContainer) {
                // Inspect data points.
                heartRate = data.getData(DataType.HEART_RATE_BPM)[0].value.toInt()
            }
        }

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
                        data.heartRate = heartRate;
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
        if (dataCollectionJob?.isActive != true) {
            startDataCollection()
        }

        // Register the callback
        measureClient.registerMeasureCallback(DataType.Companion.HEART_RATE_BPM, heartRateCallback)
    }

    override fun onPause() {
        super.onPause()
        stopDataCollection()

        // Unregister the callback.
        lifecycleScope.launch {
            measureClient.unregisterMeasureCallback(DataType.Companion.HEART_RATE_BPM, heartRateCallback)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopDataCollection()
    }
}