package com.example.hectoclash.Login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.hectoclash.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CreateAccount : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_account)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val mailEditText = findViewById<TextInputEditText>(R.id.signUpEmail)
        val passEditText = findViewById<TextInputEditText>(R.id.signUpPass)
        val alreadyAccount = findViewById<Button>(R.id.tvLogin)
        val signupContinue = findViewById<CardView>(R.id.signUpContinue)

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("User")

        alreadyAccount.setOnClickListener {
            startActivity(Intent(this, SignIn::class.java))
        }

        signupContinue.setOnClickListener {
            val email = mailEditText?.text.toString().trim()
            val pass = passEditText?.text.toString().trim()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                signupUser(email, pass)
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signupUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    val user = User(mail = email, pass = password)
                    if (userId != null) {
                        storeUserInDatabase(email, user) // Use email as key for Realtime Database
                    }
                    Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, Username::class.java)
                    intent.putExtra("mail", email) // Pass the email to the Username activity
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        "Authentication Failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun storeUserInDatabase(email: String, user: User) {
        databaseReference.child(email.replace(".", ",")).setValue(user)
            .addOnSuccessListener {
                Log.d("CreateAccount", "User data saved to Realtime Database for email: $email")
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to store user data in Realtime Database", Toast.LENGTH_SHORT).show()
                Log.e("CreateAccount", "Error storing user data in Realtime Database", e)
            }
    }
}

// User data model for Realtime Database
data class User(
    val mail: String = "",
    val pass: String = ""
)