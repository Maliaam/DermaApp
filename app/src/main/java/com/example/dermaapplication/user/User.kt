package com.example.dermaapplication.user

import android.app.Activity
import android.widget.Toast
import com.example.dermaapplication.Utilities
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class User {

    /**
     * Metoda rejestrująca użytkownika w bazie danych Firebase
     *
     * @param email
     * @param password
     * @param activity
     */
    fun registerUser(email: String, password: String, activity: Activity) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            Utilities.auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity) { task ->
                    if (task.isSuccessful) {
                        val firebaseUser: FirebaseUser? = task.result.user
                        firebaseUser?.let { user ->
                            val userData = hashMapOf(
                                "email" to email,
                                "password" to password,
                                "uid" to user.uid
                            )
                            Utilities.firestore.collection("users").add(userData)
                            Toast.makeText(
                                activity,
                                "Rejestracja powiodła się",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    } else {
                        Toast.makeText(
                            activity,
                            "Rejestracja nie powiodła się",
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                }
        }
    }

    /**
     * Metoda logująca użytkownika do aplikacji
     *
     * @param email
     * @param password
     * @param activity
     */
    fun loginUser(email: String, password: String, activity: Activity) {
        Utilities.auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        activity,
                        "Zalogowano",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        activity,
                        "Logowanie nie powiodło się",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
    }

    /**
     * Metoda wysyłająca maila do użytkownika. Pozwala na zresetowania hasła
     *
     * @param email
     * @param activity
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