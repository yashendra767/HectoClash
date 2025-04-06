package com.example.hectoclash.more

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.hectoclash.Login.CreateAccount
import com.example.hectoclash.Login.GetStarted
import com.example.hectoclash.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Profile : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailText: TextView
    private lateinit var hectoName: TextView
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        emailText = findViewById(R.id.Gmail)
        hectoName = findViewById(R.id.uniqueid)
        val level =findViewById<TextView>(R.id.tVhectoLevel)
        val score =findViewById<TextView>(R.id.tVhectoScore)



        val logOut = findViewById<TextView>(R.id.btnLogout)
        logOut.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this@Profile, GetStarted::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        val email = auth.currentUser?.email
        val safeEmail = email?.replace(".", ",")

        if (email != null && safeEmail != null) {
            emailText.text = email

            firestore.collection("users").document(safeEmail).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val userHectoName = document.getString("heptoName")
                        hectoName.text = userHectoName ?: "No HectoName"
                        val scoretext=document.getLong("HectoScore")?.toInt()
                        score.text ="👑 ${scoretext ?:0}"

                        val levelNumber = document.getLong("unlockedLevel")?.toInt()
                        level.text = "👑 ${levelNumber ?: 1}"

                    } else {
                        Log.d("ProfileActivity", "No such document in Firestore")
                        hectoName.text = "No HectoName"
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("ProfileActivity", "Error fetching hectoName from Firestore", exception)
                    hectoName.text = "Error loading"
                }

        } else {
            Log.w("ProfileActivity", "User email is null.")
        }
    }
}
