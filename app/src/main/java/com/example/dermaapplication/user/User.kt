package com.example.dermaapplication.user

import android.app.Activity
import android.widget.Toast
import com.example.dermaapplication.Utilities
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class User {

    /**
     * Metoda rejestrująca użytkownika w bazie danych Firebase
     *
     * @param login
     * @param email
     * @param password
     * @param activity
     */
    fun registerUser(
        login: String,
        email: String,
        password: String,
        activity: Activity
    ) {
        if (login.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
            Utilities.auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity) { task ->
                    if (task.isSuccessful) {
                        val database = FirebaseFirestore.getInstance()
                        val userId = generateId()
                        val user = hashMapOf(
                            "email" to email,
                            "login" to login,
                            "password" to password,
                            "id" to userId
                        )
                        database.collection("users").add(user)
                    } else {
                        Toast.makeText(activity, "Login failed", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    fun loginUser(email: String, password: String, activity: Activity) {
        Utilities.auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(activity, "Successful login", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(activity, "Login failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Metoda generująca losowe id
     * @return String id
     */
    //TODO: PASSWORD RESET
    private fun resetPassword(){

    }

    private fun generateId(): String {
        return UUID.randomUUID().toString()
    }
    fun getUserId(){

    }

}