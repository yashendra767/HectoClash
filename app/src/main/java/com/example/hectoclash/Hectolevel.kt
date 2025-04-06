package com.example.hectoclash

import SequenceDataItem
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

data class NumberData(val value: Int)

class Hectolevel : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var levelAdapter: LevelAdapter
    private var numberList: List<NumberData> = listOf()
    private var sequenceDataList: List<SequenceDataItem> = listOf()
    private var unlockedLevel: Int = 1

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hectolevel)

        recyclerView = findViewById(R.id.levelrecview)
        recyclerView.layoutManager = LinearLayoutManager(this)

        numberList = (1..1000).map { NumberData(it) }
        sequenceDataList = loadSequenceData()

        val currentUser = auth.currentUser
        val uid = currentUser?.email.toString().replace(".",",") ?: return

        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                unlockedLevel = document.getLong("unlockedLevel")?.toInt() ?: 1
                Log.d("Hectolevel", "Initial unlocked level fetched: $unlockedLevel")
                levelAdapter = LevelAdapter(this, numberList, sequenceDataList, unlockedLevel)
                recyclerView.adapter = levelAdapter
            }
            .addOnFailureListener {
                Log.e("Hectolevel", "Failed to fetch user data", it)
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
            val newLevel = data?.getIntExtra("unlockedLevel", unlockedLevel) ?: unlockedLevel
            Log.d("Hectolevel", "onActivityResult: Received newLevel = $newLevel, Current unlockedLevel = $unlockedLevel")
            if (newLevel > unlockedLevel) {
                unlockedLevel = newLevel
                levelAdapter.updateUnlockedLevel(unlockedLevel)
                Toast.makeText(this, "🎉 Congratulations! Level $unlockedLevel unlocked!", Toast.LENGTH_LONG).show()
                Log.d("Hectolevel", "Unlocked level updated to: $unlockedLevel")
            } else {
                Log.d("Hectolevel", "New level ($newLevel) is not greater than current unlocked level ($unlockedLevel). No update.")
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Log.d("Hectolevel", "onActivityResult: Activity Result Canceled")
        } else {
            Log.w("Hectolevel", "onActivityResult: Unknown requestCode ($requestCode) or resultCode ($resultCode)")
        }
    }

    private fun loadSequenceData(): List<SequenceDataItem> {
        return try {
            val inputStream = assets.open("sequence.json")
            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<List<SequenceDataItem>>() {}.type
            Gson().fromJson(reader, type)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to load sequence data", Toast.LENGTH_SHORT).show()
            emptyList()
        }
    }
}