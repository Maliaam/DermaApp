package com.example.dermaapplication

import android.annotation.SuppressLint
import android.util.Log
import com.example.dermaapplication.database.DatabaseFetch
import com.example.dermaapplication.user.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Klasa Utilities zawiera metody pomocniczne oraz zmienne globalne, które są używane w aplikacji.
 */
class Utilities {

    @SuppressLint("StaticFieldLeak")
    companion object {
        /**
         * Zmienna globalna przechowująca informację o statusie użytkownika (czy jest doktorem).
         * Jest ustawiana przy logowaniu aby uniknąć wielokrotnych zapytań do bazy danych.
         */
        var isUserDoctor: Boolean? = null

        /** Referencja do instancji Firestore */
        val firestore = FirebaseFirestore.getInstance()

        /** Referencja do instancji FirebaseAuth */
        val auth = FirebaseAuth.getInstance()

        /** Referencja do instancji FirebaseStorage */
        val storage = FirebaseStorage.getInstance().reference

        /** Instancja klasy DatabaseFetch do wymiany danych z bazą */
        val databaseFetch = DatabaseFetch()

        /** Instancja klasy użytkownika */ //TODO: Przemyśleć jej użyteczność
        val user = User()

        /** UID aktualnie zalogowanego użytkownika */
        private var userUID: String = ""


        /**
         * Pobiera UID aktualnie zalogowanego użytkownika. Jeżeli użytkownik jest zalogowany to
         * zwraca jego UID w przeciwnym razie zwraca pusty ciąg znaków.
         *
         * @return String reprezentujący UID użytkownika.
         */
        fun getCurrentUserUid(): String {
            if (auth.currentUser != null) {
                userUID = auth.currentUser!!.uid
            }
            return userUID
        }

        /**
         * Inicjalizuje status użytkownika jako doktora lub zwykłego użytkownika.
         *
         * @param callback callback, Funkcja, która jest wywoływana po ustawieniu statusu, przyjmuje
         * parametr Boolean wskazujący czy dany użytkownik jest doktorem.
         */
        fun initializeUserStatus(callback: (Boolean) -> Unit) {
            checkIfUserIsDoctor { isDoctor ->
                isUserDoctor = isDoctor
                callback(isDoctor)
            }
        }

        /**
         * Pobiera imię i nazwisko aktualnie zalogowanego użytkownika z bazy danych Firebase.
         * Dane są pobierane z kolekcji "doctors" lub "users" na podstawie zmiennej isUserDoctor.
         *
         * @param callback, Funkcja, która jest wywoływana z pełnym imieniem i nazwiskiem użytkownika
         */
        fun getCurrentUserName(callback: (String?) -> Unit) {
            val collection = if (isUserDoctor == true) "doctors" else "users"
            val userUID = getCurrentUserUid()

            firestore.collection(collection)
                .whereEqualTo("uid", userUID)
                .get()
                .addOnSuccessListener { value ->
                    if (!value.isEmpty) {
                        val document = value.documents[0]
                        val name = document.getString("name") ?: ""
                        val surname = document.getString("surname") ?: ""
                        val fullName = "$name $surname"
                        Log.e("CurrentUserName",fullName)
                        callback(fullName)
                    }
                }
        }

        /**
         * Pobiera aktualny czas z systemu, na którym używana jest aplikacja.
         * Dane są pobierane w formacie "yyyy-MM-dd HH:mm:ss"
         * Wykorzystywana do znaczników ("timestamp") wysłanych wiadomości.
         */
        @SuppressLint("SimpleDateFormat")
        fun getCurrentTime(): String {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = Date(System.currentTimeMillis())

            return format.format(date)
        }

        /**
         * Tworzy unikalny identyfikator czatu, na podstawie UID nadawcy oraz UID odbierającego.
         * Unikalny identyfikator jest tworzony poprzez konkatencje dwóch stringów.
         *
         * @param senderUID String reprezentujący UID nadawcy.
         * @param receiverUID String reprezentujący UID odbiorcy.
         * @return String reprezentujący unikalny identyfikator czatu.
         */
        fun createUniqueChatId(senderUID: String, receiverUID: String): String {
            return senderUID + receiverUID
        }

        /**
         * Sprawdza czy aktualnie zalogowany użytkownik jest lekarzem.
         * Wyszukuje id wszystkich lekarzy w bazie danych, a następnie tworzy z nich liste.
         * Wykorzystuje UID aktualnie zalogowanego użytkownika do porównania z listą UID wszystkich
         * lekarzy.
         *
         * @param callback Funkcja przyjmująca parametr Boolean, wskazujący czy użytkownik jest
         * lekarzem. Zwraca true jeśli jest lekarzem, false jeśli nie jest.
         */
        private fun checkIfUserIsDoctor(callback: (Boolean) -> Unit) {
            val userUID = getCurrentUserUid()
            firestore.collection("doctors")
                .get()
                .addOnSuccessListener { value ->
                    val isDoctor = value.documents.any { document ->
                        document.getString("uid") == userUID
                    }
                    callback(isDoctor)
                }.addOnFailureListener {
                    callback(false)
                }
        }
    }
}