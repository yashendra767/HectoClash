package com.example.hectoclash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.example.hectoclash.Login.Login

class SplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Edge-to-edge fullscreen mode
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_splash_screen)

        // Delay before moving to MainActivity (e.g., 4 seconds)
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, Login::class.java))
            finish() // Close splash screen
        }, 4000)
    }
}
