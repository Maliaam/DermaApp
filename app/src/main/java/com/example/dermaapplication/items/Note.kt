package com.example.dermaapplication.items

/**
 * Klasa danych reprezentująca notatkę wpisaną przez użytkownika w ramach wpisu do dziennika.
 *
 * @param date data notatki.
 * @param content treść notatki.
 */
data class Note(
    val date: String,
    val content: String
)