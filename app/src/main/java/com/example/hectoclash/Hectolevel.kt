package com.example.hectoclash

import SequenceDataItem
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
import java.lang.reflect.Type

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

        numberList = (1..20).map { NumberData(it) }
        sequenceDataList = loadSequenceData()

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val uid = currentUser.email.toString()
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.contains("unlockedLevel")) {
                    unlockedLevel = document.getLong("unlockedLevel")?.toInt() ?: 1
                } else {
                    unlockedLevel = 1
                    // initialize on first login
                    firestore.collection("users").document(uid).set(mapOf("unlockedLevel" to 1))
                }

                levelAdapter = LevelAdapter(this, numberList, sequenceDataList, unlockedLevel)
                recyclerView.adapter = levelAdapter
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load progress", Toast.LENGTH_SHORT).show()
                Log.e("Hectolevel", "Error fetching unlocked level: ", it)
            }
    }

    fun unlockNextLevel(currentLevel: Int) {
        val currentUser = auth.currentUser ?: return
        val uid = currentUser.uid

        if (currentLevel + 1 > unlockedLevel) {
            unlockedLevel = currentLevel + 1
            firestore.collection("users").document(uid)
                .update("unlockedLevel", unlockedLevel)
                .addOnSuccessListener {
                    levelAdapter.updateUnlockedLevel(unlockedLevel)
                }
                .addOnFailureListener {
                    Log.e("Hectolevel", "Failed to update unlocked level", it)
                }
        }
    }

    private fun loadSequenceData(): List<SequenceDataItem> {
        return try {
            val inputStream = assets.open("sequence.json")
            val reader = InputStreamReader(inputStream)
            val type: Type = object : TypeToken<List<SequenceDataItem>>() {}.type
            Gson().fromJson(reader, type) ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to load sequence data", Toast.LENGTH_SHORT).show()
            emptyList()
        }
    }
}
