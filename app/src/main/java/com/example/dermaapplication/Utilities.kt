package com.example.dermaapplication

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import com.example.dermaapplication.user.User

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class Utilities {

    @SuppressLint("StaticFieldLeak")
    companion object {
        val firestore = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val user = User()

        /**
         * Metoda sprawdzająca UID użytkownika
         *
         * @return UID użytkownika (String | null)
         */
        fun getCurrentUserUid(): String? {
            return auth.currentUser?.uid
        }


    }
}