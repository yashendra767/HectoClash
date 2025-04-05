package com.example.hectoclash.dataclass

data class GameData(
    val player1: String? = "",
    val player2: String? = "",
    val status: String? = "",
    val startTime : Long? = null,
    val question: Question? = Question()

)
