package com.example.hectoclash.Gamemode

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.hectoclash.R
import com.example.hectoclash.dataclass.GameData
import com.example.hectoclash.dataclass.HectoQuestion
import com.google.firebase.database.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader
import kotlin.random.Random

class GameInterface : AppCompatActivity() {

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
    private val gameDuration = 2 * 60 * 1000L // 2 minutes
    private val colors = listOf("#f94144", "#f3722c", "#f8961e", "#f9844a", "#f9c74f")

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_interface)

        // Initialize UI
        initializeViews()
        setupListeners()

        val gameId = intent.getStringExtra("gameId")
        if (!gameId.isNullOrEmpty()) {
            loadGameFromFirebase(gameId)
        } else {
            loadRandomSequence()
            startGameTimer(gameDuration)
        }
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

        // Operator insertion
        val operatorMap = mapOf(
            R.id.cardAdd to "+",
            R.id.cardSubtract to "-",
            R.id.cardMultiply to "*",
            R.id.cardDivide to "/"
        )

        operatorMap.forEach { (id, op) ->
            findViewById<CardView>(id).setOnClickListener { insertOperatorIntoFocusedField(op) }
        }
    }




    private fun loadGameFromFirebase(gameId: String) {
        val gameRef = FirebaseDatabase.getInstance().reference.child("games").child(gameId)
        val startTime = System.currentTimeMillis()

        gameRef.child("status").setValue("active")
        gameRef.child("startTime").setValue(startTime)

        gameRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val game = snapshot.getValue(GameData::class.java)

                if (game != null) {
                    if (game.status == "active") {
                        val startTime = game.startTime ?: return
                        val remainingTime = gameDuration - (System.currentTimeMillis() - startTime)

                        // Prevent game from starting again on repeated trigger
                        gameRef.removeEventListener(this)

                        if (game.question != null) {
                            sequenceTextView.text = "Hecto Sequence: ${game.question!!.sequence}"
                            displayEditableSolution(game.question.solution)
                            correctOperatorSequence = game.question.operator_sequence
                            startGameTimer(remainingTime)
                        }
                    } else if (game.status == "waiting") {
                        sequenceTextView.text = "Waiting for opponent to accept..."
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@GameInterface, "Failed to load game", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun startGameTimer(remainingTime: Long) {
        timer = object : CountDownTimer(remainingTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 60000
                val seconds = (millisUntilFinished % 60000) / 1000
                timerTextView.text = String.format("Time Left: %02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                timerTextView.text = "Time's Up!"
                checkSolution()
            }
        }.start()
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
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
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
                val method = EditText::class.java.getMethod("setShowSoftInputOnFocus", Boolean::class.javaPrimitiveType)
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
