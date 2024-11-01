package com.ece454.watchapp

import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import android.content.Context

class WearableDataSender(private val context: Context) {

    private val dataClient: DataClient = Wearable.getDataClient(context)

    // Convert com.ece454.watchapp.tile.SensorData to bytes for sending
    private fun serializeSensorData(data: SensorData): ByteArray {
        return """
            {
                "heartRate": ${data.heartRate},
                "steps": ${data.steps},
                "temperature": ${data.temperature},
                "timestamp": ${data.timestamp}
            }
        """.trimIndent().toByteArray()
    }

    // Send data to mobile
    suspend fun sendToMobile(data: SensorData) {
        try {
            val request = PutDataMapRequest.create("/sensor_data").apply {
                dataMap.putByteArray("sensor_data", serializeSensorData(data))
                dataMap.putLong("timestamp", System.currentTimeMillis())
            }.asPutDataRequest()
                .setUrgent()

            val result = dataClient.putDataItem(request).await()
            println("Data sent successfully: ${result.uri}")
        } catch (e: Exception) {
            println("Error sending data: ${e.message}")
            e.printStackTrace()
        }
    }

}