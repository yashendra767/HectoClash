package com.example.hectoclash

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.example.hectoclash.Gamemode.Learn
import com.example.hectoclash.Gamemode.PlayOnline
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class home : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val playOnline = view.findViewById<CardView>(R.id.playOnline)

        playOnline.setOnClickListener {
            findRandomOpponent()
        }
        val learnHecto = view.findViewById<CardView>(R.id.learnHectoClash)
        learnHecto.setOnClickListener{
            val intent = Intent(requireContext(),Learn::class.java)
            startActivity(intent)
        }
        return view
    }


    private fun findRandomOpponent() {
        val currentUserEmail = auth.currentUser?.email?.replace(".", ",") ?: return

        firestore.collection("users").get()
            .addOnSuccessListener { result ->
                val otherUsers = result.documents.filter { it.id != currentUserEmail }

                if (otherUsers.isNotEmpty()) {
                    val randomUser = otherUsers.random()
                    val opponentName = randomUser.getString("heptoName")

                    if (!opponentName.isNullOrEmpty()) {
                        saveOpponentToSharedPrefs(opponentName)
                        startActivity(Intent(requireContext(), PlayOnline::class.java))
                    } else {
                        Toast.makeText(requireContext(), "Opponent has no username.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "No other users found!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to get users: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("HomeFragment", "Firestore error: ", e)
            }
    }

    private fun saveOpponentToSharedPrefs(opponentName: String) {
        val sharedPrefs = requireContext().getSharedPreferences("HectoClashPrefs", Context.MODE_PRIVATE)
        sharedPrefs.edit()
            .putString("opponent_name", opponentName)
            .apply()
        Log.d("SharedPrefs", "Opponent $opponentName stored for game.")
    }
}
