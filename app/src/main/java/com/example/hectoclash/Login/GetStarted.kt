package com.example.hectoclash.Login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.hectoclash.MainActivity

import com.example.hectoclash.R
import com.example.hectoclash.home
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

private const val RC_SIGN_IN = 123

class GetStarted : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var firestore: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_get_started)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("User")
        firestore = FirebaseFirestore.getInstance()

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val getStartedButton = findViewById<CardView>(R.id.getstarted)
        val googleButton = findViewById<CardView>(R.id.google)

        getStartedButton.setOnClickListener {
            startActivity(Intent(this, CreateAccount::class.java))
            finish()
        }

        googleButton.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed: ${e.statusCode}", Toast.LENGTH_SHORT).show()
                Log.e("GetStarted", "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        val userEmail = it.email
                        val userName = acct.displayName // Google account's display name
                        if (userEmail != null && userName != null) {
                            saveUserData(userEmail, userName)
                        }
                    }
                } else {
                    Toast.makeText(this, "Firebase authentication failed.", Toast.LENGTH_SHORT).show()
                    Log.e("GetStarted", "Firebase auth failed", task.exception)
                }
            }
    }

    private fun saveUserData(userEmail: String, userName: String) {
        // Save to Realtime Database (email and password - but Google sign-in doesn't give us the password directly)
        val userMap = hashMapOf(
            "email" to userEmail,
            "password" to "GoogleSignIn" // Placeholder, you don't get the actual password
        )

        databaseReference.child(userEmail.replace(".", ",")).setValue(userMap)
            .addOnSuccessListener {
                Log.d("GetStarted", "User data saved to Realtime Database")
            }
            .addOnFailureListener { e ->
                Log.e("GetStarted", "Failed to save user data to Realtime Database", e)
            }

        // Save to Firestore (email and name)
        val firestoreUserData = hashMapOf(
            "name" to userName
        )

        firestore.collection("users").document(userEmail)
            .set(firestoreUserData, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Google sign-in successful!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save user data to Firestore.", Toast.LENGTH_SHORT).show()
                Log.e("GetStarted", "Failed to save user data to Firestore", e)
            }
    }
}