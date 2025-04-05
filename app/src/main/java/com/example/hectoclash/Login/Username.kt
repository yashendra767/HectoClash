package com.example.hectoclash.Login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
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

class Username : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_username)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val currentUserEmail = auth.currentUser?.email?.replace(".", ",") ?: ""
        val heptoNameEditText = findViewById<EditText>(R.id.hectoName)
        val saveNameButton = findViewById<CardView>(R.id.getstarted)

        saveNameButton.setOnClickListener {
            val heptoName = heptoNameEditText.text.toString().trim()

            if (heptoName.isNotEmpty()) {
                saveHeptoName(currentUserEmail, heptoName)
            } else {
                Toast.makeText(this, "Please enter a HeptoName", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveHeptoName(userMail: String, heptoName: String) {
        val userData = hashMapOf("heptoName" to heptoName)

        firestore.collection("users").document(userMail)
            .set(userData, SetOptions.merge())
            .addOnSuccessListener {
                saveToSharedPreferences(heptoName)
                Toast.makeText(this, "HeptoName saved successfully!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save HeptoName: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("Username", "Error saving HeptoName", e)
            }
    }

    private fun saveToSharedPreferences(heptoName: String) {
        val sharedPref = getSharedPreferences("HectoClashPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("user_heptoName", heptoName)
            apply()
        }
        Log.d("SharedPrefs", "HeptoName saved in SharedPreferences")
    }
}
