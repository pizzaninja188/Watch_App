package com.ece454.watchapp  // Use your package name

data class SensorData(
    val heartRate: Int,
    val steps: Int,
    val temperature: Float,
    val timestamp: Long = System.currentTimeMillis()
)