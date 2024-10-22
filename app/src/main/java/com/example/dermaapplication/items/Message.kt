package com.example.dermaapplication.items

data class Message(
    val senderId: String = "",
    val senderName: String = "",
    val receiverId: String = "",
    val messageText: String = "",
    val timestamp:String = ""
)
