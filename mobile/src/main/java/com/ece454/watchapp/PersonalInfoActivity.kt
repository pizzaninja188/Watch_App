package com.ece454.watchapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import androidx.core.widget.doOnTextChanged
import java.util.Locale

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
    private lateinit var weightEditText: EditText
    private lateinit var weightSeekBar: SeekBar
    private lateinit var weightErrorTextView: TextView
    private lateinit var heightFeetEditText: EditText
    private lateinit var heightInchesEditText: EditText
    private lateinit var heightSeekBar: SeekBar
    private lateinit var heightErrorTextView: TextView
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

        val sharedPref = getPreferences(MODE_PRIVATE)
        if (sharedPref.contains("Gender")) {
            isMale = sharedPref.getBoolean("Gender", true)
            isFemale = !isMale
            maleRadioButton.isChecked = isMale
            femaleRadioButton.isChecked = isFemale
        }

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

        if (sharedPref.contains("Age")) {
            val age = sharedPref.getInt("Age", 0)
            ageEditText.setText(String.format(Locale.getDefault(), "%d", age))
            if (age < ageSeekBar.min) ageSeekBar.progress = ageSeekBar.min
            else if (age > ageSeekBar.max) ageSeekBar.progress = ageSeekBar.max
            else ageSeekBar.progress = age
        }

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

        ageEditText.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    // do nothing
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    if (s != null && s.isNotEmpty()) {
                        val num = Integer.parseInt(s.toString())
                        if (num < ageSeekBar.min) {
                            ageSeekBar.setOnSeekBarChangeListener(null)
                            ageSeekBar.progress = ageSeekBar.min
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
                        }
                        else if (num > ageSeekBar.max) {
                            ageSeekBar.setOnSeekBarChangeListener(null)
                            ageSeekBar.progress = ageSeekBar.max
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
                        }
                        else ageSeekBar.progress = num
                        ageEditText.setSelection(ageEditText.length())
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                    // do nothing
                }
            }
        )

        // weight entry logic
        weightEditText = findViewById<EditText>(R.id.weightEditText)
        weightSeekBar = findViewById<SeekBar>(R.id.weightSeekBar)
        weightErrorTextView = findViewById<TextView>(R.id.weightErrorTextView)

        if (sharedPref.contains("Weight")) {
            val weight = sharedPref.getInt("Weight", 0)
            weightEditText.setText(String.format(Locale.getDefault(), "%d", weight))
            if (weight < weightSeekBar.min) weightSeekBar.progress = weightSeekBar.min
            else if (weight > weightSeekBar.max) weightSeekBar.progress = weightSeekBar.max
            else weightSeekBar.progress = weight
        }

        weightSeekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    weightEditText.setText("$progress")
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    // do nothing
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    // do nothing
                }
            }
        )

        weightEditText.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    // do nothing
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    if (s != null && s.isNotEmpty()) {
                        val num = Integer.parseInt(s.toString())
                        if (num < weightSeekBar.min) {
                            weightSeekBar.setOnSeekBarChangeListener(null)
                            weightSeekBar.progress = weightSeekBar.min
                            weightSeekBar.setOnSeekBarChangeListener(
                                object : SeekBar.OnSeekBarChangeListener {
                                    override fun onProgressChanged(
                                        seekBar: SeekBar?,
                                        progress: Int,
                                        fromUser: Boolean
                                    ) {
                                        weightEditText.setText("$progress")
                                    }

                                    override fun onStartTrackingTouch(seekBar: SeekBar?) {
                                        // do nothing
                                    }

                                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                                        // do nothing
                                    }
                                }
                            )
                        }
                        else if (num > weightSeekBar.max) {
                            weightSeekBar.setOnSeekBarChangeListener(null)
                            weightSeekBar.progress = weightSeekBar.max
                            weightSeekBar.setOnSeekBarChangeListener(
                                object : SeekBar.OnSeekBarChangeListener {
                                    override fun onProgressChanged(
                                        seekBar: SeekBar?,
                                        progress: Int,
                                        fromUser: Boolean
                                    ) {
                                        weightEditText.setText("$progress")
                                    }

                                    override fun onStartTrackingTouch(seekBar: SeekBar?) {
                                        // do nothing
                                    }

                                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                                        // do nothing
                                    }
                                }
                            )
                        }
                        else weightSeekBar.progress = num
                        weightEditText.setSelection(weightEditText.length())
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                    // do nothing
                }
            }
        )

        // height entry logic
        heightFeetEditText = findViewById<EditText>(R.id.heightFeetEditText)
        heightInchesEditText = findViewById<EditText>(R.id.heightInchesEditText)
        heightSeekBar = findViewById<SeekBar>(R.id.heightSeekBar)
        heightErrorTextView = findViewById<TextView>(R.id.heightErrorTextView)

        if (sharedPref.contains("Height")) {
            val height = sharedPref.getInt("Height", 0)
            heightFeetEditText.setText(String.format(Locale.getDefault(), "%d", height / 12))
            heightInchesEditText.setText(String.format(Locale.getDefault(), "%d", height % 12))
            if (height < heightSeekBar.min) heightSeekBar.progress = heightSeekBar.min
            else if (height > heightSeekBar.max) heightSeekBar.progress = heightSeekBar.max
            else heightSeekBar.progress = height
        }

        heightSeekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    heightFeetEditText.setText("${progress / 12}")
                    heightInchesEditText.setText("${progress % 12}")
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    // do nothing
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    // do nothing
                }
            }
        )

        heightFeetEditText.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    // do nothing
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    if (s != null && s.isNotEmpty()) {
                        val feet = Integer.parseInt(s.toString())
                        val inches = if (heightInchesEditText.length() == 0) 0 else
                            Integer.parseInt(heightInchesEditText.text.toString())
                        val height = feet * 12 + inches
                        if (height < heightSeekBar.min) {
                            heightSeekBar.setOnSeekBarChangeListener(null)
                            heightSeekBar.progress = heightSeekBar.min
                            heightSeekBar.setOnSeekBarChangeListener(
                                object : SeekBar.OnSeekBarChangeListener {
                                    override fun onProgressChanged(
                                        seekBar: SeekBar?,
                                        progress: Int,
                                        fromUser: Boolean
                                    ) {
                                        heightFeetEditText.setText("${progress / 12}")
                                        heightInchesEditText.setText("${progress % 12}")
                                    }

                                    override fun onStartTrackingTouch(seekBar: SeekBar?) {
                                        // do nothing
                                    }

                                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                                        // do nothing
                                    }
                                }
                            )
                        }
                        else if (height > heightSeekBar.max) {
                            heightSeekBar.setOnSeekBarChangeListener(null)
                            heightSeekBar.progress = heightSeekBar.max
                            heightSeekBar.setOnSeekBarChangeListener(
                                object : SeekBar.OnSeekBarChangeListener {
                                    override fun onProgressChanged(
                                        seekBar: SeekBar?,
                                        progress: Int,
                                        fromUser: Boolean
                                    ) {
                                        heightFeetEditText.setText("${progress / 12}")
                                        heightInchesEditText.setText("${progress % 12}")
                                    }

                                    override fun onStartTrackingTouch(seekBar: SeekBar?) {
                                        // do nothing
                                    }

                                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                                        // do nothing
                                    }
                                }
                            )
                        }
                        else heightSeekBar.progress = height
                        heightFeetEditText.setSelection(heightFeetEditText.length())
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                    // do nothing
                }
            }
        )

        heightInchesEditText.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    // do nothing
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    if (s != null && s.isNotEmpty()) {
                        val feet = if (heightFeetEditText.length() == 0) 0 else
                            Integer.parseInt(heightFeetEditText.text.toString())
                        var inches = Integer.parseInt(s.toString())
                        if (inches > 11) {
                            heightInchesEditText.setText("11")
                            inches = 11
                        }
                        val height = feet * 12 + inches
                        if (height < heightSeekBar.min) {
                            heightSeekBar.setOnSeekBarChangeListener(null)
                            heightSeekBar.progress = heightSeekBar.min
                            heightSeekBar.setOnSeekBarChangeListener(
                                object : SeekBar.OnSeekBarChangeListener {
                                    override fun onProgressChanged(
                                        seekBar: SeekBar?,
                                        progress: Int,
                                        fromUser: Boolean
                                    ) {
                                        heightFeetEditText.setText("${progress / 12}")
                                        heightInchesEditText.setText("${progress % 12}")
                                    }

                                    override fun onStartTrackingTouch(seekBar: SeekBar?) {
                                        // do nothing
                                    }

                                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                                        // do nothing
                                    }
                                }
                            )
                        }
                        else if (height > heightSeekBar.max) {
                            heightSeekBar.setOnSeekBarChangeListener(null)
                            heightSeekBar.progress = heightSeekBar.max
                            heightSeekBar.setOnSeekBarChangeListener(
                                object : SeekBar.OnSeekBarChangeListener {
                                    override fun onProgressChanged(
                                        seekBar: SeekBar?,
                                        progress: Int,
                                        fromUser: Boolean
                                    ) {
                                        heightFeetEditText.setText("${progress / 12}")
                                        heightInchesEditText.setText("${progress % 12}")
                                    }

                                    override fun onStartTrackingTouch(seekBar: SeekBar?) {
                                        // do nothing
                                    }

                                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                                        // do nothing
                                    }
                                }
                            )
                        }
                        else heightSeekBar.progress = height
                        heightInchesEditText.setSelection(heightInchesEditText.length())
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                    // do nothing
                }
            }
        )

        saveButton.setOnClickListener {
            var done = true
            if (!(isMale || isFemale)) {
                genderErrorTextView.visibility = View.VISIBLE
                done = false
            } else genderErrorTextView.visibility = View.INVISIBLE
            if (ageEditText.length() == 0) {
                ageErrorTextView.visibility = View.VISIBLE
                done = false
            } else ageErrorTextView.visibility = View.INVISIBLE
            if (weightEditText.length() == 0) {
                weightErrorTextView.visibility = View.VISIBLE
                done = false
            } else weightErrorTextView.visibility = View.INVISIBLE
            if (heightFeetEditText.length() == 0 || heightInchesEditText.length() == 0) {
                heightErrorTextView.visibility = View.VISIBLE
                done = false
            } else heightErrorTextView.visibility = View.INVISIBLE
            if (done) {
                errorTextView.visibility = View.INVISIBLE
                val editor = sharedPref.edit()
                editor.putBoolean("Gender", isMale)
                editor.putInt("Age", Integer.parseInt(ageEditText.text.toString()))
                editor.putInt("Weight", Integer.parseInt(weightEditText.text.toString()))
                editor.putInt("Height", Integer.parseInt(heightFeetEditText.text.toString()) * 12 +
                        Integer.parseInt(heightInchesEditText.text.toString()))
                editor.apply()
                val intent = Intent(this@PersonalInfoActivity, MainActivity::class.java).apply {
                    putExtra("Gender", isMale)
                    putExtra("age", Integer.parseInt(ageEditText.text.toString()))
                    putExtra("weight", Integer.parseInt(weightEditText.text.toString()))
                    putExtra("height", Integer.parseInt(heightFeetEditText.text.toString()) * 12 +
                            Integer.parseInt(heightInchesEditText.text.toString()))
                }
                startActivity(intent)
            }
            else errorTextView.visibility = View.VISIBLE

        }
    }
}