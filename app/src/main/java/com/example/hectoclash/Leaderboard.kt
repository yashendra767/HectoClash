package com.example.hectoclash

import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hectoclash.dataclass.LeaderBoard
import com.google.firebase.firestore.FirebaseFirestore

class Leaderboard : Fragment() {

    private lateinit var spinner: Spinner
    private lateinit var rvLeaderboard: RecyclerView
    private lateinit var leaderboardAdapter: LeaderBoardAdapter
    private val db = FirebaseFirestore.getInstance()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_leaderboard, container, false)

        spinner = view.findViewById(R.id.spinner)
        rvLeaderboard = view.findViewById(R.id.rvLeaderboard)
        leaderboardAdapter = LeaderBoardAdapter(emptyList(), requireContext())

        rvLeaderboard.layoutManager = LinearLayoutManager(requireContext())
        rvLeaderboard.adapter = leaderboardAdapter

        setupSpinner()

        return view
    }

    private fun setupSpinner() {
        val items = listOf("Select an option", "HectoScore", "HectoLevel")

        val adapter = object : ArrayAdapter<String>(
            requireContext(),
            R.layout.spinner_item_white,
            items
        ) {
            override fun isEnabled(position: Int) = position != 0
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                view.setTextColor(if (position == 0) Color.GRAY else Color.WHITE)
                return view
            }
        }

        adapter.setDropDownViewResource(R.layout.spinner_item_white)
        spinner.adapter = adapter
        spinner.setSelection(0, false)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position > 0) {
                    val selected = parent.getItemAtPosition(position).toString()
                    fetchLeaderboardData(selected)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun fetchLeaderboardData(category: String) {
        val field = when (category) {
            "HectoScore" -> "HectoScore"
            "HectoLevel" -> "unlockedLevel"
            else -> return
        }

        db.collection("users")
            .orderBy(field, com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val leaderboardList = mutableListOf<LeaderBoard>()

                for (doc in documents) {
                    val name = doc.getString("heptoName") ?: "Unknown"
                    val value = doc.getLong(field)?.toInt() ?: 0
                    leaderboardList.add(LeaderBoard(name, value))
                }

                leaderboardAdapter.updateData(leaderboardList)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error fetching data", Toast.LENGTH_SHORT).show()
            }
    }
}
