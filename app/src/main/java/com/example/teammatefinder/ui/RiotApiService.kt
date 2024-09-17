package com.example.teammatefinder.ui


import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

data class Summoner(val id: String, val accountid: String, val puuid: String, val profileiconid: Int, val revisionDate: Long, val summonerLevel: Int)
data class MiniSeries(val losses: Int, val progress: String, val target: Int, val wins: Int)


interface RiotApiService {
    // 1. Fetch Riot account by Riot ID
    @GET("/riot/account/v1/accounts/by-riot-id/{gameName}/{tagLine}")
    fun getAccountByRiotId(
        @Path("gameName") gameName: String,
        @Path("tagLine") tagLine: String,
        @Query("api_key") apiKey: String
    ): Call<RiotAccount>

    // 2. Fetch match IDs by PUUID
    @GET("/lol/match/v5/matches/by-puuid/{puuid}/ids")
    fun getMatchIdsByPuuid(
        @Path("puuid") puuid: String,
        @Query("start") start: Int,
        @Query("count") count: Int,
        @Query("api_key") apiKey: String
    ): Call<List<String>>

    // 3. Fetch match details by matchId
    @GET("/lol/match/v5/matches/{matchId}")
    fun getMatchDetailsByMatchId(
        @Path("matchId") matchId: String,
        @Query("api_key") apiKey: String
    ): Call<MatchDetails>
    @GET("/lol/summoner/v4/summoners/by-puuid/{encryptedPUUID}")
    fun getSummonerByPUUID(
        @Path("encryptedPUUID") encryptedPUUID: String,
        @Query("api_key") apiKey: String
    ): Call<Summoner>

    @GET("/lol/league/v4/entries/by-summoner/{encryptedSummonerId}")
    fun getLeagueEntriesBySummonerId(
        @Path("encryptedSummonerId") encryptedSummonerId: String,
        @Query("api_key") apiKey: String
    ): Call<List<LeagueEntry>>


}