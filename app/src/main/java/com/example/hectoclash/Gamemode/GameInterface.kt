package com.example.hectoclash.Gamemode

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
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
    private lateinit var submitButton: Button
    private lateinit var resetButton: Button
    private lateinit var newSequenceButton: Button

    private val operatorFields = mutableListOf<EditText>()
    private var correctOperatorSequence: List<String> = listOf()

    private val colors = listOf("#f94144", "#f3722c", "#f8961e", "#f9844a", "#f9c74f")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_interface)

        sequenceTextView = findViewById(R.id.sequence)
        solutionContainer = findViewById(R.id.solutioncontainer)

        loadRandomSequence()
    }

    private fun insertOperator(op: String) {
        val focused = operatorFields.find { it.hasFocus() }
        focused?.setText(op)
    }

    private fun loadRandomSequence() {
        val item = getRandomSequence()
        item?.let {
            sequenceTextView.text = "Sequence: ${it.sequence}"
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
        val splitSolution = solution.split(regex)

        for (i in numbers.indices) {
            addNumberCard(numbers[i], i)

            if (i < splitSolution.size - 1) {
                val ops = splitSolution[i + 1].trim()
                for (char in ops) {
                    when (char) {
                        '(', ')' -> addParenthesisView(char)
                        in "+-*/" -> addOperatorInput()
                    }
                }
            }
        }

        val resultText = TextView(this).apply {
            text = "= 100"
            textSize = 22f
            gravity = Gravity.CENTER
            setTextColor(Color.BLACK)
        }
        solutionContainer.addView(resultText)
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
            width = 50 // ⬅ Reduced width
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

    private fun checkSolution() {
        val userInput = operatorFields.map { it.text.toString().trim() }

        if (userInput.any { it.isEmpty() }) {
            showDialog("❌ Incomplete!", "Please fill in all operator fields.")
            return
        }

        if (userInput == correctOperatorSequence) {
            showDialog("✅ Correct!", "You entered the correct operators! 🎉")
        } else {
            showDialog("❌ Incorrect!", "Correct sequence: ${correctOperatorSequence.joinToString(" ")}")
        }
    }

    private fun resetInputs() {
        operatorFields.forEach { it.text.clear() }
    }

    private fun showDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}
