package com.ece454.watchapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.wearable.*
import java.text.SimpleDateFormat
import java.util.*
import org.json.JSONObject

class MainActivity : AppCompatActivity(), DataClient.OnDataChangedListener {

    private lateinit var sensorDataText: TextView // TextView to display sensor data
    private lateinit var lastUpdateText: TextView // TextView to display last update time

    private val dataClient by lazy { Wearable.getDataClient(this) }// DataClient to receive sensor data
    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())// Date format for timestamp

    companion object {
        private const val TAG = "MobileActivity"
        private const val SENSOR_DATA_PATH = "/sensor_data"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorDataText = findViewById(R.id.sensorDataText)
        lastUpdateText = findViewById(R.id.lastUpdateText)
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
}