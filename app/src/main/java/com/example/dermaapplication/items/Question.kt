package com.example.dermaapplication.items

data class Question(
    val question: String = "",
    val answers: List<String> = emptyList()
)