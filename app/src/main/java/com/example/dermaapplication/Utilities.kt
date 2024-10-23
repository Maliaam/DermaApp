package com.example.dermaapplication

import android.annotation.SuppressLint
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Utilities {

    @SuppressLint("StaticFieldLeak")
    companion object {
        val firestore = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        private var userId = ""

        fun getCurrentUserUid(): String {
            if (auth.currentUser != null) {
                userId = auth.currentUser!!.uid
            }
            return userId
        }
    }
}