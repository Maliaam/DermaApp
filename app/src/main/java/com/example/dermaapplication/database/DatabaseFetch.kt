package com.example.dermaapplication.database

import android.net.Uri
import android.util.Log
import com.example.dermaapplication.Utilities
import com.example.dermaapplication.items.Disease
import com.example.dermaapplication.items.JournalRecord
import com.example.dermaapplication.items.Message
import com.example.dermaapplication.items.Note
import com.example.dermaapplication.items.Pin
import com.example.dermaapplication.items.Question
import com.example.dermaapplication.items.SurveyResponse
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
     * @param callback Funkcja zwrotna, która otrzyma listę informacji o chorobie lub null w przypadku błędu.
     */
    fun fetchDisease(diseaseName: String, callback: (Disease?) -> Unit) {
        Utilities.firestore.collection("diseases").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val documents = task.result!!.documents
                for (document in documents) {
                    val databaseName = document.getString("name")
                    if (diseaseName == databaseName) {
                        val description = document.getString("description")
//                        val specialists = document.getString("specialist")
                        val symptoms = document.get("symptoms") as? List<String> ?: emptyList()
                        val images = document.get("images") as? List<String> ?: emptyList()

                        val diseaseDetails = Disease(
                            databaseName,
                            description = description!!,
                            symptoms = symptoms,
                            images = images
                        )
                        callback(diseaseDetails)
                        return@addOnCompleteListener
                    }
                }
                callback(null)
            } else {
                callback(null)
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
                    val diseaseSymptoms = document.get("symptoms") as? List<String> ?: emptyList()
                    val diseaseImages = document.get("images") as? List<String> ?: emptyList()
                    diseasesList.add(
                        Disease(
                            diseaseName,
                            diseaseDescription,
                            diseaseSymptoms,
                            diseaseImages
                        )
                    )
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

                    if (id != null && questionText != null && answersString != null && theme != null) {
                        val answers = answersString.split(",").map { it.trim() }
                        Question(
                            id,
                            questionText,
                            answers,
                            theme,
                            nextQuestion,
                            expectedAnswer,
                            additionalInfo
                        )
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

    fun fetchJournalRecords(callback: (List<JournalRecord>) -> Unit) {
        Utilities.firestore.collection("journals")
            .whereEqualTo("userUID", Utilities.getCurrentUserUid())
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val journalRecords = task.result?.documents?.mapNotNull { document ->
                        try {
                            val userUID = document.getString("userUID")
                            val recordTitle = document.getString("recordTitle")
                            val date = document.getString("date")
                            val imageUrls = document.get("imageUrls") as MutableList<String>

                            val frontPins = (document.get("frontPins") as? List<Map<String, Any>>)
                                ?.mapNotNull { map ->
                                    val x = (map["x"] as? Number)?.toFloat()
                                    val y = (map["y"] as? Number)?.toFloat()
                                    if (x != null && y != null) Pin(x, y) else null
                                }
                            val backPins = (document.get("backPins") as? List<Map<String, Any>>)
                                ?.mapNotNull { map ->
                                    val x = (map["x"] as? Number)?.toFloat()
                                    val y = (map["y"] as? Number)?.toFloat()
                                    if (x != null && y != null) Pin(x, y) else null
                                }
                            val surveyResponses =
                                (document.get("surveyResponses") as? List<Map<String, String>>)
                                    ?.mapNotNull { map ->
                                        val question = map["question"]
                                        val answer = map["response"]
                                        if (question != null && answer != null)
                                            SurveyResponse(question, answer) else null
                                    }
                            val additionalNotes =
                                (document.get("additionalNotes") as? List<Map<String, String>>)
                                    ?.mapNotNull { map ->
                                        val date = map["date"]
                                        val content = map["content"]
                                        if (date != null && content != null)
                                            Note(date, content) else null
                                    }

                            JournalRecord(
                                userUID = userUID ?: "Użytkownik niezalogowany",
                                recordTitle = recordTitle ?: "Brak tytułu",
                                date = date ?: "Brak daty",
                                imageUrls = imageUrls,
                                frontPins = frontPins,
                                backPins = backPins,
                                surveyResponses = surveyResponses,
                                additionalNotes = additionalNotes,
                                documentId = document.id
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                            null
                        }
                    } ?: listOf()
                    callback(journalRecords)
                } else {
                    callback(listOf())
                }
            }
    }

//    fun fetchJournalRecords(callback: (List<JournalRecord>) -> Unit) {
//        Utilities.firestore.collection("journals").get().addOnCompleteListener { task ->
//            if (task.isSuccessful) {
//                val journalRecords = task.result?.documents?.mapNotNull { document ->
//                    try {
//                        val userUID = document.getString("userUID")
//                        val recordTitle = document.getString("recordTitle")
//                        val date = document.getString("date")
//                        val imageUrls = document.get("imageUrls") as? List<String> ?: listOf()
//                        val frontPins = (document.get("frontPins") as? List<Map<String, Any>>)
//                            ?.map { Pair(it["x"] as Float, it["y"] as Float) }
//                        val backPins = (document.get("backPins") as? List<Map<String, Any>>)
//                            ?.map { Pair(it["x"] as Float, it["y"] as Float) }
//                        val surveyResponses =
//                            (document.get("surveyResponses") as? List<Map<String, String>>)
//                                ?.map { Pair(it["question"] ?: "", it["response"] ?: "") }
//                        val additionalNotes = document.getString("additionalNotes")
//
//                        JournalRecord(
//                            recordTitle = recordTitle ?: "Brak tytułu",
//                            userUID = userUID ?: "Nieznany użytkownik",
//                            date = date ?: "Brak daty",
//                            imageUrls = imageUrls,
//                            frontPins = frontPins,
//                            backPins = backPins,
//                            surveyResponses = surveyResponses,
//                            additionalNotes = additionalNotes,
//                            documentId = document.id
//                        )
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                        null
//                    }
//                } ?: listOf()
//
//                callback(journalRecords)
//            } else {
//                callback(listOf())
//            }
//        }
//    }


    fun addJournalRecordToDatabase(
        record: JournalRecord,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val recordMap = mapOf(
            "userUID" to record.userUID,
            "recordTitle" to record.recordTitle,
            "date" to record.date,
            "imageUrls" to listOf<String>(),
            "frontPins" to listOf<Map<Float, Float>>(),
            "backPins" to listOf<Map<Float, Float>>(),
            "surveyResponses" to listOf<Map<String, String>>(),
            "additionalNotes" to null,
            "documentId" to null
        )

        Utilities.firestore.collection("journals")
            .add(recordMap)
            .addOnSuccessListener { documentReference ->
                val documentId = documentReference.id
                onSuccess(documentId)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun updateJournalRecordNote(
        documentId: String,
        updatedNotes: List<Note>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val documentReference = Utilities.firestore.collection("journals")
            .document(documentId)

        val notesToSave = updatedNotes.map {
            mapOf(
                "date" to it.date,
                "content" to it.content
            )
        }
        documentReference.update("additionalNotes", notesToSave)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }


    fun removeJournalByDocumentId(
        documentId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        Utilities.firestore.collection("journals")
            .document(documentId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }


}