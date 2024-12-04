package com.ece454.watchapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class TargetHeartRateActivity : AppCompatActivity() {

    private lateinit var chatbotHelper: ChatbotHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        chatbotHelper = ChatbotHelper(this)

        // Retrieve the selected activity passed from MainActivity
        val selectedActivity = intent.getStringExtra("selectedActivity") ?: "Resting"

        // Fetch the target heart rate using the chatbot
        fetchTargetHeartRate(selectedActivity)
    }

    private fun fetchTargetHeartRate(activity: String) {
        val prompt = "What is my target heart rate when $activity? Respond with only a range of numbers and no characters"

        lifecycleScope.launch {
            val response = chatbotHelper.getChatbotResponse(prompt)
            val targetHeartRate = parseHeartRateResponse(response)
            sendResultToMainActivity(targetHeartRate)
        }
    }

    private fun parseHeartRateResponse(response: String): String {
        // Extract a numeric range like "60-100" from the response
        return response.trim().takeIf { it.matches(Regex("\\d+-\\d+")) } ?: "Unknown"
    }

    private fun sendResultToMainActivity(targetHeartRate: String) {
        val resultIntent = Intent().apply {
            putExtra("targetHeartRate", targetHeartRate)
        }
        setResult(RESULT_OK, resultIntent)
        finish() // Close this activity
    }
}



