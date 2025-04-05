package com.example.hectoclash.Gamemode

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
import java.util.*
import kotlin.random.Random

class PlayOnline : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()
    private var waitingDialog: AlertDialog? = null
    private var gameRequestDialog: AlertDialog? = null
    private var questionsList: List<HectoQuestion> = emptyList()
    private var gameNotificationsListener: ChildEventListener? = null
    private var gameStatusListener: ValueEventListener? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_online)

        // Load questions from JSON on activity creation
        loadQuestions()

        // Get SharedPreferences
        val sharedPref = getSharedPreferences("HectoClashPrefs", Context.MODE_PRIVATE)
        val currentUserName = sharedPref.getString("heptoname", "You")
        val opponentName = sharedPref.getString("opponent_name", "Opponent")
        val opponentEmail = sharedPref.getString("opponent_email", "opponent_name")

        Log.d("PlayOnline", "Current user: $currentUserName, Opponent: $opponentName, OpponentEmail: $opponentEmail")

        // Set user names in UI
        findViewById<TextView>(R.id.currentUserNameTextView).text = currentUserName
        findViewById<TextView>(R.id.opponentNameTextView).text = opponentName

        // Start game logic
        findViewById<CardView>(R.id.StartGame).setOnClickListener {
            startGameRequest(currentUserName, opponentName, opponentEmail)
        }

        // Listen for incoming game requests
        listenForIncomingGameRequests()
    }

    private fun loadQuestions() {
        questionsList = readJsonFromAssets().orEmpty()
        Log.d("PlayOnline", "Loaded ${questionsList.size} questions.")
        if (questionsList.isEmpty()) {
            Toast.makeText(this, "Failed to load questions.", Toast.LENGTH_LONG).show()
        }
    }

    private fun startGameRequest(currentUserName: String?, opponentName: String?, opponentEmail: String?) {
        val currentUserEmail = auth.currentUser?.email

        when {
            currentUserEmail == null || opponentEmail.isNullOrEmpty() -> {
                Toast.makeText(this, "User information is missing.", Toast.LENGTH_SHORT).show()
                Log.e("PlayOnline", "Missing info: current=$currentUserEmail, opponent=$opponentEmail")
            }
            currentUserEmail == opponentEmail -> {
                Toast.makeText(this, "You cannot play against yourself!", Toast.LENGTH_SHORT).show()
            }
            questionsList.isEmpty() -> {
                Toast.makeText(this, "No questions available. Cannot start game.", Toast.LENGTH_SHORT).show()
            }
            else -> {
                val gameId = UUID.randomUUID().toString()
                val currentTime = System.currentTimeMillis()
                val question = getRandomQuestion()

                if (question != null) {
                    val gameRequest = mapOf(
                        "player1" to currentUserEmail,
                        "player2" to opponentEmail,
                        "player1Name" to currentUserName,
                        "player2Name" to opponentName,
                        "status" to "waiting",
                        "startTime" to currentTime,
                        "player1Ready" to false,
                        "player2Ready" to false,
                        "countdownStarted" to false,
                        "syncStartTime" to 0L,
                        "question" to mapOf(
                            "sequence" to question.sequence,
                            "operator_sequence" to question.operator_sequence,
                            "solution" to question.solution
                        )
                    )

                    // Save game data
                    database.child("games").child(gameId).setValue(gameRequest)
                        .addOnSuccessListener {
                            Log.d("PlayOnline", "Game data saved with ID: $gameId")

                            // Create a separate notification for the opponent
                            val gameNotification = mapOf(
                                "gameId" to gameId,
                                "senderName" to currentUserName,
                                "senderEmail" to currentUserEmail,
                                "timestamp" to currentTime
                            )

                            // Create a sanitized email key (replace dots with commas)
                            val sanitizedEmail = opponentEmail.replace(".", ",")

                            // Write to user-specific notification node
                            database.child("game_notifications")
                                .child(sanitizedEmail)
                                .child(gameId)
                                .setValue(gameNotification)
                                .addOnSuccessListener {
                                    Log.d("PlayOnline", "Game notification saved for $opponentEmail")
                                    Toast.makeText(this, "Game request sent! Waiting for opponent...", Toast.LENGTH_SHORT).show()
                                    showWaitingForOpponentDialog(gameId)
                                }
                                .addOnFailureListener { error ->
                                    Log.e("PlayOnline", "Failed to save notification: ${error.message}")
                                    Toast.makeText(this, "Failed to notify opponent.", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener { error ->
                            Log.e("PlayOnline", "Failed to save game: ${error.message}")
                            Toast.makeText(this, "Failed to start game.", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Failed to get a random question.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun listenForIncomingGameRequests() {
        val currentUserEmail = auth.currentUser?.email
        if (currentUserEmail != null) {
            val sanitizedEmail = currentUserEmail.replace(".", ",")

            Log.d("PlayOnline", "Listening for notifications at: $sanitizedEmail")

            // Create listener
            gameNotificationsListener = object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    Log.d("PlayOnline", "Notification received: ${snapshot.key}")

                    val gameId = snapshot.child("gameId").getValue(String::class.java)
                    val senderName = snapshot.child("senderName").getValue(String::class.java) ?: "Unknown player"

                    if (gameId != null) {
                        // Show the game request dialog
                        showGameRequestDialog(gameId, senderName)

                        // Remove the notification once processed
                        snapshot.ref.removeValue()
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    // Not needed for notifications
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    // Not needed for notifications
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    // Not needed for notifications
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("PlayOnline", "Game notifications listener cancelled: ${error.message}")
                }
            }

            // Attach listener
            database.child("game_notifications")
                .child(sanitizedEmail)
                .addChildEventListener(gameNotificationsListener!!)
        }
    }

    private fun showWaitingForOpponentDialog(gameId: String) {
        waitingDialog = AlertDialog.Builder(this)
            .setTitle("Waiting for Opponent")
            .setMessage("Waiting for opponent to accept your game request...")
            .setCancelable(false)
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                cancelGameRequest(gameId)
            }
            .create()

        waitingDialog?.show()

        listenForGameStatusChanges(gameId)
    }

    private fun listenForGameStatusChanges(gameId: String) {
        val gameStatusRef = database.child("games").child(gameId)

        // Remove previous listener if exists
        if (gameStatusListener != null) {
            gameStatusRef.removeEventListener(gameStatusListener!!)
        }

        // Create new listener
        gameStatusListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.child("status").getValue(String::class.java)
                Log.d("PlayOnline", "Game status changed to: $status")

                when (status) {
                    "active" -> {
                        // Mark player1 as ready since they initiated the game
                        database.child("games").child(gameId).child("player1Ready").setValue(true)
                        startGameInterface(gameId)
                    }
                    "rejected" -> {
                        dismissWaitingDialog()
                        showGameRejectedToast()
                        gameStatusRef.removeEventListener(this)
                    }
                    "cancelled" -> {
                        dismissWaitingDialog()
                        gameStatusRef.removeEventListener(this)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                dismissWaitingDialog()
                Toast.makeText(this@PlayOnline, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                gameStatusRef.removeEventListener(this)
            }
        }

        // Attach listener
        gameStatusRef.addValueEventListener(gameStatusListener!!)
    }

    private fun dismissWaitingDialog() {
        waitingDialog?.dismiss()
        waitingDialog = null
    }

    private fun cancelGameRequest(gameId: String) {
        database.child("games").child(gameId).child("status").setValue("cancelled")
            .addOnSuccessListener {
                Toast.makeText(this, "Game request cancelled.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to cancel game request.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showGameRequestDialog(gameId: String, player1Name: String) {
        // Dismiss any existing dialog
        gameRequestDialog?.dismiss()

        // Create new dialog
        gameRequestDialog = AlertDialog.Builder(this)
            .setTitle("Game Invitation")
            .setMessage("$player1Name wants to play. Accept?")
            .setPositiveButton("Accept") { _, _ ->
                acceptGameRequest(gameId)
            }
            .setNegativeButton("Reject") { _, _ ->
                rejectGameRequest(gameId)
            }
            .setCancelable(false)
            .create()

        gameRequestDialog?.show()
    }

    private fun acceptGameRequest(gameId: String) {
        // Set the game status to active
        database.child("games").child(gameId).child("status").setValue("active")
            .addOnSuccessListener {
                // Set player2 as ready since they explicitly accepted
                database.child("games").child(gameId).child("player2Ready").setValue(true)
                startGameInterface(gameId)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to accept game.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun rejectGameRequest(gameId: String) {
        database.child("games").child(gameId).child("status").setValue("rejected")
            .addOnSuccessListener {
                Toast.makeText(this, "Game request rejected.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to reject game.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun startGameInterface(gameId: String) {
        dismissWaitingDialog()
        gameRequestDialog?.dismiss()
        val intent = Intent(this, GameInterface::class.java)
        intent.putExtra("gameId", gameId)
        startActivity(intent)
    }

    private fun showGameRejectedToast() {
        dismissWaitingDialog()
        Toast.makeText(this, "Game request was rejected by the opponent.", Toast.LENGTH_SHORT).show()
    }

    private fun showGameCancelledToast() {
        dismissWaitingDialog()
        Toast.makeText(this, "The game request was cancelled.", Toast.LENGTH_SHORT).show()
    }

    private fun getRandomQuestion(): HectoQuestion? {
        return if (questionsList.isNotEmpty()) questionsList[Random.nextInt(questionsList.size)] else null
    }

    private fun readJsonFromAssets(): List<HectoQuestion>? {
        return try {
            val inputStream = assets.open("sequence.json")
            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<List<HectoQuestion>>() {}.type
            Gson().fromJson(reader, type)
        } catch (e: Exception) {
            Log.e("PlayOnline", "Error reading sequence.json: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-register listeners when activity comes to foreground
        if (gameNotificationsListener == null) {
            listenForIncomingGameRequests()
        }
    }

    override fun onPause() {
        super.onPause()
        // Don't remove listeners on pause to ensure we still receive notifications
    }

    override fun onDestroy() {
        super.onDestroy()
        // Dismiss any dialogs to prevent memory leaks
        waitingDialog?.dismiss()
        gameRequestDialog?.dismiss()

        // Remove all listeners
        val currentUserEmail = auth.currentUser?.email
        if (currentUserEmail != null && gameNotificationsListener != null) {
            val sanitizedEmail = currentUserEmail.replace(".", ",")
            database.child("game_notifications")
                .child(sanitizedEmail)
                .removeEventListener(gameNotificationsListener!!)
            gameNotificationsListener = null
        }

        // Remove game status listener if exists
        if (gameStatusListener != null) {
            gameStatusListener = null
        }
    }
}