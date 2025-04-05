package com.example.hectoclash

import SequenceDataItem
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class LevelAdapter(
    val context: Hectolevel,
    var numberList: List<NumberData>,
    var sequencedata: List<SequenceDataItem>,
    private var unlockedLevel: Int
) : RecyclerView.Adapter<LevelAdapter.LevelViewHolder>() {

    class LevelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val levelNumberTextView: TextView = itemView.findViewById(R.id.levelnum)
        val levelNumberTextView1: TextView = itemView.findViewById(R.id.levelnum1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LevelViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.levelrecview, parent, false)
        return LevelViewHolder(view)
    }

    override fun getItemCount(): Int = numberList.size

    override fun onBindViewHolder(holder: LevelViewHolder, position: Int) {
        val currentNumberData = numberList[position]
        val levelNumber = currentNumberData.value
        val sequenceItem = sequencedata.getOrNull(position)

        holder.levelNumberTextView.text = "$levelNumber"
        holder.levelNumberTextView1.text = levelNumber.toString()

        if (levelNumber <= unlockedLevel) {
            holder.itemView.alpha = 1f
            holder.itemView.isEnabled = true
            holder.itemView.setOnClickListener {
                if (sequenceItem != null) {
                    Toast.makeText(context, "Clicked Level: $levelNumber", Toast.LENGTH_SHORT).show()
                    val intent = Intent(context, LevelDetailActivity::class.java)
                    intent.putExtra("clickedLevel", levelNumber.toString())
                    intent.putExtra("Sequencedata", sequenceItem.sequence)
                    intent.putExtra("Soln_of_seq", sequenceItem.solution)
                    intent.putExtra("soln_operator_seq", ArrayList(sequenceItem.operator_sequence))
                    context.startActivity(intent)
                } else {
                    Toast.makeText(context, "Level $levelNumber - Sequence data missing", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            holder.itemView.alpha = 0.3f
            holder.itemView.isEnabled = false
            holder.itemView.setOnClickListener {
                Toast.makeText(context, "❌ Level Locked! Complete previous levels.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun updateUnlockedLevel(newUnlockedLevel: Int) {
        unlockedLevel = newUnlockedLevel
        notifyDataSetChanged()
    }
}
