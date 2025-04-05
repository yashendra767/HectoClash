package com.example.hectoclash.dataclass



data class Question(
    val sequence: String = "",
    val solution: String = "",
    val operator_sequence: List<String> = emptyList()
)


