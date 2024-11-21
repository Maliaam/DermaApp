package com.example.dermaapplication.items

import java.util.Date

/**
 * Klasa danych przedstawiająca wpis użytkownika do jego dziennika zmian skórnych.
 *
 * @param id identyfikator wpisu.
 * @param userUID identyfikator uzytkownika.
 * @param recordTitle tytuł wpisu.
 * @param date data utworzenia wpisu.
 * @param imageUrls zdjęcia zmiany skórnej dodanej przez użytkownika.
 * @param frontPins Przednie pinezki, które użytkownik zaznaczył w ankiecie.
 * @param backPins Tylne pinezki, które użytkownik zaznaczył w ankiecie.
 * @param surveyResponses Odpowiedzi do pytań udzielonych w ankiecie.
 * @param additionalNotes Dodatkowe informacje, które może wpisać użytkownik.
 */
data class JournalRecord(
    val id: Int,
    val userUID: String,
    val recordTitle: String,
    val date: Date,
    val imageUrls: List<String> = listOf(),
    val frontPins: List<Pair<Float, Float>>? = null,
    val backPins: List<Pair<Float, Float>>? = null,
    val surveyResponses: List<Pair<String, String>>? = null,
    val additionalNotes: String? = null
)