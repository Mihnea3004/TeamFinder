package com.example.teammatefinder.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
            .inflate(R.layout.item_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val player = players[position]

        // Bind data to views
        holder.tagTextView.text = player.tag
        holder.winrateTextView.text = player.winrate.toString()
        holder.serverTextView.text = player.server
        when (game) {
            "League of Legends" -> {
                when (player.division) {
                    "Iron" -> holder.divisionImageView.setImageResource(R.drawable.iron_lol)
                    "Bronze" -> holder.divisionImageView.setImageResource(R.drawable.bronze_lol)
                    "Silver" -> holder.divisionImageView.setImageResource(R.drawable.silver_lol)
                    "Gold" -> holder.divisionImageView.setImageResource(R.drawable.gold_lol)
                    "Platinum" -> holder.divisionImageView.setImageResource(R.drawable.platinum_lol)
                    "Diamond" -> holder.divisionImageView.setImageResource(R.drawable.diamond_lol)
                    "Master" -> holder.divisionImageView.setImageResource(R.drawable.master_lol)
                    "Grandmaster" -> holder.divisionImageView.setImageResource(R.drawable.grandmaster_lol)
                    "Challenger" -> holder.divisionImageView.setImageResource(R.drawable.challenger_lol)
                    else -> holder.divisionImageView.setImageResource(R.drawable.unranked_lol)
                }
            }
            "Valorant" -> {
             //   when (player.division) {

             //   }
            }
            "TFT" -> {
                when (player.division) {
                    "Iron" -> holder.divisionImageView.setImageResource(R.drawable.iron_lol)
                    "Bronze" -> holder.divisionImageView.setImageResource(R.drawable.bronze_lol)
                    "Silver" -> holder.divisionImageView.setImageResource(R.drawable.silver_lol)
                    "Gold" -> holder.divisionImageView.setImageResource(R.drawable.gold_lol)
                    "Platinum" -> holder.divisionImageView.setImageResource(R.drawable.platinum_lol)
                    "Diamond" -> holder.divisionImageView.setImageResource(R.drawable.diamond_lol)
                    "Master" -> holder.divisionImageView.setImageResource(R.drawable.master_lol)
                    "Grandmaster" -> holder.divisionImageView.setImageResource(R.drawable.grandmaster_lol)
                    "Challenger" -> holder.divisionImageView.setImageResource(R.drawable.challenger_lol)
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
        val tagTextView: TextView = view.findViewById(R.id.tagView)
        val divisionImageView: ImageView = view.findViewById(R.id.divisionView)
        val winrateTextView: TextView = view.findViewById(R.id.winrateView)
        val serverTextView: TextView = view.findViewById(R.id.serverView)
    }
    }
