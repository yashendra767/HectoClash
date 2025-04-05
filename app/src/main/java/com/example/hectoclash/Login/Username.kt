package com.example.hectoclash.Login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.hectoclash.MainActivity
import com.example.hectoclash.R
import com.example.hectoclash.home
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class Username : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
 private lateinit var auth :FirebaseAuth
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
        val currentuser = auth.currentUser?.email.toString().replace(".",",")
        val mail = intent.getStringExtra("mail")
        val heptoNameEditText = findViewById<EditText>(R.id.hectoName) // Replace with your EditText ID
        val saveNameButton = findViewById<CardView>(R.id.getstarted) // Replace with your Button ID

        saveNameButton.setOnClickListener {
            val heptoName = heptoNameEditText.text.toString().trim()
            val email = mail.toString().replace(".",",")
            if (heptoName.isNotEmpty() ) {
                saveHeptoName(currentuser, heptoName)
            } else {
                Toast.makeText(this, "Please enter a HeptoName", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveHeptoName(userMail: String, heptoName: String) {
        val userData = hashMapOf(
            "heptoName" to heptoName
        )

        // Use the email as the document ID
        firestore.collection("users").document(userMail)
            .set(userData, SetOptions.merge()) // Use merge to avoid overwriting existing data
            .addOnSuccessListener {
                Toast.makeText(this, "HeptoName saved successfully!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java)) // Navigate to Home or next activity
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save HeptoName: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("Username", "Error saving HeptoName", e)
            }
    }
}