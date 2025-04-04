package com.example.hectoclash

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class LevelAdapter(
    val context: Hectolevel,
    var numberList: List<NumberData>
) : RecyclerView.Adapter<LevelAdapter.LevelViewHolder>() {

    class LevelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val levelNumberTextView: TextView = itemView.findViewById(R.id.levelnum)
        val levelNumberTextView1: TextView = itemView.findViewById(R.id.levelnum1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LevelViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.levelrecview, parent, false)
        return LevelViewHolder(view)
    }

    override fun getItemCount(): Int {
        return numberList.size
    }

    override fun onBindViewHolder(holder: LevelViewHolder, position: Int) {
        val currentNumberData = numberList[position]
        val levelNumber = currentNumberData.value
        holder.levelNumberTextView.text = "Level $levelNumber"
        holder.levelNumberTextView1.text = levelNumber.toString()

        holder.itemView.setOnClickListener {
            Toast.makeText(context, "Clicked Level: $levelNumber", Toast.LENGTH_SHORT).show()
            val intent = Intent(context, LevelDetailActivity::class.java)
            intent.putExtra("clickedLevel", levelNumber)
            context.startActivity(intent)
        }
    }
}