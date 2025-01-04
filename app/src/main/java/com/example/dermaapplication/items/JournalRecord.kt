package com.example.dermaapplication.items

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

/**
 * Klasa danych przedstawiająca wpis użytkownika do jego dziennika zmian skórnych.
 *
 * @param userUID identyfikator uzytkownika.
 * @param recordTitle tytuł wpisu.
 * @param date data utworzenia wpisu.
 * @param imageUrls zdjęcia zmiany skórnej dodanej przez użytkownika.
 * @param frontPins Przednie pinezki, które użytkownik zaznaczył w ankiecie.
 * @param backPins Tylne pinezki, które użytkownik zaznaczył w ankiecie.
 * @param surveyResponses Odpowiedzi do pytań udzielonych w ankiecie.
 * @param additionalNotes Dodatkowe informacje, które może wpisać użytkownik.
 * @param documentId Identyfikator ustalony przez Firebase.
 */
data class JournalRecord(
    val userUID: String,
    val recordTitle: String,
    val date: String,
    val imageUrls: MutableList<String> = mutableListOf(),
    val frontPins: List<Pin>? = null,
    val backPins: List<Pin>? = null,
    val surveyResponses: List<Survey>? = null,
    var additionalNotes: List<Note>? = null,
    var documentId: String?,
    val doctorName: String? = null,
    val doctorUid: String? = null
) : Serializable