package com.example.hectoclash.Gamemode

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.hectoclash.MainActivity
import com.example.hectoclash.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class LossResultScreen : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_loss_result_screen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val cont = findViewById<CardView>(R.id.cont)
        cont.setOnClickListener {
            startActivity(Intent(this,MainActivity::class.java))
        }

        val score = findViewById<TextView>(R.id.scoretominus)
        score.text = "-20"
        deductHectoScoreFromFirestore(20)
    }

    private fun deductHectoScoreFromFirestore(scoreToDeduct: Int) {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val email = currentUser.email.toString().replace(".",",") ?: return
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users").document(email)

            val updates = hashMapOf<String, Any>(
                "HectoScore" to com.google.firebase.firestore.FieldValue.increment(-scoreToDeduct.toLong())
            )

            userRef.set(updates, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d("Firestore", "HectoScore decremented by $scoreToDeduct for $email.")
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Failed to decrement HectoScore: ${e.message}")
                }
        } else {
            Log.w("Auth", "No authenticated user found.")
        }
    }
}
