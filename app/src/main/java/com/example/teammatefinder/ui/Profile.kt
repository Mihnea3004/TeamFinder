package com.example.teammatefinder.ui

import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.teammatefinder.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Profile : AppCompatActivity() {

    private lateinit var riotApiService: RiotApiService
    private lateinit var riotApiService2: RiotApiService
    private val apiKEY = "put api key here"
    private var baseURLServer = "https://eun1.api.riotgames.com"
    private var baseURLREGION = "https://europe.api.riotgames.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        val databaseHelper = DatabaseHelper(this)
        val username = intent.getStringExtra("username") ?: return
        val selectedOption = intent.getStringExtra("selectedOption") ?: return
        val serverView: TextView = findViewById(R.id.profile_server)
        val headerView: TextView = findViewById(R.id.profile_text)
        val winrateView: TextView = findViewById(R.id.winrate_profile)
        val profileRankView: TextView = findViewById(R.id.profile_rank)

        val retrofit = RetrofitClient.getClient(baseURLREGION)
        riotApiService = retrofit.create(RiotApiService::class.java)
        val retrofit2 = RetrofitClient.getClient(baseURLServer)
        riotApiService2 = retrofit2.create(RiotApiService::class.java)

        baseURLServer = getRiotURL(getServer(username, "League of Legends"))
        baseURLREGION = getRiotRegion(getServer(username, "League of Legends"))

        val cursor = databaseHelper.retrieveDataUser(username, "LolPlayers")
        if (cursor!!.moveToFirst()) {
            val summonerName = cursor.getString(1)
            val lolServer = cursor.getString(3)
            val winrate = cursor.getString(4)
            val rank = cursor.getString(2).toString()
            Log.d("Profile", "Rank: $rank")
            val rankNew = cursor.getString(2).split(" ")[0]
            when (selectedOption) {
                "League of Legends" -> {
                    "League of Legends".also { headerView.text = it }
                    for (i in summonerName.indices) {
                        if (summonerName[i] == '#') {
                            val tag = summonerName.substring(i + 1, summonerName.length)
                            val gameName = summonerName.substring(0, i)
                            lolfetchAccountByRiotId(gameName, tag)
                        }
                    }
                    serverView.text = lolServer
                    "${winrate.substring(0,5)}%".also { winrateView.text = it }
                    profileRankView.text = rank
                    updateRankView(rankNew)

                }

                "Valorant" -> {
                    // Add logic for Valorant if needed
                }

                "TFT" -> {
                    // Add logic for TFT if needed
                }
            }

        } else {
            Log.e("Profile", "No data found for the given username")
        }
        cursor.close()
    }

    private fun lolfetchAccountByRiotId(gameName: String, tagLine: String) {
        riotApiService.getAccountByRiotId(gameName, tagLine, apiKEY)
            .enqueue(object : Callback<RiotAccount> {
                override fun onResponse(call: Call<RiotAccount>, response: Response<RiotAccount>) {
                    if (response.isSuccessful) {
                        val account = response.body()
                        val puuid = account?.puuid
                        puuid?.let {
                            lolfetchMatchIdsByPuuid(it)
                        }
                    } else {
                        Log.e("Profile", "Error: ${response.errorBody()}")
                    }
                }

                override fun onFailure(call: Call<RiotAccount>, t: Throwable) {
                    Log.e("Profile", "Failure: ${t.message}")
                }
            })
    }

    private fun updateRankView(rank: String) {
        val rankView: ImageView = findViewById(R.id.profile_image_rank)
        val rankText: TextView = findViewById(R.id.profile_rank)

        val rankImageRes = when (rank) {
            "IRON" -> R.drawable.iron_lol
            "BRONZE" -> R.drawable.bronze_lol
            "SILVER" -> R.drawable.silver_lol
            "GOLD" -> R.drawable.gold_lol
            "PLATINUM" -> R.drawable.platinum_lol
            "DIAMOND" -> R.drawable.diamond_lol
            "MASTER" -> R.drawable.master_lol
            "GRANDMASTER" -> R.drawable.grandmaster_lol
            "CHALLENGER" -> R.drawable.challenger_lol
            else -> R.drawable.unranked_lol
        }

        // Log rank and check if it's being set correctly
        Log.d("Profile", "Updating rank image and text for rank: $rank")

        runOnUiThread {
            rankView.setImageResource(rankImageRes)
            rankText.text = rank
        }
    }



    // Helper function to get rank value
  /*  private fun getRankValue(tier: String): Int {
        return when (tier) {
            "UNRANKED" -> 0
            "IRON" -> 1
            "BRONZE" -> 2
            "SILVER" -> 3
            "GOLD" -> 4
            "PLATINUM" -> 5
            "DIAMOND" -> 6
            "MASTER" -> 7
            "GRANDMASTER" -> 8
            "CHALLENGER" -> 9
            else -> 0
        }
    } */

    private fun lolfetchMatchIdsByPuuid(puuid: String) {
        riotApiService2.getMatchIdsByPuuid(puuid, 0, 10, apiKEY)
            .enqueue(object : Callback<List<String>> {
                override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                    if (response.isSuccessful) {
                        val matchIds = response.body()
                        matchIds?.let {
                            if (it.isNotEmpty()) {
                                lolfetchMatchDetailsByMatchId(it[0])
                            }
                        }
                    } else {
                        Log.e("Profile", "Error: ${response.errorBody()}")
                    }
                }

                override fun onFailure(call: Call<List<String>>, t: Throwable) {
                    Log.e("Profile", "Failure: ${t.message}")
                }
            })
    }

    private fun lolfetchMatchDetailsByMatchId(matchId: String) {
        riotApiService2.getMatchDetailsByMatchId(matchId, apiKEY)
            .enqueue(object : Callback<MatchDetails> {
                override fun onResponse(call: Call<MatchDetails>, response: Response<MatchDetails>) {
                    if (response.isSuccessful) {
                        val matchDetails = response.body()
                        matchDetails?.let {
                            // Handle match details (e.g., display them in UI)
                        }
                    } else {
                        Log.e("Profile", "Error: ${response.errorBody()}")
                    }
                }

                override fun onFailure(call: Call<MatchDetails>, t: Throwable) {
                    Log.e("Profile", "Failure: ${t.message}")
                }
            })
    }
    private fun getServer(username: String, game: String): String {
        val databaseHelper = DatabaseHelper(this)
        val cursor: Cursor
        when (game) {
            "League of Legends" -> {
                cursor = databaseHelper.retrieveDataUser(username, "LolPlayers")!!
                cursor.use {
                    if (it.moveToFirst()) {
                        return it.getString(3).toString()
                    }
                }
            }

            "Valorant" -> {
                cursor = databaseHelper.retrieveDataUser(username, "ValorantPlayers")!!
                cursor.use {
                    if (it.moveToFirst()) {
                        return it.getString(3).toString()
                    }
                }
            }

            "TFT" -> {
                cursor = databaseHelper.retrieveDataUser(username, "TFTPlayers")!!
                cursor.use {
                    if (it.moveToFirst()) {
                        return it.getString(3).toString()
                    }
                }
            }

            else -> throw IllegalArgumentException("Unsupported game type")
        }
        return "N/A"
    }
    private fun getRiotURL(region: String): String {
        when(region){
            "BR" -> return "https://br1.api.riotgames.com"
            "EUNE" -> return "https://eun1.api.riotgames.com"
            "EUW" -> return "https://euw1.api.riotgames.com"
            "JP" -> return "https://jp1.api.riotgames.com"
            "KR" -> return "https://kr.api.riotgames.com"
            "LAN" -> return "https://la1.api.riotgames.com"
            "LAS" -> return "https://la2.api.riotgames.com"
            "NA" -> return "https://na1.api.riotgames.com"
            "OCE" -> return "https://oc1.api.riotgames.com"
            "RU" -> return "https://ru.api.riotgames.com"
            "TR" -> return "https://tr1.api.riotgames.com"
            "PH" -> return "https://ph2.api.riotgames.com"
            "SG" -> return "https://sg2.api.riotgames.com"
            "TH" -> return "https://th2.api.riotgames.com"
            "TW" -> return "https://tw2.api.riotgames.com"
            "VN" -> return "https://vn2.api.riotgames.com"
            else -> return "https://eun1.api.riotgames.com"
        }
    }
    private fun getRiotRegion(region: String): String {
        when(region){
            "BR" -> return "https://americas.api.riotgames.com"
            "EUNE" -> return "https://europe.api.riotgames.com"
            "EUW" -> return "https://europe.api.riotgames.com"
            "JP" -> return "https://asia.api.riotgames.com"
            "KR" -> return "https://asia.api.riotgames.com"
            "LAN" -> return "https://americas.api.riotgames.com"
            "LAS" -> return "https://americas.api.riotgames.com"
            "NA" -> return "https://americas.api.riotgames.com"
            "OCE" -> return "https://asia.api.riotgames.com"
            "RU" -> return "https://europe.api.riotgames.com"
            "TR" -> return "https://europe.api.riotgames.com"
            "PH" -> return "https://sea.api.riotgames.com"
            "SG" -> return "https://sea.api.riotgames.com"
            "TH" -> return "https://sea.api.riotgames.com"
            "TW" -> return "https://sea.api.riotgames.com"
            "VN" -> return "https://sea.api.riotgames.com"
            else -> return "https://europe.api.riotgames.com"
        }
    }
}
