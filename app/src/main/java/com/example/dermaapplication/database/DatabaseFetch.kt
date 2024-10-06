package com.example.dermaapplication.database

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.Exception

class DatabaseFetch {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance();

    /**
     * Metoda pobierająca dane z bazy danych Firebase.
     *
     * @return Lista nazw chorób znajdująca się w bazie danych.
     * @throws Exception przy nieudanym pobraniu danych.
     */
    fun fetchDiseasesNames(): Task<List<String>> {
        val task = firestore.collection("diseases").get()
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
                throw exception ?: Exception("Unknown error during database fetch")
            }
        }
    }

    fun fetchDisease(diseaseName: String): Task<List<String>> {
        val task = firestore.collection("diseases").get()
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
                throw exception ?: Exception("Unknown error during database fetch")
            }
        }
    }
}