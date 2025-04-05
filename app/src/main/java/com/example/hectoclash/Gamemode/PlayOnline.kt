package com.example.hectoclash.Gamemode

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.hectoclash.R


class PlayOnline : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_online)

        val StartGame = findViewById<CardView>(R.id.StartGame)
        StartGame.setOnClickListener {
            startActivity(Intent(this, GameInterface::class.java))
        }

    }
}
