package com.example.dermaapplication.items

/**
 * Klasa reprezentująca pytanie w ankiecie.
 *
 * @property question tekst pytania.
 * @property answers lista odpowiedzi.
 */
data class Question(
    val question: String,
    val answers: List<String>
)