package com.example.hectoclash

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.util.Log
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LevelDetailActivity : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private lateinit var solutionContainer: LinearLayout
    private lateinit var sequenceTextView: TextView
    private lateinit var levelNumberTextView: TextView
    private lateinit var continueButton: CardView
    private val operatorFields = mutableListOf<EditText>()
    private var correctOperatorSequence: List<String> = listOf()
    private var clickedLevel: Int = 1

    private val colors = listOf("#f94144", "#f3722c", "#f8961e", "#f9844a", "#f9c74f", "#90be6d", "#43aa8b", "#577590")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_level_detail)

        levelNumberTextView = findViewById(R.id.levelgame_levelno)
        sequenceTextView = findViewById(R.id.sequence)
        solutionContainer = findViewById(R.id.solutioncontainer_level)
        continueButton = findViewById(R.id.btnsubmit_level)

        clickedLevel = intent.getStringExtra("clickedLevel")?.toIntOrNull() ?: 1
        val seqData = intent.getStringExtra("Sequencedata")
        val solnOfSeq = intent.getStringExtra("Soln_of_seq")
        val solnOperatorSeq = intent.getStringArrayListExtra("soln_operator_seq") ?: arrayListOf()

        Log.d("LevelDetail", "onCreate: Clicked Level = $clickedLevel")
        levelNumberTextView.text = clickedLevel.toString()
        sequenceTextView.text = "Hecto Sequence: ${seqData ?: ""}"
        correctOperatorSequence = solnOperatorSeq

        solnOfSeq?.let { displaySolutionSequence(it) }

        continueButton.setOnClickListener { checkUserAnswer() }

        findViewById<CardView>(R.id.cardAdd).setOnClickListener { insertOperatorIntoFirstEmpty("+") }
        findViewById<CardView>(R.id.cardSubtract).setOnClickListener { insertOperatorIntoFirstEmpty("-") }
        findViewById<CardView>(R.id.cardMultiply).setOnClickListener { insertOperatorIntoFirstEmpty("*") }
        findViewById<CardView>(R.id.cardDivide).setOnClickListener { insertOperatorIntoFirstEmpty("/") }
        findViewById<ImageView>(R.id.resetbtn).setOnClickListener { resetFields() }
    }

    private fun getSafeColor(index: Int): Int = Color.parseColor(colors.getOrNull(index % colors.size) ?: "#CCCCCC")

    private fun displaySolutionSequence(solution: String) {
        solutionContainer.removeAllViews()
        operatorFields.clear()

        val numbers = "[0-9]+".toRegex().findAll(solution).map { it.value }.toList()
        var numberIndex = 0

        for (char in solution) {
            when {
                char.isDigit() -> {
                    addNumberTextView(numbers.getOrNull(numberIndex++) ?: "", numberIndex)
                }
                char in "+-*/" -> addOperatorInputField(operatorFields.size)
                char == '(' || char == ')' -> addParenthesisTextView(char)
            }
        }
    }

    private fun addNumberTextView(number: String, index: Int) {
        val textView = TextView(this).apply {
            text = number
            textSize = 50f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 24f
                setColor(getSafeColor(index))
                setStroke(3, Color.DKGRAY)
            }
            layoutParams = LinearLayout.LayoutParams(150, 150).apply {
                setMargins(22, 22, 22, 22)
            }
        }
        solutionContainer.addView(textView)
    }

    private fun addOperatorInputField(position: Int) {
        val editText = EditText(this).apply {
            hint = "?"
            textSize = 40f
            gravity = Gravity.CENTER
            filters = arrayOf(InputFilter.LengthFilter(1))
            inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            keyListener = DigitsKeyListener.getInstance("+-*/")
            isFocusable = false
            isClickable = true
            layoutParams = LinearLayout.LayoutParams(150, 150).apply {
                setMargins(22, 22, 22, 22)
            }
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 16f
                setColor(getSafeColor(position))
                setStroke(2, Color.DKGRAY)
            }
        }
        operatorFields.add(editText)
        solutionContainer.addView(editText)
    }

    private fun addParenthesisTextView(char: Char) {
        val textView = TextView(this).apply {
            text = char.toString()
            textSize = 70f
            gravity = Gravity.CENTER
            setTextColor(Color.BLUE)
            background = GradientDrawable().apply {
                cornerRadius = 24f
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(42, 42, 42, 42)
            }
        }
        solutionContainer.addView(textView)
    }

    private fun insertOperatorIntoFirstEmpty(operator: String) {
        for (editText in operatorFields) {
            if (editText.text.isNullOrBlank()) {
                editText.setText(operator)
                break
            }
        }
    }

    private fun checkUserAnswer() {
        val userInput = operatorFields.map { it.text.toString().trim() }

        if (userInput.any { it.isEmpty() }) {
            Toast.makeText(this, "Please fill in all operators!", Toast.LENGTH_SHORT).show()
            return
        }

        var isCorrect = true
        userInput.forEachIndexed { index, input ->
            val isMatch = input == correctOperatorSequence.getOrNull(index)
            operatorFields[index].background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 16f
                setColor(Color.parseColor(if (isMatch) "#4CAF50" else "#F44336"))
            }
            if (!isMatch) isCorrect = false
        }

        if (isCorrect) {
            unlockAndReturn()
        } else {
            Toast.makeText(this, "❌ Incorrect! Try again.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun unlockAndReturn() {
        val uid = auth.currentUser?.email.toString().replace(".",",") ?: return
        val nextLevel = clickedLevel + 1
        Log.d("LevelDetail", "unlockAndReturn: Current Level = $clickedLevel, Next Level to unlock = $nextLevel")

        firestore.collection("users").document(uid)
            .update("unlockedLevel", nextLevel)
            .addOnSuccessListener {
                Log.d("LevelDetail", "Firestore update success. Unlocked level: $nextLevel")
                val intent = Intent().apply {
                    putExtra("unlockedLevel", nextLevel)
                }
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
            .addOnFailureListener {
                Log.e("LevelDetail", "Failed to update unlocked level", it)
                finish()
            }
    }

    private fun resetFields() {
        operatorFields.forEachIndexed { index, field ->
            field.setText("")
            field.background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 16f
                setColor(getSafeColor(index))
                setStroke(2, Color.DKGRAY)
            }
        }
    }
}