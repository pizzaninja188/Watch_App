package com.ece454.watchapp

import android.content.Context
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatbotHelper(context: Context) {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = "AIzaSyAKAfd_CcsMUkPkz9aEoKakGcW2Yr32I20"
    )

    private val sharedPref = context.getSharedPreferences("PersonalInfo", Context.MODE_PRIVATE)

    suspend fun getChatbotResponse(message: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val gender = if (sharedPref.getBoolean("Gender", false)) "male" else "female"
                val age = sharedPref.getInt("Age", 0)
                val height = sharedPref.getInt("Height", 0)
                val weight = sharedPref.getInt("Weight", 0)
                val prompt = "$message. I am a $height\" $age year old $gender that weighs $weight pounds"
                val response = generativeModel.generateContent(prompt)
                response.text ?: "I'm sorry, I couldn't process that."
            } catch (e: Exception) {
                "Error: Unable to fetch response."
            }
        }
    }
}
