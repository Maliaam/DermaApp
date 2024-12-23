package com.example.dermaapplication.items

data class Survey(
    val title: String,
    val date: String,
    val items: List<SurveyItem>
)
