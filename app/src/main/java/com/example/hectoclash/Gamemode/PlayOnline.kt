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
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader
import java.util.*
import kotlin.random.Random

class PlayOnline : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_online)

        // Get SharedPreferences
        val sharedPref = getSharedPreferences("HectoClashPrefs", Context.MODE_PRIVATE)
        val currentUserName = sharedPref.getString("heptoname", "You")
        val opponentName = sharedPref.getString("opponent_name", "Opponent")
        val opponentUserId = sharedPref.getString("opponent_name", null) // Correct key

        Log.d("PlayOnline", "Current user: $currentUserName, Opponent: $opponentName")

        // Set user names in UI
        findViewById<TextView>(R.id.currentUserNameTextView).text = currentUserName
        findViewById<TextView>(R.id.opponentNameTextView).text = opponentName

        // Start game logic
        findViewById<CardView>(R.id.StartGame).setOnClickListener {
            val currentUserId = auth.currentUser?.uid

            if (currentUserId == null || opponentUserId.isNullOrEmpty()) {
                Toast.makeText(this, "User info missing", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (currentUserId == opponentUserId) {
                Toast.makeText(this, "Cannot play against yourself!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val gameId = UUID.randomUUID().toString()

            val currentTime = System.currentTimeMillis()

            FirebaseDatabase.getInstance().reference.child("games")
                .child(gameId).child("status").setValue("active")
            FirebaseDatabase.getInstance().reference.child("games")
                .child(gameId).child("startTime").setValue(currentTime)
            val question = getRandomSequence()

            if (question == null) {
                Toast.makeText(this, "Failed to load questions", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val gameRequest = mapOf(
                "player1" to currentUserId,
                "player2" to opponentUserId,
                "status" to "waiting",
                "question" to mapOf(
                    "sequence" to question.sequence,
                    "operator_sequence" to question.operator_sequence,
                    "solution" to question.solution
                )
            )

            database.child("games").child(gameId).setValue(gameRequest)
                .addOnSuccessListener {
                    Toast.makeText(this, "Game request sent!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, GameInterface::class.java)
                    intent.putExtra("gameId", gameId)
                    startActivity(intent)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to start game", Toast.LENGTH_SHORT).show()
                }
        }



        val database = FirebaseDatabase.getInstance().reference
        val userGamesRef = database.child("games")

        userGamesRef.orderByChild("player2").equalTo(currentUserName)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val game = snapshot.getValue(GameData::class.java)
                    if (game?.status == "waiting") {
                        // Show dialog
                        showGameRequestDialog(snapshot.key ?: "", game)
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {

                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    fun showGameRequestDialog(gameId: String, game: GameData) {
        AlertDialog.Builder(this)
            .setTitle("Game Invitation")
            .setMessage("Player ${game.player1} wants to play. Accept?")
            .setPositiveButton("Accept") { _, _ ->
                startMultiplayerGame(gameId, game)
            }
            .setNegativeButton("Reject") { _, _ ->
                FirebaseDatabase.getInstance().reference.child("games").child(gameId)
                    .child("status").setValue("rejected")
            }
            .setCancelable(false)
        .show()
    }
    private fun startMultiplayerGame(gameId: String, game: GameData) {
        val database = FirebaseDatabase.getInstance().reference

        // Update game status to active
        database.child("games").child(gameId).child("status").setValue("active")
            .addOnSuccessListener {
                // Start the game interface activity
                val intent = Intent(this, GameInterface::class.java)
                intent.putExtra("gameId", gameId)
                startActivity(intent)
                finish() // Optional: close current lobby screen
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to start game", Toast.LENGTH_SHORT).show()
                }
    }

    private fun getRandomSequence(): HectoQuestion? {
        return readJsonFromAssets()?.let { list ->
            if (list.isNotEmpty()) list[Random.nextInt(list.size)] else null
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
}
