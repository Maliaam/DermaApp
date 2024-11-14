package com.example.dermaapplication.fragments.chatFragments

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dermaapplication.MainActivity
import com.example.dermaapplication.R
import com.example.dermaapplication.Utilities
import com.example.dermaapplication.adapters.MessageAdapter
import com.example.dermaapplication.items.Message
import com.example.dermaapplication.vmd.ChatViewModel

/**
 * Fragment odpowiedzialny za wyświetlanie czatu pomiędzy nadawcą a odbiorcą.
 * Umożliwia wysyłanie oraz wyświetlanie wiadomości w czasie rzeczywistym.
 */
class MessageChatFragment : Fragment() {
    private lateinit var viewModel: ChatViewModel
    private lateinit var personName: TextView
    private val messagesList = ArrayList<Message>()
    private val adapter by lazy { MessageAdapter(messagesList) }
    private lateinit var messageRecyclerView: RecyclerView
    private lateinit var sendMessageButton: ImageView
    private lateinit var sendMessageText: EditText
    private lateinit var senderId: String
    private lateinit var senderName: String
    private lateinit var goBack: ImageView
    private lateinit var makePhotoButton: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_message_chat, container, false)
        (activity as? MainActivity)?.hideBottomNav()
        viewModel = ViewModelProvider(this)[ChatViewModel::class.java]

        // Inicjalizacja widoków
        sendMessageButton = view.findViewById(R.id.message_sendMessage)
        sendMessageText = view.findViewById(R.id.message_editText)
        personName = view.findViewById(R.id.message_receiverNameSurname)
        makePhotoButton = view.findViewById(R.id.message_makePhoto)

        // Pobranie danych z bundle (przekazanych z ChatMenuFragment)
        senderId = arguments?.getString("senderUID")!!
        senderName = arguments?.getString("senderName")!!
        personName.text = senderName

        // Inicjalizacja wiadomości z bazy danych oraz ich obserwacja
        viewModel.loadMessages(senderId)
        observeMessages()

        // Obsługa wysłania wiadomości do odbiorcy poprzez kliknięcie przycisku
        sendMessageButton.setOnClickListener {
            val messageText = sendMessageText.text.toString()
            Utilities.getCurrentUserName { currentUserName ->
                val message = Message(
                    senderId = Utilities.getCurrentUserUid(), // UID nadawcy
                    senderName = currentUserName ?: "Unknown", // Imię nadawcy
                    receiverId = senderId, // UID odbiorcy
                    receiverName = arguments?.getString("senderName")!!, // Imię odbiorcy
                    messageText = messageText, // Treść wiadomości
                    timestamp = Utilities.getCurrentTime() // Znacznik czasowy wiadomości
                )
                viewModel.sendMessage(message)
            }
            // Wyczyszczenie pola tekstowego po wysłaniu wiadomości
            sendMessageText.setText("")

        }

        // Inicjalizacja recyclerView
        messageRecyclerView = view.findViewById(R.id.message_RecyclerView)
        setupRecyclerView(messageRecyclerView, adapter)

        // Obsługa przycisku powrotu do ChatMenuFragment
        goBack = view.findViewById(R.id.message_goBack)
        goBack.setOnClickListener {
            (activity as? MainActivity)?.showBottomNav()
            (activity as? MainActivity)?.replaceFragment(ChatMenuFragment())
        }

        makePhotoButton.setOnClickListener{
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            //TODO: Dodanie kamery i robienie zdjęcia
        }

        return view
    }

    /**
     * Konfiguracja RecyclerView z określonym adapterem.
     *
     * @param recyclerView RecyclerView do skonfigurowania.
     * @param adapter Adapter używany do wyświetlania danych.
     */
    private fun setupRecyclerView(recyclerView: RecyclerView, adapter: RecyclerView.Adapter<*>) {
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
        }
    }

    /**
     * Obserwuje zmiany listy wiadomości w czasie rzeczywistym. Gdy zajdzie zmiana w liście
     * wiadomości, aktualizuje RecyclerView oraz przewija do ostatniej wiadomości.
     */
    private fun observeMessages() {
        viewModel.getMessagesList().observe(viewLifecycleOwner) { messages ->
            messagesList.clear()
            messagesList.addAll(messages)
            adapter.notifyDataSetChanged()
            messageRecyclerView.scrollToPosition(messagesList.size - 1)
        }
    }
}