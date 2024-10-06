package com.example.dermaapplication.user

import android.app.Activity
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class User {
    private val userConnection: FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * Metoda rejestrująca użytkownika w bazie danych Firebase
     *
     * @param login
     * @param email
     * @param password
     * @param activity
     */
    fun registerUser(login: String, email: String, password: String, activity: Activity) {
        if (login.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
            userConnection.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity) { task ->
                    if (task.isSuccessful) {
                        val database = FirebaseFirestore.getInstance()
                        val user = hashMapOf(
                            "email" to email,
                            "login" to login,
                            "password" to password
                        )
                        database.collection("users").add(user)
                    } else {
                        Toast.makeText(activity, "Login failed", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    // TODO: Metoda do logowania użytwkonika
    fun loginUser(login: String, password: String) {

    }
}