package com.example.dermaapplication.fragments

import android.os.Bundle
import android.util.Log
import com.example.dermaapplication.items.Message
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dermaapplication.R
import com.example.dermaapplication.adapters.ChatAdapter
import com.example.dermaapplication.database.DatabaseFetch
import com.example.dermaapplication.vmd.ChatViewModel
import java.text.SimpleDateFormat
import java.util.Locale


class ChatFragment : Fragment() {
    private lateinit var viewModel: ChatViewModel
    private val messList = ArrayList<Message>()
    private val adapter by lazy { ChatAdapter(messList) }
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var userID: String
    private lateinit var databaseFetch: DatabaseFetch

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        viewModel = ViewModelProvider(this)[ChatViewModel::class.java]

        chatRecyclerView = view.findViewById(R.id.chat_rv)
        setupRecyclerView()
        databaseFetch = DatabaseFetch()
        //TODO GET USER ID
        userID = "9YVo5GyArBcVKoLyRpOU"
        fetchMessagesFromDatabase()

        return view
    }

    private fun setupRecyclerView() {
        chatRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ChatFragment.adapter
        }
    }

    private fun fetchMessagesFromDatabase() {
        databaseFetch.fetchMessages(userID).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val fetchedMessages: List<Message> = task.result ?: emptyList()
                val lastMessages = viewModel.lastMessage(fetchedMessages)
                val sortedMessages =
                    lastMessages.sortedByDescending { message -> message.timestamp }



                messList.clear()
                messList.addAll(sortedMessages)
                adapter.notifyDataSetChanged()
                for (message in sortedMessages) {
                    Log.d("SortedMessages", "Sender Name: ${message.senderName}, Last Message: ${message.messageText}, Timestamp: ${message.timestamp}")
                }

                // Automatyczne przewijanie do ostatniej wiadomości
            } else {
                Log.e("FetchMessages", "Failed to fetch messages", task.exception)
            }
        }
    }


}
