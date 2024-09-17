package com.example.teammatefinder.ui

data class LeagueEntry(val leagueid: String, val summonerId: String, val queueType: String, val tier: String, val rank: String, val leaguePoints: Int, val wins: Int, val losses: Int, val hotStreak: Boolean, val veteran: Boolean, val freshBlood: Boolean, val inactive: Boolean, val miniSeries: MiniSeries)
