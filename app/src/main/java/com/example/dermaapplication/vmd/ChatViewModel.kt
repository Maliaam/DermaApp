package com.example.dermaapplication.vmd

import androidx.lifecycle.ViewModel
import com.example.dermaapplication.items.Message
import com.google.firebase.firestore.FirebaseFirestore


// pobranie danego id z kolekcji o użytkowniku = id

class ChatViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    fun sendMessage(senderId: String, receiverId: String, messageText: String){
        val message = Message(senderId,receiverId,messageText)

        db.collection("messages")
            .add(message).addOnSuccessListener {
                // wiadomość
            }.addOnFailureListener{
                // exception
            }
    }

    /**
     * Metoda znajdujaca ostatnią wiadomość od użytkownika
     *
     * @return Triple(message.senderName,message.messageText,message.timestamp)
     */
    fun lastMessage(messages: List<Message>): List<Message>{
        val lastMessageMap = mutableMapOf<String,Message>()
        for (message in messages){
            val currentLastMessage = lastMessageMap[message.senderId]
            if(currentLastMessage == null || message.timestamp > currentLastMessage.timestamp){
                lastMessageMap[message.senderId] = message
            }
        }
        return lastMessageMap.values.toList()
    }
}