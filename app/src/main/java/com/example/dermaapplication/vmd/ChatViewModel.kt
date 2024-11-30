package com.example.dermaapplication.vmd

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.dermaapplication.Utilities
import com.example.dermaapplication.items.Message
import com.example.dermaapplication.items.Users

/**
 * Odpowiedzialna za zarządzanie danymi związanymi z czatem i logiką modelu czatu. Współdziała z
 * repozytoriami 'MessageRepository' oraz 'DoctorsRepository' w celu obsługi wiadomości użytkowników
 * oraz lekarzy.
 */
class ChatViewModel : ViewModel() {
    private val messagesRepository = MessageRepository()
    private val doctorsRepository = DoctorsRepository()

    /**
     * Wysyła wiadomość do wskazanego odbiorcy i zapisuje ją w bazie danych Firebase.
     *
     * Metoda sprawdza czy zalogowany użytkownik jest lekarzem, a następnie tworzy unikalny
     * identyfikator rozmowy na podstawie typu użytkownika. Następnie zapisuje wiadomość w bazie
     * danych Firebase.
     *
     * @param message Obiekt Message zawierający dane wiadomości do wysłania.
     */
    fun sendMessage(message: Message) {
        val uniqueChatId: String =
            if (Utilities.isUserDoctor == true)
                Utilities.createUniqueChatId(message.receiverId, message.senderId)
            else
                Utilities.createUniqueChatId(message.senderId, message.receiverId)

        Utilities.firestore.collection("messages")
            .document(uniqueChatId)
            .collection("chats")
            .document(Utilities.getCurrentTime(""))
            .set(message)
    }




    /**
     * Ładuje wiadomości z repozytorium dla wskazanego użytkownika na podstawie jego UID.
     *
     * @param receiverUID UID użytkownika, z którym należy wczytać rozmowy.
     */
    fun loadMessages(receiverUID: String) {
        messagesRepository.fetchMessages(receiverUID)
    }

    /**
     * Ładuje ostatnie wiadomości wymienione pomiędzy różnymi osobami.
     *
     * W zależności czy zalogowany użytkownik jest lekarzem, metoda używa odpowiedniej metody do
     * pobrania ostatnich wiadomości.
     *
     * @param listUIDs Lista UID osób, dla których mają zostać załadowane ostatnie wiadomości.
     */
    fun loadLastMessages(listUIDs: List<String>) {
        if (Utilities.isUserDoctor == true)
            messagesRepository.fetchLastMessagesForDoctors(listUIDs)
        else
            messagesRepository.fetchLastMessages(listUIDs)
    }

    /**
     * Zwraca listę ostatnich wiadomości w postaci LiveData, aktualizującą się w czasie rzeczywistym.
     *
     * @return LiveData<List<Message>> z listą ostatnich wiadomości pomiędzy dwoma użytkownikami.
     */
    fun getLastMessagesList(): LiveData<List<Message>> {
        return messagesRepository.getLastMessages()
    }

    /**
     * Zwraca listę wiadomości w postaci LiveData, aktualizującą się w czasie rzeczywistym.
     *
     * @return LiveData<List<Message>> z listą wiadomości pomiędzy dwoma użytkownikami.
     */
    fun getMessagesList(): LiveData<List<Message>> {
        return messagesRepository.getMessages()
    }

    /**
     * Zwraca listę doktorów, aktualizującą się w czasie rzeczywistym.
     *
     * @return LiveData<List<Users>> Lista lekarzy.
     */
    fun getDoctorsList(): LiveData<List<Users>> {
        return doctorsRepository.getDoctors()
    }

    /**
     * Zwraca listę UID lekarzy, która aktualizuje się w czasie rzeczywistym.
     *
     * @return LiveData<List<String>> Lista UID lekarzy.
     */
    fun getDoctorsUIDsList(): LiveData<List<String>> {
        return doctorsRepository.getDoctorsUIDs()
    }

    /**
     * Zwraca listę UID użytkowników, która aktualizuje się w czasie rzeczywistym.
     *
     * @return LiveData<List<String>> Lista UID użytkowników.
     */
    fun getUsersUIDsList(): LiveData<List<String>> {
        return doctorsRepository.getUsersUIDs()
    }
}