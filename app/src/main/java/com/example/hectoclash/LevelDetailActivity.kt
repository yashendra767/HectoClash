package com.example.hectoclash

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LevelDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_level_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
       /* val clickedLevel = intent.getIntExtra("clickedLevel", -1)
        val detailTextView = findViewById<TextView>(R.id.detailTextView)

        if (clickedLevel != -1) {
            detailTextView.text = "You clicked on Level: $clickedLevel"
        } else {
            detailTextView.text = "No level data received."
        }*/
    }
}