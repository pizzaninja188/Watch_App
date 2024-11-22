package com.ece454.watchapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PersonalInfoActivity : AppCompatActivity() {
    private lateinit var genderRadioGroup: RadioGroup
    private lateinit var maleRadioButton: RadioButton
    private lateinit var femaleRadioButton: RadioButton
    private var isMale = false
    private var isFemale = false
    private lateinit var genderErrorTextView: TextView
    private lateinit var ageEditText: EditText
    private lateinit var ageSeekBar: SeekBar
    private lateinit var ageErrorTextView: TextView
    private lateinit var errorTextView: TextView
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_personal_info)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // gender entry logic
        genderRadioGroup = findViewById<RadioGroup>(R.id.genderRadioGroup)
        maleRadioButton = findViewById<RadioButton>(R.id.maleRadioButton)
        femaleRadioButton = findViewById<RadioButton>(R.id.femaleRadioButton)
        saveButton = findViewById<Button>(R.id.saveButton)
        genderErrorTextView = findViewById<TextView>(R.id.genderErrorTextView)
        errorTextView = findViewById<TextView>(R.id.errorTextView)

        maleRadioButton.setOnCheckedChangeListener { button, bool ->
            isMale = maleRadioButton.isChecked
        }

        femaleRadioButton.setOnCheckedChangeListener { button, bool ->
            isFemale = femaleRadioButton.isChecked
        }

        // age entry logic
        ageEditText = findViewById<EditText>(R.id.ageEditText)
        ageSeekBar = findViewById<SeekBar>(R.id.ageSeekBar)
        ageErrorTextView = findViewById<TextView>(R.id.ageErrorTextView)

        ageSeekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    ageEditText.setText("$progress")
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    // do nothing
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    // do nothing
                }

            }
        )

        saveButton.setOnClickListener {
            if (!(isMale || isFemale)) {
                genderErrorTextView.visibility = View.VISIBLE
                errorTextView.visibility = View.VISIBLE
            }
        }
    }
}