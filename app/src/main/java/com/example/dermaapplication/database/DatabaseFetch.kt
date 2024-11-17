package com.example.dermaapplication.database

import android.net.Uri
import android.util.Log
import com.example.dermaapplication.Utilities
import com.example.dermaapplication.items.Disease
import com.example.dermaapplication.items.Question
import com.google.android.gms.tasks.Task
import java.util.UUID

/**
 * Klasa odpowiedzalna za komunikację z bazą danych Firebase.
 */
class DatabaseFetch {

    /**
     * Pobiera listę unikalnych nazw chorób w kolekcji "diseases" z Firebase.
     *
     * @return Task<List<String>> Zwraca listę nazw chorób.
     * @throws:  Exception W przypadku nieudanego pobierania danych zostaje wyrzucony wyjątek.
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
                throw exception ?: Exception("Unknown error during fetch")
            }
        }
    }

    /**
     * Pobiera szczegółowe informacje na temat danej choroby.
     *
     * @param diseaseName Nazwa choroby, której informacje mają zostać pobrane z bazy danych.
     * @return Task<List<String>> Zwraca znalezione informacje na temat danej choroby.
     */
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
                throw exception ?: Exception("Unknown error during fetch")
            }
        }
    }

    /**
     * Pobiera informacje o wszystkich chorobach znajdujących się w bazie danych Firebase, a
     * następnie przekazuje ją do funkcji callback.
     *
     * @param callback Funkcja zwrotna, która otrzyma listę obiektów Disease.
     */
    fun fetchDiseases(callback: (List<Disease>) -> Unit) {
        Utilities.firestore.collection("diseases").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val documents = task.result!!.documents
                val diseasesList = mutableListOf<Disease>()

                for (document in documents) {
                    val diseaseName = document.getString("name") ?: "Brak nazwy"
                    val diseaseDescription = document.getString("description") ?: "Brak opisu"
                    diseasesList.add(Disease(diseaseName, diseaseDescription))
                }
                callback(diseasesList)
            } else {
                callback(emptyList())
            }
        }
    }

    /**
     * Dodaje zdjęcie wybrane przez użytkownika do FirebaseStorage.
     *
     * @param imageUri uniform resource identifier ( uri wybranego zdjęcia).
     */
    fun uploadUserImage(imageUri: Uri) {
        val userUID = Utilities.getCurrentUserUid()
        val userImageRef = Utilities.storage.child("users/$userUID/images/${UUID.randomUUID()}.jpg")

        userImageRef.putFile(imageUri).addOnSuccessListener { task ->
            userImageRef.downloadUrl.addOnSuccessListener { uri ->
                val downloadUrl = uri.toString()
                Log.d("FirebaseStorage", "Image uploaded successfully. URL: $downloadUrl")
            }
        }.addOnFailureListener { exception ->
            Log.e("FirebaseStorage", "Image upload failed", exception)
        }
    }

    /**
     * Pobiera informacje o pytaniach i odpowiedziach znajdujących się w bazie danych Firebase.
     * Następnie parsuje odpowiedzi po przecinku i zwraca callback obiektów Question.
     *
     * @param callback Funkcja zwrotna, która otrzyma listę obiektów Question.
     */
    fun fetchQuestions(callback: (List<Question>) -> Unit) {
        Utilities.firestore.collection("questions").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val questions = task.result?.documents?.mapNotNull { document ->
                    val id = document.getString("id")?.toInt()
                    val questionText = document.getString("questionText")
                    val answersString = document.getString("answers")
                    val theme = document.getString("theme")
                    val nextQuestion = document.getString("nextQuestion")?.toInt()
                    val expectedAnswer = document.getString("expectedAnswer")
                    val additionalInfo = document.getString("additionInfo")

                    if ( id != null && questionText != null && answersString != null && theme != null) {
                        val answers = answersString.split(",").map { it.trim() }
                        Question(id,questionText, answers,theme,nextQuestion,expectedAnswer,additionalInfo)
                    } else {
                        null
                    }
                } ?: emptyList()

                callback(questions)
            } else {
                callback(emptyList())
            }
        }
    }

}