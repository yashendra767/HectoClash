package com.example.hectoclash

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hectoclash.dataclass.LeaderBoard

class LeaderBoardAdapter(
    private var itemList: List<LeaderBoard>,
    private val context: Context
) : RecyclerView.Adapter<LeaderBoardAdapter.LeaderboardViewHolder>() {

    class LeaderboardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.nameScore)
        val tvScore: TextView = view.findViewById(R.id.score)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.each_item, parent, false)
        return LeaderboardViewHolder(view)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        val item = itemList[position]
        holder.tvName.text = item.name
        holder.tvScore.text = item.value.toString()
    }

    override fun getItemCount(): Int = itemList.size

    fun updateData(newList: List<LeaderBoard>) {
        itemList = newList
        notifyDataSetChanged()
    }
}
