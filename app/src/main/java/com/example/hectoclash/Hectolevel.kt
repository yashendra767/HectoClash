package com.example.hectoclash

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Hectolevel : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var levelAdapter: LevelAdapter
    private var numberList: ArrayList<NumberData> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_hectolevel)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.levelrecview)
        recyclerView.layoutManager = LinearLayoutManager(this)

        for (i in 1..50) {
            numberList.add(NumberData(i))
        }

        levelAdapter = LevelAdapter(this, numberList)
        recyclerView.adapter = levelAdapter
    }
}


data class NumberData(val value: Int)