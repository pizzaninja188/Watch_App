package com.ece454.watchapp

data class SensorData(
    val heartRate: Int,
    val steps: Int,
    val temperature: Float,
    val timestamp: Long = System.currentTimeMillis()
)
