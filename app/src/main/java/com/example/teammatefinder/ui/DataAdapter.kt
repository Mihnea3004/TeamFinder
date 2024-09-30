package com.example.teammatefinder.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.teammatefinder.R

class DataAdapter(
    private val players: List<Player>,
    private val currentUser: String, // ID of the current user to exclude
    private val onItemClicked: (Player) -> Unit, // Callback for click events
    private val game: String
) : RecyclerView.Adapter<DataAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.player_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val player = players[position]
        // Bind data to views
        when (game) {
            "League of Legends" -> {
                holder.tagTextView.text = player.tag
                holder.winrateTextView.text = player.winrate
                holder.serverTextView.text = player.server
                val division = player.division.substringBefore(" ")
                Log.d("DataAdapter", "Division: $division")
                when (division) {
                    "IRON" -> holder.divisionImageView.setImageResource(R.drawable.iron_lol)
                    "BRONZE" -> holder.divisionImageView.setImageResource(R.drawable.bronze_lol)
                    "SILVER" -> holder.divisionImageView.setImageResource(R.drawable.silver_lol)
                    "GOLD" -> holder.divisionImageView.setImageResource(R.drawable.gold_lol)
                    "PLATINUM" -> holder.divisionImageView.setImageResource(R.drawable.platinum_lol)
                    "DIAMOND" -> holder.divisionImageView.setImageResource(R.drawable.diamond_lol)
                    "MASTER" -> holder.divisionImageView.setImageResource(R.drawable.master_lol)
                    "GRANDMASTER" -> holder.divisionImageView.setImageResource(R.drawable.grandmaster_lol)
                    "CHALLENGER" -> holder.divisionImageView.setImageResource(R.drawable.challenger_lol)
                    else -> holder.divisionImageView.setImageResource(R.drawable.unranked_lol)
                }
            }
            "Valorant" -> {
             //   when (player.division) {

             //   }
            }
            "TFT" -> {
                val division = player.division.substringBefore(" ")
                when (division) {
                    "IRON" -> holder.divisionImageView.setImageResource(R.drawable.iron_lol)
                    "BRONZE" -> holder.divisionImageView.setImageResource(R.drawable.bronze_lol)
                    "SILVER" -> holder.divisionImageView.setImageResource(R.drawable.silver_lol)
                    "GOLD" -> holder.divisionImageView.setImageResource(R.drawable.gold_lol)
                    "PLATINUM" -> holder.divisionImageView.setImageResource(R.drawable.platinum_lol)
                    "DIAMOND" -> holder.divisionImageView.setImageResource(R.drawable.diamond_lol)
                    "MASTER" -> holder.divisionImageView.setImageResource(R.drawable.master_lol)
                    "GRANDMASTER" -> holder.divisionImageView.setImageResource(R.drawable.grandmaster_lol)
                    "CHALLENGER" -> holder.divisionImageView.setImageResource(R.drawable.challenger_lol)
                    else -> holder.divisionImageView.setImageResource(R.drawable.unranked_lol)
                }
            }
        }
            // Handle item click
            holder.itemView.setOnClickListener {
                onItemClicked(player)
            }
        }

        override fun getItemCount(): Int {
            return players.filter { it.username != currentUser }.size
        }
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tagTextView: TextView = view.findViewById(R.id.cardTag)
        val divisionImageView: ImageView = view.findViewById(R.id.cardImage)
        val winrateTextView: TextView = view.findViewById(R.id.cardWinrate)
        val serverTextView: TextView = view.findViewById(R.id.cardServer)
    }
    }
