package com.example.dermaapplication.items

/**
 * Klasa reprezentująca pytanie w ankiecie.
 *
 * @property id identyfikator pytania.
 * @property question tekst pytania.
 * @property answers lista odpowiedzi na to pytanie.
 * @property theme temat pytania.
 * @property nextQuestion zależność od innego pytania.
 * @property expectedAnswer oczekiwana odpowiedź.
 * @property additionalInfo dodatkowe informacje dla użytkownika.
 */
data class Question(
    val id: Int,
    val question: String,
    val answers: List<String>,
    val theme: String,
    val nextQuestion: Int? = null,
    val expectedAnswer: String? = null,
    val additionalInfo: String? = null
)