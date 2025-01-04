package com.example.dermaapplication.database

import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.example.dermaapplication.Utilities
import com.example.dermaapplication.interfaces.JournalRecordsCallback
import com.example.dermaapplication.items.Disease
import com.example.dermaapplication.items.JournalRecord
import com.example.dermaapplication.items.Note
import com.example.dermaapplication.items.Pin
import com.example.dermaapplication.items.Question
import com.example.dermaapplication.items.Survey
import com.example.dermaapplication.items.SurveyItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

/**
 * Klasa odpowiedzalna za komunikację z bazą danych Firebase.
 */
class DatabaseFetch {
    private val doctorsRef = FirebaseFirestore.getInstance().collection("doctors")
    private val usersRef = FirebaseFirestore.getInstance().collection("users")
    private val diseaseRef = FirebaseFirestore.getInstance().collection("diseases")
    private val journalsRef = FirebaseFirestore.getInstance().collection("journals")
    private val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

    /**
     * Pobiera szczegółowe informacje na temat danej choroby.
     *
     * @param diseaseName Nazwa choroby, której informacje mają zostać pobrane z bazy danych.
     * @param callback Funkcja zwrotna, która otrzyma listę informacji o chorobie lub null w przypadku błędu.
     */
    fun fetchDisease(diseaseName: String, callback: (Disease?) -> Unit) {
        diseaseRef.get().addOnCompleteListener { task ->
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
        diseaseRef.get().addOnCompleteListener { task ->
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
                    val additionalInfo = document.getString("additionalInfo")

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

    fun fetchFilteredJournalRecords(
        journalIDs: List<String>,
        callback: (List<JournalRecord>) -> Unit
    ) {
        Utilities.firestore.collection("journals")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val journalRecords = task.result?.documents?.mapNotNull { document ->
                        try {
                            val userUID = document.getString("userUID")
                            val recordTitle = document.getString("recordTitle")
                            val date = document.getString("date")
                            val imageUrls = document.get("imageUrls") as MutableList<String>

                            // Przetwarzanie pinów
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

                            // Przetwarzanie odpowiedzi z ankiety
                            val surveysMap =
                                document.get("surveyResponses") as? List<Map<String, Any>>
                            val surveys = surveysMap?.mapNotNull { surveyMap ->
                                val title = surveyMap["title"] as? String
                                val surveyDate = surveyMap["date"] as? String
                                val questions = surveyMap["questions"] as? List<Map<String, Any>>

                                val items = questions?.map { questionMap ->
                                    SurveyItem(
                                        question = questionMap["question"] as? String ?: "",
                                        answer = questionMap["answer"] as? String ?: ""
                                    )
                                } ?: emptyList()

                                Survey(
                                    title = title ?: "Brak tytułu",
                                    date = surveyDate ?: "Brak daty",
                                    items = items
                                )
                            }

                            // Dodatkowe notatki
                            val additionalNotes =
                                (document.get("additionalNotes") as? List<Map<String, String>>)
                                    ?.mapNotNull { map ->
                                        val date = map["date"]
                                        val content = map["content"]
                                        if (date != null && content != null)
                                            Note(date, content) else null
                                    }

                            // Tworzenie obiektu JournalRecord
                            JournalRecord(
                                userUID = userUID ?: "Użytkownik niezalogowany",
                                recordTitle = recordTitle ?: "Brak tytułu",
                                date = date ?: "Brak daty",
                                imageUrls = imageUrls,
                                frontPins = frontPins,
                                backPins = backPins,
                                surveyResponses = surveys,
                                additionalNotes = additionalNotes,
                                documentId = document.id
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                            null
                        }
                    } ?: listOf()

                    // Filtruj dzienniki na podstawie journalIDs lekarza
                    val filteredRecords = journalRecords.filter { record ->
                        journalIDs.contains(record.documentId)
                    }

                    // Logowanie znalezionych dzienników
                    Log.d(
                        "FilteredJournalRecords",
                        "Znalezione dzienniki po filtrze: ${filteredRecords.size}"
                    )
                    filteredRecords.forEach { record ->
                        Log.d(
                            "FilteredJournalRecords",
                            "Dziennik: ${record.recordTitle}, Data: ${record.date}"
                        )
                    }

                    callback(filteredRecords)
                } else {
                    callback(listOf())
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

                            // Przetwarzanie pinów
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
                            val doctorName = (document.getString("doctorName"))
                            val doctorUid = (document.getString("doctorUid"))

                            // Przetwarzanie odpowiedzi z ankiety
                            val surveysMap =
                                document.get("surveyResponses") as? List<Map<String, Any>>
                            val surveys = surveysMap?.mapNotNull { surveyMap ->
                                val title = surveyMap["title"] as? String
                                val surveyDate = surveyMap["date"] as? String
                                val questions = surveyMap["questions"] as? List<Map<String, Any>>

                                val items = questions?.map { questionMap ->
                                    SurveyItem(
                                        question = questionMap["question"] as? String ?: "",
                                        answer = questionMap["answer"] as? String ?: ""
                                    )
                                } ?: emptyList()

                                Survey(
                                    title = title ?: "Brak tytułu",
                                    date = surveyDate ?: "Brak daty",
                                    items = items,
                                )
                            }

                            // Dodatkowe notatki
                            val additionalNotes =
                                (document.get("additionalNotes") as? List<Map<String, String>>)
                                    ?.mapNotNull { map ->
                                        val date = map["date"]
                                        val content = map["content"]
                                        if (date != null && content != null)
                                            Note(date, content) else null
                                    }

                            // Tworzenie obiektu JournalRecord
                            JournalRecord(
                                userUID = userUID ?: "Użytkownik niezalogowany",
                                recordTitle = recordTitle ?: "Brak tytułu",
                                date = date ?: "Brak daty",
                                imageUrls = imageUrls,
                                frontPins = frontPins,
                                backPins = backPins,
                                surveyResponses = surveys,
                                additionalNotes = additionalNotes,
                                documentId = document.id,
                                doctorName = doctorName,
                                doctorUid = doctorUid
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                            null
                        }
                    } ?: listOf()

                    // Logowanie znalezionych dzienników
                    Log.d("JournalRecords", "Znalezione dzienniki: ${journalRecords.size}")
                    journalRecords.forEach { record ->
                        Log.d(
                            "JournalRecords",
                            "Dziennik: ${record.recordTitle}, Data: ${record.date}"
                        )
                    }

                    callback(journalRecords)
                } else {
                    callback(listOf())
                }
            }
    }

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


    fun updateJournalSurveys(
        documentId: String,
        surveys: List<Survey>,
        title: String,
        date: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val documentReference = Utilities.firestore.collection("journals").document(documentId)

        val newSurveysMap = surveys.map { survey ->
            mapOf(
                "title" to title,
                "date" to date,
                "questions" to survey.items.map { item ->
                    mapOf(
                        "question" to item.question,
                        "answer" to item.answer
                    )
                }
            )
        }

        // Pobranie aktualnych surveyResponses z dokumentu
        documentReference.get()
            .addOnSuccessListener { documentSnapshot ->
                val existingSurveys =
                    documentSnapshot.get("surveyResponses") as? List<Map<String, Any>>
                        ?: emptyList()

                // Dodanie nowych survey do istniejącej listy
                val updatedSurveys = existingSurveys + newSurveysMap

                // Zaktualizowanie pola w bazie danych
                documentReference.update("surveyResponses", updatedSurveys)
                    .addOnSuccessListener {
                        onSuccess() // Zakończono pomyślnie
                    }
                    .addOnFailureListener { exception ->
                        onFailure(exception) // Obsługuje błędy
                    }
            }
            .addOnFailureListener { exception ->
                onFailure(exception) // Obsługuje błędy podczas pobierania danych
            }
    }

    fun updatePinsInSurvey(documentId: String, frontPins: List<Pin>, backPins: List<Pin>) {
        val documentRef = journalsRef.document(documentId)


        val frontPinsAsMap = frontPins.map { pin -> mapOf("x" to pin.x, "y" to pin.y) }
        val backPinsAsMap = backPins.map { pin -> mapOf("x" to pin.x, "y" to pin.y) }

        documentRef.update(
            "frontPins", frontPinsAsMap,
            "backPins", backPinsAsMap
        ).addOnSuccessListener {
            Log.d("FirestoreUpdate", "Pins successfully added to the journal.")
        }.addOnFailureListener { e ->
            Log.e("FirestoreUpdate", "Error updating document: ", e)
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

    fun deleteSurveyFromJournal(
        documentId: String,
        surveyToDelete: Survey,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val documentReference = Utilities.firestore.collection("journals").document(documentId)
        documentReference.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val currentSurveys = document.get("surveyResponses") as? List<Map<String, Any>>
                if (currentSurveys != null) {
                    val updatedSurveys = currentSurveys.filterNot { survey ->
                        survey["title"] == surveyToDelete.title &&
                                survey["date"] == surveyToDelete.date
                    }
                    documentReference.update("surveyResponses", updatedSurveys)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { exception -> onFailure(exception) }
                }
            }
        }
    }

    /**
     * Wyszukuje dokument użytkownika w kolekcji "users" na podstawie pola "uid".
     *
     * @param userUid Unikalny identyfikator użytkownika (UID), na podstawie którego znajduje
     *                dokument.
     * @param callback Funkcja zwrotna, która zwraca znaleziony dokument ('DocumentSnapshot'), lub
     *                 'null', jeśli dokument nie został znaleziony.
     */
    private fun findUserDocument(userUid: String, callback: (DocumentSnapshot?) -> Unit) {
        usersRef.whereEqualTo("uid", userUid).get()
            .addOnSuccessListener { query ->
                if (!query.isEmpty) callback(query.documents[0])
                else callback(null)
            }
    }

    private fun findDoctorDocument(userUid: String, callback: (DocumentSnapshot?) -> Unit) {
        doctorsRef.whereEqualTo("uid", userUid).get()
            .addOnSuccessListener { query ->
                if (!query.isEmpty) callback(query.documents[0])
                else callback(null)
            }
    }

    /**
     * Przesyła zdjęcie profilowe użytkownika do FirebaseStorage, a następnie zapisuje jego URL w
     * FirebaseFirestore.
     *
     * @param uri URI pliku zdjęcia, które ma zostać przesłane do bazy danych.
     */
    fun uploadUserProfilePhoto(uri: Uri) {
        val userUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val storageReference = FirebaseStorage.getInstance().reference.child(
            "users/$userUid/profileImage/profileImage.jpg"
        )
        val uploadTask = storageReference.putFile(uri)
        uploadTask.addOnSuccessListener {
            storageReference.downloadUrl.addOnSuccessListener { downloadedUrl ->
                saveImageUrlToFirestore(downloadedUrl.toString())
            }
        }
    }

    /**
     * Zapisuje URL zdjęcia profilowego użytkownika w dokumencie użytkownika w FirebaseFirestore.
     *
     * @param imageUrl URL zdjęcia profilowego, który ma zostać zapisany.
     */
    fun saveImageUrlToFirestore(imageUrl: String) {
        findUserDocument(Utilities.getCurrentUserUid()) { document ->
            document?.reference?.update("profileImageUrl", imageUrl)
        }
    }

    /**
     * Pobiera URL zdjęcia profilowego użytkownika z Firestore i zwraca go za pomocą funkcji callback.
     *
     * @param callback Funkcja zwrotna, która zwraca URL zdjęcia profilowego jako 'String' lub
     *                 'null', jeśli zdjęcie nie zostało odnalezione.
     */
    fun loadUserProfileImage(callback: (String?) -> Unit) {
        findUserDocument(Utilities.getCurrentUserUid()) { document ->
            if (document != null) {
                val imageUrl = document.getString("profileImageUrl")
                callback(imageUrl)
            } else {
                callback(null)
            }
        }
    }

    fun fetchUserProfileImageUrlByUid(uid: String, callback: (String?) -> Unit) {
        findDoctorDocument(uid) { document ->
            if (document != null) {
                val imageUrl = document.getString("profileImageUrl")
                callback(imageUrl)
            }
        }
    }

    fun fetchDoctorJournalRecords(callback: JournalRecordsCallback) {
        doctorsRef.whereEqualTo("uid", currentUserUid)
            .get().addOnSuccessListener { journal ->
                if (journal != null && !journal.isEmpty) {
                    val doctorDocument = journal.documents.firstOrNull()
                    val journalsIDs =
                        doctorDocument?.get("journals") as? List<String> ?: emptyList()
                    if (journalsIDs.isNotEmpty()) {
                        fetchFilteredJournalRecords(journalsIDs) { journalRecords ->
                            val filteredRecords =
                                journalRecords.filter { it.documentId in journalsIDs }
                            callback.onJournalRecordsFetched(filteredRecords)
                        }
                    } else {
                        callback.onError("Brak powiązanych dzienników")
                    }
                } else {
                    callback.onError("Błąd pobierania danych lekarza")
                }
            }
    }

    fun addJournalToDoctor(journalCode: String) {
        doctorsRef.whereEqualTo("uid", currentUserUid).get().addOnSuccessListener { docDocument ->
            if (!docDocument.isEmpty) {
                val doctorDocument = docDocument.documents.first()
                val doctorRef = doctorDocument.reference
                doctorRef.update("journals", FieldValue.arrayUnion(journalCode))
            }
        }
    }

    fun findDoctorByNameAndSurname(name: String, surname: String, onSuccess: (String?) -> Unit) {
        doctorsRef.whereEqualTo("name", name).whereEqualTo("surname", surname).get()
            .addOnSuccessListener { doctorDocuments ->
                if (!doctorDocuments.isEmpty) {
                    val doctorDocument = doctorDocuments.documents.firstOrNull()
                    val profileImageUrl = doctorDocument?.getString("profileImageUrl")
                    onSuccess(profileImageUrl)
                }
            }
    }

    fun findNameAndSurnameByUid(uid: String, callback: (String?, String?) -> Unit) {
        usersRef.whereEqualTo("uid", uid).get().addOnSuccessListener { userDocuments ->
            if (!userDocuments.isEmpty) {
                val userDocument = userDocuments.documents.firstOrNull()
                val name = userDocument?.getString("name")
                val surname = userDocument?.getString("surname")
                callback(name, surname)
            }
        }
    }
}
