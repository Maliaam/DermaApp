package com.example.dermaapplication.database

import com.example.dermaapplication.Utilities
import com.example.dermaapplication.interfaces.AuthStateCallback
import com.google.firebase.auth.FirebaseAuth
import android.util.Log

class FirebaseAuthListener(private val callback: AuthStateCallback) {

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            Log.d("FirebaseAuthListener", "User logged in: ${currentUser.uid}")
            callback.onUserLoggedIn()
        } else {
            Log.d("FirebaseAuthListener", "User logged out")
            callback.onUserLoggedOut()
        }
    }

    fun attachListener() {
        Utilities.auth.addAuthStateListener(authStateListener)
        Log.d("FirebaseAuthListener", "AuthStateListener attached")
    }

    fun detachListener() {
        Utilities.auth.removeAuthStateListener(authStateListener)
        Log.d("FirebaseAuthListener", "AuthStateListener detached")
    }
}
