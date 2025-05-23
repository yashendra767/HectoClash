package com.example.hectoclash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.example.hectoclash.Login.GetStarted
import com.google.firebase.auth.FirebaseAuth

class SplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Edge-to-edge fullscreen mode
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_splash_screen)



        var auth = FirebaseAuth.getInstance()
        var currentuser =  auth.currentUser
        if (currentuser!=null){
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(this, MainActivity::class.java))
                finish() // Close splash screen
            }, 4000)

        }
        else{
            // Delay before moving to MainActivity (e.g., 4 seconds)
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(this, GetStarted::class.java))
                finish() // Close splash screen
            }, 4000)
        }

    }

}
