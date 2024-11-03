package com.example.dermaapplication.items

/**
 * Klasa reprezentująca wiadomość wymienianą między użytkownikami.
 *
 * @property senderId Identyfikator UID nadawcy wiadomości.
 * @property senderName Imię i nazwisko nadawcy wiadomości.
 * @property receiverId Identyfikator UID odbiorcy wiadomości.
 * @property receiverName Imię i nazwisko odbiorcy wiadomości.
 * @property messageText Treść wiadomości przesyłanej pomiędzy nadawcą a odbiorcą.
 * @property timestamp Znacznik czasowy wiadomości, w którym momencie została nadana.
 */
data class Message(
    val senderId: String = "",
    val senderName: String = "",
    val receiverId: String = "",
    val receiverName: String = "",
    val messageText: String = "",
    val timestamp: String = ""
)
