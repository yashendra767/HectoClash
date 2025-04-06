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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader
import kotlin.random.Random

class GameInterface : AppCompatActivity() {

    private lateinit var currentUserEmail: String
    private var isFirstPlayer: Boolean = false

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

    // Firebase
    private lateinit var gameRef: DatabaseReference
    private var gameId: String? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if this activity is being re-created unnecessarily
        if (!isTaskRoot() && intent?.hasCategory(Intent.CATEGORY_LAUNCHER) == true) {
            finish() // If it's not the root and was launched again, just finish
            return
        }

        setContentView(R.layout.activity_game_interface)

        // Initialize UI
        initializeViews()
        setupListeners()
        var auth =FirebaseAuth.getInstance()
        currentUserEmail =auth.currentUser?.email.toString().replace(".",",")

        gameId = intent.getStringExtra("gameId")
        gameRef= gameId?.let { FirebaseDatabase.getInstance().getReference("games").child(it) }!!
        if (!gameId.isNullOrEmpty()) {
            loadGameFromFirebase(gameId!!)
            setupRealtimeListeners()
        } else {
            loadRandomSequence()
            startGameTimer(gameDuration)
        }



    }

    private fun setupRealtimeListeners() {
        // First, determine if the current user is player 1
        gameRef.child("player1").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val player1Email = snapshot.getValue(String::class.java)
                isFirstPlayer = (player1Email == currentUserEmail)

                // Now set up listeners based on player role
                setupPlayerSolutionListener()
                setupWinnerListener()
                setupGameStatusListener()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@GameInterface, "Failed to identify player role", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupPlayerSolutionListener() {
        // Listen to opponent's solution only
        val opponentSolutionKey = if (isFirstPlayer) "player2Solution" else "player1Solution"

        gameRef.child(opponentSolutionKey).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val solution = snapshot.getValue(object : GenericTypeIndicator<List<String>>() {})
                if (solution != null) {
                    updateOpponentSolution(solution)
                } else {
                    resetOpponentSolution()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@GameInterface, "Failed to load opponent's solution", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupWinnerListener() {
        gameRef.child("winner").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val winnerEmail = snapshot.getValue(String::class.java)
                if (!winnerEmail.isNullOrEmpty()) {
                    timer?.cancel()
                    if (winnerEmail == currentUserEmail) {
                        showResultDialog("✅ Correct!", "You solved it first!") {
                            startActivity(Intent(this@GameInterface, WinResultScreen::class.java))
                            finish()
                        }
                    } else {
                        showResultDialog(
                            "❌ Incorrect!",
                            "Opponent solved it first. Correct sequence: ${correctOperatorSequence.joinToString(" ")}"
                        ) {
                            startActivity(Intent(this@GameInterface, LossResultScreen::class.java))
                            finish()
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@GameInterface, "Failed to load winner data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupGameStatusListener() {
        gameRef.child("status").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.getValue(String::class.java)
                if (status == "finished") {
                    timer?.cancel()
                    showResultDialog(
                        "⏱️ Time's Up!",
                        "No one solved it in time. Correct sequence: ${correctOperatorSequence.joinToString(" ")}"
                    ) {
                        finish()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@GameInterface, "Failed to load game status", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun updateOpponentSolution(solution: List<String>) {
        // You'll need to visually represent the opponent's solution.
        // This could involve displaying a separate read-only view or updating the existing one with different styling.
        // For now, let's just log it.
        println("Opponent's Solution: $solution")
    }

    private fun resetOpponentSolution() {
        println("Opponent's solution reset")
        // Update UI to reflect the reset of the opponent's solution
    }

    private fun showResultDialog(title: String, message: String, onDismiss: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { _, _ -> onDismiss() }
            .setCancelable(false)
            .show()
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

        gameRef.addListenerForSingleValueEvent(object : ValueEventListener { // Changed to SingleValueEvent
            override fun onDataChange(snapshot: DataSnapshot) {
                val game = snapshot.getValue(GameData::class.java)

                if (game != null) {
                    if (game.question != null) {
                        sequenceTextView.text = "Hecto Sequence: ${game.question!!.sequence}"
                        displayEditableSolution(game.question.solution)
                        correctOperatorSequence = game.question.operator_sequence
                        val initialRemainingTime = gameDuration - (System.currentTimeMillis() - (game.startTime ?: startTime))
                        if (initialRemainingTime > 0) {
                            startGameTimer(initialRemainingTime)
                        } else {
                            timerTextView.text = "Time's Up!"
                        }
                    }
                    // You might need to set up a separate listener for ongoing game changes
                } else {
                    Toast.makeText(this@GameInterface, "Game not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@GameInterface, "Failed to load game", Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }


    private fun startGameTimer(remainingTime: Long) {
        timer?.cancel() // Ensure any existing timer is cancelled
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
            AlertDialog.Builder(this)
                .setTitle("❌ Incomplete!")
                .setMessage("Please fill in all operator fields.")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        // Check if already solved (winner exists)
        gameRef.child("winner").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Someone already won
                    Toast.makeText(this@GameInterface, "Game already ended!", Toast.LENGTH_SHORT).show()
                    return
                }

                val isCorrect = userInputSequence == correctOperatorSequence

                // Save player's solution
                val solutionKey = if (isFirstPlayer) "player1Solution" else "player2Solution"
                gameRef.child(solutionKey).setValue(userInputSequence)

                if (isCorrect) {
                    // Mark current user as winner
                    gameRef.child("winner").setValue(currentUserEmail)

                    // Show win dialog
                    AlertDialog.Builder(this@GameInterface)
                        .setTitle("✅ Correct!")
                        .setMessage("You solved it first! 🎉")
                        .setPositiveButton("OK") { _, _ ->
                            startActivity(Intent(this@GameInterface, WinResultScreen::class.java))
                            finish()
                        }
                        .setCancelable(false)
                        .show()
                } else {
                    // Show incorrect dialog (but don't mark winner)
                    AlertDialog.Builder(this@GameInterface)
                        .setTitle("❌ Incorrect!")
                        .setMessage("Correct sequence: ${correctOperatorSequence.joinToString(" ")}")
                        .setPositiveButton("OK", null)
                        .show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@GameInterface, "Error checking winner", Toast.LENGTH_SHORT).show()
                }
        })
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