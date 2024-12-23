package com.example.dermaapplication.items

/**
 * Klasa danych reprezentująca odpowiedź użytkownika na pytanie w ankiecie.
 *
 * @param question treść pytania z ankiety.
 * @param answer odpowiedź użytkownika na pytanie.
 */
data class SurveyItem(
    val question: String,
    val answer: String
)
