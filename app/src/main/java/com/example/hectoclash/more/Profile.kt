package com.example.hectoclash.more

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.hectoclash.Login.CreateAccount
import com.example.hectoclash.Login.SignIn
import com.example.hectoclash.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class Profile : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var fb: DatabaseReference
    private lateinit var nameEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val logOut = findViewById<TextView>(R.id.btnLogout)
        logOut.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this@Profile, CreateAccount::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
        auth = FirebaseAuth.getInstance()
        val email = auth.currentUser?.email?.replace(".", ",")
        fb = FirebaseDatabase.getInstance().getReference("User")
        nameEditText = findViewById(R.id.Name)

        if (email != null) {
            fb.child(email).child("name")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val userName = snapshot.getValue(String::class.java)
                            nameEditText.setText(userName)
                        } else {
                            Log.d("ProfileActivity", "Name not found for user: $email")
                            // Optionally set a default value or handle the case where the name is not present
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("ProfileActivity", "Database error: ${error.message}")
                        // Optionally display an error message to the user
                    }
                })
        } else {
            Log.w("ProfileActivity", "User email is null.")
            // Optionally handle the case where the user is not logged in
        }
    }
}