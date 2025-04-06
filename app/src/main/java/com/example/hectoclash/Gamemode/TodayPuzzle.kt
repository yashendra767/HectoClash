package com.example.hectoclash.Gamemode

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.hectoclash.R
import com.example.hectoclash.dataclass.HectoQuestion
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TodayPuzzle : AppCompatActivity() {

    // UI Elements
    private lateinit var sequenceTextView: TextView
    private lateinit var solutionContainer: LinearLayout
    private lateinit var timerTextView: TextView
    private lateinit var submitButton: CardView
    private lateinit var resetButton: ImageView

    // Game State
    private val operatorFields = mutableListOf<EditText>()
    private var correctOperatorSequence = listOf<String>()
    private var userInputSequence = listOf<String>()
    private var timer: CountDownTimer? = null
    private val colors = listOf("#f94144", "#f3722c", "#f8961e", "#f9844a", "#f9c74f")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_today_puzzle)

        initializeViews()
        setupListeners()
        loadRandomSequence()
        if (userInputSequence == correctOperatorSequence) {
            markPuzzleAsDoneToday() // <- Call this

        }


    }

    private fun markPuzzleAsDoneToday() {
        val prefs = getSharedPreferences("HectoClashPrefs", Context.MODE_PRIVATE)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        prefs.edit().putString("daily_puzzle_date", today).apply()
    }



    private fun initializeViews() {
        sequenceTextView = findViewById(R.id.sequence)
        solutionContainer = findViewById(R.id.solutioncontainer)
        timerTextView = findViewById(R.id.timerText)
        submitButton = findViewById(R.id.btnsubmit)
        resetButton = findViewById(R.id.resetbtn)
    }

    private fun setupListeners() {
        submitButton.setOnClickListener { checkSolution() }

        resetButton.setOnClickListener {
            operatorFields.forEach { it.setText("") }
            operatorFields.firstOrNull()?.requestFocus()
        }


        val operatorMap = mapOf(
            R.id.cardAdd to "+",
            R.id.cardSubtract to "-",
            R.id.cardMultiply to "*",
            R.id.cardDivide to "/"
        )

        operatorMap.forEach { (id, op) ->
            findViewById<CardView>(id).setOnClickListener {
                insertOperatorIntoFocusedField(op)
            }
        }
    }

    private fun checkSolution() {
        userInputSequence = operatorFields.map { it.text.toString().trim() }

        if (userInputSequence.any { it.isEmpty() }) {
            showDialog("❌ Incomplete!", "Please fill in all operator fields.")
            return
        }

        if (userInputSequence == correctOperatorSequence) {
            markPuzzleAsDoneToday() // ✅ Save completion
            showDialog("✅ Correct!", "You entered the correct operators! 🎉")
        } else {
            showDialog("❌ Incorrect!", "Correct sequence: ${correctOperatorSequence.joinToString("")}")
            }
    }

    private fun showDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                finish() // 👈 This finishes TodayPuzzle and returns to MainActivity
            }
            .setCancelable(false)
            .show()
    }




        private fun loadRandomSequence() {
        val item = getRandomSequence()
        if (item != null) {
            sequenceTextView.text = "Hecto Sequence: ${item.sequence}"
            correctOperatorSequence = item.operator_sequence
            displayEditableSolution(item.solution)
        } else {
            sequenceTextView.text = "Failed to load sequence."
        }
    }

    private fun getRandomSequence(): HectoQuestion? {
        return readJsonFromAssets()?.let { it.randomOrNull() }
    }

    private fun readJsonFromAssets(): List<HectoQuestion>? {
        return try {
            val inputStream = assets.open("sequence.json")
            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<List<HectoQuestion>>() {}.type
            Gson().fromJson(reader, type)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun displayEditableSolution(solution: String) {
        solutionContainer.removeAllViews()
        operatorFields.clear()

        val regex = "[0-9]+".toRegex()
        val numbers = regex.findAll(solution).map { it.value }.toList()
        var numberIndex = 0

        for (char in solution) {
            when {
                char.isDigit() -> addNumberCard(numbers[numberIndex++], numberIndex)
                char in "+-*/" -> addOperatorInput()
                char == '(' || char == ')' -> addParenthesisView(char)
            }
        }
    }

    private fun addNumberCard(number: String, index: Int) {
        val card = CardView(this).apply {
            radius = 24f
            layoutParams = LinearLayout.LayoutParams(100, 100).apply {
                setMargins(12, 12, 12, 12)
            }
            setCardBackgroundColor(Color.parseColor(colors[index % colors.size]))
        }

        val text = TextView(this).apply {
            text = number
            textSize = 20f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
        }

        card.addView(text)
        solutionContainer.addView(card)
    }

    private fun addOperatorInput() {
        val editText = EditText(this).apply {
            textSize = 20f
            width = 40
            height = 80
            gravity = Gravity.CENTER
            filters = arrayOf(InputFilter.LengthFilter(1))
            setTextColor(Color.WHITE)
            inputType = InputType.TYPE_NULL
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(Color.TRANSPARENT)
            }

            try {
                val method = EditText::class.java.getMethod(
                    "setShowSoftInputOnFocus",
                    Boolean::class.javaPrimitiveType
                )
                method.invoke(this, false)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        operatorFields.add(editText)
        solutionContainer.addView(editText)

        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                if (s?.length == 1) {
                    val next = operatorFields.getOrNull(operatorFields.indexOf(editText) + 1)
                    next?.requestFocus()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun addParenthesisView(char: Char) {
        val view = TextView(this).apply {
            text = char.toString()
            textSize = 40f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
        }
        solutionContainer.addView(view)
    }

    private fun insertOperatorIntoFocusedField(operator: String) {
        val focusedField = operatorFields.find { it.isFocused }
        focusedField?.setText(operator)
        val currentIndex = operatorFields.indexOf(focusedField)
        operatorFields.getOrNull(currentIndex + 1)?.requestFocus()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}
