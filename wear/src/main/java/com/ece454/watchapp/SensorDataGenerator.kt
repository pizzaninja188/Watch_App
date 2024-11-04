package com.ece454.watchapp

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.Flow
import android.content.Context
import kotlinx.coroutines.currentCoroutineContext
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

class SensorDataGenerator(context: Context) {

    private var totalSteps = 0
    private val dataSender = WearableDataSender(context)

    fun generateSensorData(): Flow<SensorData> = flow {
        try {
            while (currentCoroutineContext().isActive) {
                val data = SensorData(
                    heartRate = Random.nextInt(from = 60, until = 120),
                    steps = totalSteps.also { totalSteps += Random.nextInt(from = 0, until = 2) },
                    temperature = 36.6f + Random.nextFloat() * 2.0f
                )

                emit(data)  // Emit for local use
                dataSender.sendToMobile(data)  // Send to mobile

                delay(timeMillis = 1000)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}