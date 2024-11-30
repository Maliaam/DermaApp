package com.example.dermaapplication.user

import android.app.Activity
import android.widget.Toast
import com.example.dermaapplication.MainActivity
import com.example.dermaapplication.Utilities
import com.example.dermaapplication.fragments.UserFeedFragment
import com.google.firebase.auth.FirebaseUser

/**
 * Klasa umożliwiająca zarządzaniem konta użytkownika.
 */
class User {

    /**
     * Rejestruje użytkownika w bazie danych Firebase za pomocą podanego e-maila, hasła oraz
     * danych dodatkowych takich jak imię, nazwisko. Przypisuje unikalny identyfikator UID użytkownika.
     *
     * @param email E-mail użytkownika.
     * @param password Hasło użytkownika.
     * @param name Imię użytkownika.
     * @param surname Nazwisko użytkownika.
     * @param collection Nazwa kolekcji w bazie danych.
     * @param activity Aktywność, w której wywołano logowanie.
     */
    fun registerUser(
        email: String,
        password: String,
        name: String,
        surname: String,
        collection: String,
        activity: Activity
    ) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            Utilities.auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity) { task ->
                    if (task.isSuccessful) {
                        val firebaseUser: FirebaseUser? = task.result.user
                        firebaseUser?.let { user ->

                            val newUserData = hashMapOf(
                                "email" to email,
                                "password" to password,
                                "name" to name,
                                "surname" to surname,
                                "uid" to user.uid
                            )
                            Utilities.firestore.collection(collection).add(newUserData)
                            Toast.makeText(activity, "Rejestracja powiodła się", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        Toast.makeText(activity, "Rejestracja nie powiodła się", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
        }
    }

    /**
     * Loguje użytkownika do aplikacji poprzez Firebase za pomocą podanych danych uwierzytelniających.
     * Po pomyślnym zalogowaniu następuje przekierowanie do fragmentu UserFeedFragment
     *
     * @param email E-mail użytkownika.
     * @param password Hasło użytkownika.
     * @param activity Aktywność, w której wywołano logowanie
     * @param callback Funkcja, która przyjmuje parametr Boolean, wskazująca czy logowanie jest pomyślne
     */
    fun loginUser(
        email: String,
        password: String,
        activity: Activity,
        callback: (Boolean) -> Unit
    ) {
        Utilities.auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(activity, "Zalogowano", Toast.LENGTH_SHORT).show()
                    (activity as? MainActivity)?.replaceFragment(UserFeedFragment())
                    callback(true)
                } else {
                    Toast.makeText(
                        activity, "Logowanie nie powiodło się", Toast.LENGTH_SHORT
                    ).show()
                    callback(false)
                }
            }
    }

    /**
     * Wysyła e-mail do użytkownika z linkiem do zresetowania hasła.
     *
     * @param email E-mail użytkownika, na który zostanie wysłany link do zresetowania hasła.
     * @param activity Aktywność, w której wywołano metodę.
     */
    fun resetPassword(email: String, activity: Activity) {
        if (email.isNotEmpty()) {
            Utilities.auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        activity,
                        "Wysłano maila do zresetowania hasła",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }
    }
}