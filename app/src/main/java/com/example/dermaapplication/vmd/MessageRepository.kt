package com.example.dermaapplication.vmd

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.dermaapplication.Utilities
import com.example.dermaapplication.items.Message
import com.google.firebase.firestore.Query

/**
 * Klasa odpowiedzialna za pobieranie i przechowywanie danych wiadomości w bazie danych Firebase.
 * Używana jest jako repozytorium, które dostarcza dane do innych komponentów aplikacji poprzez
 * obiekty LiveData.
 */
class MessageRepository {
    /**
     * Przechowuje listę wiadomości ( wysłanych i odebranych) dla konkretnej konwersacji jako
     * obiekty typu 'Message'
     */
    private val messages = MutableLiveData<List<Message>>()

    /**
     * Przechowuje listę obiektów typu 'Message' reprezentujących ostatnie wysłane lub otrzymane
     * wiadomości.
     */
    private val lastMessages = MutableLiveData<List<Message>>()

    /**
     * Pobiera wszystkie wiadomości między aktualnie zalogowanym użytkownikiem a innym użytkownikiem,
     * o podanym UID. Aktualizuje zmienną 'messages'.
     *
     * @param receiverUID UID drugiego użytkownika.
     */
    fun fetchMessages(receiverUID: String) {
        val loggedUserUID = Utilities.getCurrentUserUid()
        val uniqueChatId: String =
            if (Utilities.isUserDoctor == true)
                Utilities.createUniqueChatId(receiverUID, loggedUserUID)
            else
                Utilities.createUniqueChatId(loggedUserUID, receiverUID)

        Utilities.firestore.collection("messages")
            .document(uniqueChatId)
            .collection("chats")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Log.e("MessageRepository", "Błąd w czasie pobierania wiadomości", exception)
                    return@addSnapshotListener
                }
                val messagesList = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(Message::class.java)
                } ?: emptyList()
                messages.value = messagesList
            }
    }

    /**
     * Pobiera i wyszukuje ostatnie wiadomości z bazy danych Firebase na podstawie UID zalogowanego
     * użytkownika.
     * @param doctorUIDs Lista UID wszystkich doktorów, z którymi istnieje historia rozmów.
     */
    fun fetchLastMessages(doctorUIDs: List<String>) {
        val loggedUserUID = Utilities.getCurrentUserUid()
        val messagesList = mutableListOf<Message>()
        var completedRequests = 0

        doctorUIDs.forEach { doctorUID ->
            val uniqueChatID = Utilities.createUniqueChatId(loggedUserUID, doctorUID)
            Utilities.firestore.collection("messages")
                .document(uniqueChatID)
                .collection("chats")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener { chatDocument ->
                    chatDocument.forEach { document ->
                        val message = document.toObject(Message::class.java)
                        messagesList.add(message)
                    }
                    completedRequests++
                    if (completedRequests == doctorUIDs.size) {
                        lastMessages.value = messagesList
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("MessagesRepository", "Error fetching messages for $doctorUID", e)
                    completedRequests++
                    if (completedRequests == doctorUIDs.size) {
                        lastMessages.value = messagesList
                    }
                }
        }
    }

    /**
     * Pobiera i wyszukuje ostatnie wiadomości z bazy danych Firebase na podstawie UID zalogowanego
     * użytkownika.
     * @param usersUIDs Lista UID wszystkich użytkowników, z którymi istnieje historia rozmów.
     */
    fun fetchLastMessagesForDoctors(usersUIDs: List<String>) {
        val loggedUserUID = Utilities.getCurrentUserUid()
        val messagesList = mutableListOf<Message>()
        var completedRequests = 0

        usersUIDs.forEach { userUID ->
            val uniqueChatID = Utilities.createUniqueChatId(userUID, loggedUserUID)
            Log.e("MessagesRepository", uniqueChatID)
            Utilities.firestore.collection("messages")
                .document(uniqueChatID)
                .collection("chats")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener { chatDocument ->
                    chatDocument.forEach { document ->
                        val message = document.toObject(Message::class.java)
                        messagesList.add(message)
                        Log.d("MessagesRepository", "Fetched message: $message")
                    }
                    completedRequests++
                    if (completedRequests == usersUIDs.size) {
                        lastMessages.value = messagesList
                        Log.d("MessagesRepository", "All messages fetched doc: $messagesList")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("MessagesRepository", "Error fetching messages for $userUID", e)
                    completedRequests++
                    if (completedRequests == usersUIDs.size) {
                        lastMessages.value = messagesList
                    }
                }
        }
    }

    /**
     * Zwraca listę obiektów 'Messages' reprezentujących wiadomości dla wybranego czatu.
     *
     * @return LiveData<List<Message>> z listą wiadomości pomiędzy dwoma użytkownikami.
     */
    fun getMessages(): LiveData<List<Message>> = messages

    /**
     * Zwraca listę obiektów 'Messages' reprezentujących ostatnią wiadomość dla każdej konwersacji.
     *
     * @return LiveData<List<Message>> z listą ostatnich wiadomości pomiędzy różnymi użytkownikami.
     */
    fun getLastMessages(): LiveData<List<Message>> = lastMessages
}