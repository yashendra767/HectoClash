package com.example.hectoclash

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.hectoclash.Gamemode.Learn
import com.example.hectoclash.Gamemode.PlayOnline
import com.example.hectoclash.Gamemode.TodayPuzzle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class home : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var dailyPuzzleCard: CardView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val playOnline = view.findViewById<CardView>(R.id.playOnline)
        val learnHecto = view.findViewById<CardView>(R.id.learnHectoClash)
        dailyPuzzleCard = view.findViewById(R.id.dailyPuzzle)
        val hectolevel = view.findViewById<CardView>(R.id.solveHectoClash)

        hectolevel.setOnClickListener {
            startActivity(Intent(requireContext(), Hectolevel::class.java))
        }

        playOnline.setOnClickListener { findRandomOpponent() }

        learnHecto.setOnClickListener {
            startActivity(Intent(requireContext(), Learn::class.java))
        }

        // Check and update puzzle status right when the view is created
        updateDailyPuzzleStatus()

        dailyPuzzleCard.setOnClickListener {
            if (hasCompletedTodayPuzzle()) {
                Toast.makeText(requireContext(), "✅ You’ve already solved today’s puzzle!", Toast.LENGTH_SHORT).show()
            } else {
                startActivity(Intent(requireContext(), TodayPuzzle::class.java))
            }
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        updateDailyPuzzleStatus() // Re-check every time user returns to the fragment
    }

    private fun hasCompletedTodayPuzzle(): Boolean {
        val prefs = requireContext().getSharedPreferences("HectoClashPrefs", Context.MODE_PRIVATE)
        val savedDate = prefs.getString("daily_puzzle_date", "") ?: ""
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return savedDate == today
    }

    private fun updateDailyPuzzleStatus() {
        if (hasCompletedTodayPuzzle()) {
            dailyPuzzleCard.alpha = 0.3f
        } else {
            dailyPuzzleCard.alpha = 1f
        }
    }

    private fun findRandomOpponent() {
        val currentUserEmail = auth.currentUser?.email ?: return

        firestore.collection("users").get()
            .addOnSuccessListener { result ->
                val otherUsers = result.documents.filter { it.id != currentUserEmail.replace(".", ",") }

                if (otherUsers.isNotEmpty()) {
                    val randomUser = otherUsers.random()
                    val opponentEmail = randomUser.id.replace(",", ".") // Get email from document ID
                    val opponentName = randomUser.getString("heptoName")

                    if (!opponentEmail.isNullOrEmpty() && !opponentName.isNullOrEmpty()) {
                        saveOpponentToSharedPrefs(opponentName, opponentEmail) // Pass email
                        startActivity(Intent(requireContext(), PlayOnline::class.java))
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Opponent data is incomplete.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "No other users found!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Failed to get users: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("HomeFragment", "Firestore error: ", e)
            }
    }

    private fun saveOpponentToSharedPrefs(opponentName: String, opponentEmail: String) {
        val prefs = requireContext().getSharedPreferences("HectoClashPrefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("opponent_name", opponentName)
        editor.putString("opponent_email", opponentEmail) // Save email
        editor.apply()
        Log.d("SharedPrefs", "Opponent $opponentName ($opponentEmail) stored for game.")
    }
}