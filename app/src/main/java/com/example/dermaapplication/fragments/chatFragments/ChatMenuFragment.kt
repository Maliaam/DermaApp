package com.example.dermaapplication.fragments.chatFragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dermaapplication.MainActivity
import com.example.dermaapplication.R
import com.example.dermaapplication.Utilities
import com.example.dermaapplication.adapters.ChatAdapter
import com.example.dermaapplication.adapters.DoctorsChatAdapter
import com.example.dermaapplication.items.Message
import com.example.dermaapplication.items.Users
import com.example.dermaapplication.vmd.ChatViewModel

/**
 * Fragment odpowiadający za wyświetlenie głównego menu czatu.
 * Umożliwia przeglądanie ostatnich wiadomości oraz listy dostępnych lekarzy, z którymi można
 * rozpocząć rozmowę.
 */
class ChatMenuFragment : Fragment() {
    private lateinit var viewModel: ChatViewModel
    private val messList = ArrayList<Message>()
    private val docList = ArrayList<Users>()
    private val adapter by lazy { ChatAdapter(messList) }
    private val docAdapter by lazy { DoctorsChatAdapter(docList) }
    private lateinit var doctorsRecyclerView: RecyclerView
    private lateinit var chatRecyclerView: RecyclerView

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        viewModel = ViewModelProvider(this)[ChatViewModel::class.java]

        // Inicjalizacja recyclerview oraz potrzebnych do jego działania danych
        chatRecyclerView = view.findViewById(R.id.chat_rv)
        doctorsRecyclerView = view.findViewById(R.id.doctors_recyclerview)
        setupRecyclerView(chatRecyclerView, adapter, false)
        setupRecyclerView(doctorsRecyclerView, docAdapter, true)
        initializeOnClickListeners()

        // Obserwacja listy lekarzy w czasie rzeczywistym
        viewModel.getDoctorsUIDsList().observe(viewLifecycleOwner) { doctorsUIDsList ->
            viewModel.loadLastMessages(doctorsUIDsList)
        }
        viewModel.getUsersUIDsList().observe(viewLifecycleOwner) { usersUIDsList ->
            viewModel.loadLastMessages(usersUIDsList)
        }
        viewModel.getDoctorsList().observe(viewLifecycleOwner) { doctors ->
            docList.clear()
            docList.addAll(doctors)
            docAdapter.notifyDataSetChanged()
        }
        // Obserwacja ostatnich wiadomości użytkownika
        viewModel.getLastMessagesList().observe(viewLifecycleOwner) { lastMessages ->
            messList.addAll(lastMessages)
            adapter.notifyDataSetChanged()
        }
        return view
    }

    /**
     * Konfiguracja RecyclerView z określonym adapterem i układem.
     *
     * @param recyclerView RecyclerView do skonfigurowania.
     * @param adapter Adapter używany do wyświetlania danych.
     * @param isHorizontal Określa czy layout ma być poziomy lub pionowy.
     */
    private fun setupRecyclerView(recyclerView: RecyclerView, adapter: RecyclerView.Adapter<*>,
                                  isHorizontal: Boolean) {
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager =
                LinearLayoutManager(requireContext(),
                    if (isHorizontal) LinearLayoutManager.HORIZONTAL
                    else LinearLayoutManager.VERTICAL, false)
            this.adapter = adapter
        }
    }

    /**
     * Inicjalizacja onItemClick dla adapterów.
     * Ustawia opowiedź na kliknięcie w item w RecyclerView.
     * Po kliknięciu w item, otwierany jest nowy fragment, do którego przekazywane są dane.
     */
    private fun initializeOnClickListeners() {
        docAdapter.onItemClick = { user ->
            openChatFragment(user.name, user.uid)
        }
        adapter.onItemClick = { message ->
            if (message.senderId == Utilities.getCurrentUserUid()) {
                openChatFragment(message.receiverName, message.receiverId)
            } else {
                openChatFragment(message.senderName, message.senderId)
            }
        }
    }

    /**
     * Otwiera fragment MessageChatFragment, przekazując nazwę i UID użytkownika.
     *
     * @param senderName Imię i nazwisko nadawcy wiadomości.
     * @param senderUID Identyfikator UID nadawcy wiadomości.
     */
    private fun openChatFragment(senderName: String, senderUID: String) {
        val bundle = Bundle().apply {
            putString("senderName", senderName)
            putString("senderUID", senderUID)
        }
        val messageChatFragment = MessageChatFragment().apply { arguments = bundle }
        (activity as? MainActivity)?.replaceFragment(messageChatFragment)
    }
}