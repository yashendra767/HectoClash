package com.example.hectoclash.Gamemode

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.hectoclash.R
import com.example.hectoclash.dataclass.HectoQuestion
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader
import kotlin.random.Random

class GameInterface : AppCompatActivity() {

    private lateinit var solutionContainer: LinearLayout
    private lateinit var sequenceTextView: TextView
    private lateinit var timerTextView: TextView
    private lateinit var submitButton: CardView
    private lateinit var resetButton: Button
    private lateinit var newSequenceButton: Button

    private val operatorFields = mutableListOf<EditText>()
    private var correctOperatorSequence: List<String> = listOf()
    private var userInputSequence: List<String> = listOf()

    private val colors = listOf("#f94144", "#f3722c", "#f8961e", "#f9844a", "#f9c74f")
    private var timer: CountDownTimer? = null
    private val gameDuration = 2 * 60 * 1000L  // 2 minutes

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_interface)

        sequenceTextView = findViewById(R.id.sequence)
        solutionContainer = findViewById(R.id.solutioncontainer)
        timerTextView = findViewById(R.id.timerText)
        submitButton = findViewById(R.id.btnsubmit)

        submitButton.setOnClickListener { checkSolution() }

        loadRandomSequence()
        startGameTimer()
    }

    private fun checkSolution() {
        userInputSequence = operatorFields.map { it.text.toString().trim() }

        if (userInputSequence.any { it.isEmpty() }) {
            showDialog("❌ Incomplete!", "Please fill in all operator fields.")
            return
        }

        if (userInputSequence == correctOperatorSequence) {
            showDialog("✅ Correct!", "You entered the correct operators! 🎉")
        } else {
            showDialog("❌ Incorrect!", "Correct sequence: ${correctOperatorSequence.joinToString(" ")}")
        }
    }

    private fun showDialog(title: String, message: String) {
        val dialog = AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .create()
        dialog.show()
    }



    private fun startGameTimer() {
        timer = object : CountDownTimer(gameDuration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 60000
                val seconds = (millisUntilFinished % 60000) / 1000
                timerTextView.text = String.format("Time Left: %02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                timerTextView.text = "Time's Up!"
//                showTimeUpDialog()
            }
        }.start()
    }

//    private fun showTimeUpDialog() {
//        val dialog = Dialog(this)
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//        dialog.setCancelable(false)
//        dialog.setContentView(R.layout.dialog_time_up)
//
//        val btnProceed = dialog.findViewById<Button>(R.id.btnProceed)
//        btnProceed.setOnClickListener {
//            dialog.dismiss()
//            evaluateUserInput()
//        }
//
//        dialog.show()
//    }

    private fun evaluateUserInput() {
        userInputSequence = operatorFields.map { it.text.toString().trim() }
        val correctCount = userInputSequence.zip(correctOperatorSequence).count { it.first == it.second }

        val intent = Intent(this, OnlineResultScreen::class.java).apply {
            putExtra("correctCount", correctCount)
            putExtra("totalCount", correctOperatorSequence.size)
        }
        startActivity(intent)
        finish()
    }

    private fun loadRandomSequence() {
        val item = getRandomSequence()
        item?.let {
            sequenceTextView.text = "Hecto Sequence: ${it.sequence}"
            correctOperatorSequence = it.operator_sequence
            displayEditableSolution(it.solution)
        } ?: run {
            sequenceTextView.text = "Failed to load sequence."
        }
    }

    private fun getRandomSequence(): HectoQuestion? {
        return readJsonFromAssets()?.takeIf { it.isNotEmpty() }?.let {
            it[Random.nextInt(it.size)]
        }
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
                char.isDigit() -> {
                    addNumberCard(numbers[numberIndex], numberIndex)
                    numberIndex++
                }
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

        val numberText = TextView(this).apply {
            text = number
            textSize = 20f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
        }

        card.addView(numberText)
        solutionContainer.addView(card)
    }

    private fun addOperatorInput() {
        val editText = EditText(this).apply {
            textSize = 22f
            width = 40
            height = 80
            gravity = Gravity.CENTER
            inputType = InputType.TYPE_CLASS_TEXT
            filters = arrayOf(InputFilter.LengthFilter(1))
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                setColor(Color.TRANSPARENT)
            }
        }

        operatorFields.add(editText)
        solutionContainer.addView(editText)

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                if (s?.length == 1) {
                    val next = operatorFields.getOrNull(operatorFields.indexOf(editText) + 1)
                    next?.requestFocus()
                }
            }
        })
    }

    private fun addParenthesisView(char: Char) {
        val textView = TextView(this).apply {
            text = char.toString()
            textSize = 40f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
        }
        solutionContainer.addView(textView)
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}
