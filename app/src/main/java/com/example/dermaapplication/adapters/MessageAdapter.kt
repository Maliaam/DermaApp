package com.example.dermaapplication.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dermaapplication.R
import com.example.dermaapplication.Utilities
import com.example.dermaapplication.items.Message
import com.example.dermaapplication.user.User

class MessageAdapter(var messageList: List<Message>) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    private val messageLEFT = 0
    private val messageRIGHT = 1

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.messageText)
        val timestamp: TextView = itemView.findViewById(R.id.messageTime)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.message_display, parent, false)
        return MessageViewHolder(view)
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.messageText.text = messageList[position].messageText
    }

    override fun getItemViewType(position: Int) =
        if (messageList[position].senderId == Utilities.getCurrentUserUid()) messageRIGHT else messageLEFT

}