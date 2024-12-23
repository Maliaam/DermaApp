package com.example.dermaapplication.items

/**
 * Klasa przedstawiająca informacje o użytkowniku.
 *
 * @property email E-mail użytkownika, wykorzystywane do logowania.
 * @property password Hasło użytkownika, wykorzystywane do logowania.
 * @property name Imię użytkownika, wykorzystywane do wiadomości.
 * @property surname Nazwisko użytkownika, wykorzystywana do wiadomości.
 * @property uid Identyfikator UID użytkownika.
 * @property profileImageUrl URL zdjęcia profilowego użytkownika.
 */
data class Users(
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val surname: String = "",
    val uid: String = "",
    val profileImageUrl: String = ""
)
