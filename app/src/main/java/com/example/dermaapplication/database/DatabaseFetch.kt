package com.example.dermaapplication.database

import android.util.Log
import com.example.dermaapplication.Utilities
import com.example.dermaapplication.items.Message
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore

class DatabaseFetch {

    /**
     * Metoda pobierająca dane z bazy danych Firebase.
     *
     * @return Lista nazw chorób znajdująca się w bazie danych.
     * @throws: Exception przy nieudanym pobieraniu danych.
     */
    fun fetchDiseasesNames(): Task<List<String>> {
        val task = Utilities.firestore.collection("diseases").get()
        return task.continueWith { task1 ->
            if (task1.isSuccessful) {
                val documents = task1.result!!.documents
                val diseasesNames = mutableListOf<String>()

                for (document in documents) {
                    val diseaseName = document.getString("name")
                    if (diseaseName != null && !diseasesNames.contains(diseaseName)) {
                        diseasesNames.add(diseaseName)
                    }
                }
                diseasesNames
            } else {
                val exception = task1.exception
                Log.e("DatabaseFetch", "Error during database fetch", exception)
                throw exception ?: Exception("Unknown error during fetch")
            }
        }
    }

    fun fetchDisease(diseaseName: String): Task<List<String>> {
        val task = Utilities.firestore.collection("diseases").get()
        return task.continueWith { task1 ->
            if (task1.isSuccessful) {
                val documents = task1.result!!.documents
                val diseaseFetch = mutableListOf<String>()

                for (document in documents) {
                    val databaseName = document.getString("name")
                    if (diseaseName == databaseName) {
                        val description = document.getString("description")
                        val specialists = document.getString("specialist")
                        val symptoms = document.get("symptoms").toString()

                        if (description != null) diseaseFetch.add(description)
                        if (specialists != null) diseaseFetch.add(specialists)
                        diseaseFetch.add(symptoms)
                    }
                }
                diseaseFetch
            } else {
                val exception = task1.exception
                Log.e("DatabaseFetch", "Error during database fetch", exception)
                throw exception ?: Exception("Unknown error during fetch")
            }
        }
    }

    fun fetchMessages(userId: String): Task<List<Message>> {
        val task = Utilities.firestore.collection("messages").get()
        return task.continueWith { task ->
            if (task.isSuccessful) {
                val documents = task.result!!.documents
                val messages = mutableListOf<Message>()
                for (document in documents) {
                    val databaseId = document.getString("receiverId")
                    if (userId == databaseId) {
                        val senderId = document.getString("senderId") ?: ""
                        val senderName = document.getString("senderName") ?: "Unknown sender"
                        val receiverId = document.getString("receiverId") ?: ""
                        val messageText = document.getString("messageText") ?: "No message"
                        val timestamp = document.getString("timestamp") ?: "21:37"

                        val message = Message(
                            senderId = senderId,
                            senderName = senderName,
                            receiverId = receiverId,
                            messageText = messageText,
                            timestamp = timestamp
                        )
                        messages.add(message)
                    }
                }
                messages
            } else {
                val exception = task.exception
                Log.e("DatabaseFetch", "Error during database fetch - messages", exception)
                throw exception ?: Exception("Error during fetch")
            }
        }
    }


}
