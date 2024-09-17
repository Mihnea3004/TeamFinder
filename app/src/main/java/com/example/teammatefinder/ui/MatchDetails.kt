package com.example.teammatefinder.ui

data class MatchDetails(
    val gameId: String,
    val platformId: String,
    val gameCreation: Long,
    val gameDuration: Long,
    val participants: List<Participant>
)
