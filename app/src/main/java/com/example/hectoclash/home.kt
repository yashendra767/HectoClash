package com.example.hectoclash

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import com.example.hectoclash.Gamemode.Learn
import com.example.hectoclash.Gamemode.PlayOnline

class home : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val playOnline = view.findViewById<CardView>(R.id.playOnline)
        val solveHectoClash = view.findViewById<CardView>(R.id.solveHectoClash)
        val dailyPuzzle = view.findViewById<CardView>(R.id.dailyPuzzle)
        val gameRooms = view.findViewById<CardView>(R.id.gameRooms)
        val learnHectoClash = view.findViewById<CardView>(R.id.learnHectoClash)

        playOnline.setOnClickListener {
            startActivity(Intent(requireContext(), PlayOnline::class.java))
        }

//        solveHectoClash.setOnClickListener {
//            startActivity(Intent(requireContext(), SolveHectoClashActivity::class.java))
//        }
//
//        dailyPuzzle.setOnClickListener {
//            startActivity(Intent(requireContext(), DailyPuzzleActivity::class.java))
//        }
//
//        gameRooms.setOnClickListener {
//            startActivity(Intent(requireContext(), GameRoomsActivity::class.java))
//        }
//
        learnHectoClash.setOnClickListener {
            startActivity(Intent(requireContext(), Learn::class.java))
        }

        return view
    }
}
