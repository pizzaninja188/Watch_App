package com.ece454.watchapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class StartupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_startup)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val sharedPrefs = getSharedPreferences("PersonalInfo", MODE_PRIVATE)
        if (sharedPrefs.contains("Gender")) {
            val intent = Intent(this@StartupActivity, MainActivity::class.java)
            startActivity(intent)
        } else {
            val intent = Intent(this@StartupActivity, PersonalInfoActivity::class.java)
            intent.putExtra("FirstTime", true)
            startActivity(intent)
        }
    }
}