package com.ece454.watchapp

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var loadingIndicator: View
    private val chatMessages = mutableListOf<ChatMessage>()
    private lateinit var chatAdapter: ChatAdapter

    private val generativeModel = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = "AIzaSyAKAfd_CcsMUkPkz9aEoKakGcW2Yr32I20"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // Initialize views
        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)
        loadingIndicator = findViewById(R.id.loadingIndicator)

        // Initially hide loading indicator
        loadingIndicator.visibility = View.GONE

        // Set up back button
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Set up RecyclerView
        chatAdapter = ChatAdapter(chatMessages)
        chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = chatAdapter
        }

        // Set up send button
        sendButton.setOnClickListener {
            val message = messageInput.text.toString()
            if (message.isNotEmpty()) {
                sendMessage(message)
                messageInput.text.clear()
            }
        }

        // Add initial greeting
        addBotMessage("Hello! I'm your AI health assistant. How can I help you today?")
    }

    private fun showLoading() {
        loadingIndicator.visibility = View.VISIBLE
        // Disable send button while loading
        sendButton.isEnabled = false
        messageInput.isEnabled = false
        // Scroll to show loading indicator
        chatRecyclerView.scrollToPosition(chatMessages.size - 1)
    }

    private fun hideLoading() {
        loadingIndicator.visibility = View.GONE
        // Re-enable input
        sendButton.isEnabled = true
        messageInput.isEnabled = true
    }

    private fun sendMessage(message: String) {
        // Add user message to chat
        addUserMessage(message)

        // Show loading indicator
        showLoading()

        // Generate AI response
        lifecycleScope.launch {
            try {
                val sharedPref = getSharedPreferences("PersonalInfo", MODE_PRIVATE)
                val gender = if (sharedPref.getBoolean("Gender", false)) "male" else "female"
                val age = sharedPref.getInt("Age", 0)
                val height = sharedPref.getInt("Height", 0)
                val weight = sharedPref.getInt("Weight", 0)
                val response = generativeModel.generateContent(message +
                        ". I am a $height\" $age year old $gender that weighs $weight pounds")
                hideLoading()
                addBotMessage(response.text ?: "I'm sorry, I couldn't process that.")
            } catch (e: Exception) {
                hideLoading()
                addBotMessage("Sorry, there was an error processing your request.")
            }
        }
    }

    private fun addUserMessage(message: String) {
        chatMessages.add(ChatMessage(message, true))
        chatAdapter.notifyItemInserted(chatMessages.size - 1)
        chatRecyclerView.scrollToPosition(chatMessages.size - 1)
    }

    private fun addBotMessage(message: String) {
        chatMessages.add(ChatMessage(message, false))
        chatAdapter.notifyItemInserted(chatMessages.size - 1)
        chatRecyclerView.scrollToPosition(chatMessages.size - 1)
    }
}
