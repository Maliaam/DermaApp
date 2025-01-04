package com.example.dermaapplication.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dermaapplication.R
import com.example.dermaapplication.Utilities
import com.example.dermaapplication.items.Message
import com.google.firebase.auth.FirebaseAuth

class ChatAdapter(var messageList: List<Message>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    var onItemClick: ((Message) -> Unit)? = null

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.chatUserImage)
        val receiverName: TextView = itemView.findViewById(R.id.chatUserNameSurname)
        val lastMessageText: TextView = itemView.findViewById(R.id.last_message)
        val lastMessageHour: TextView = itemView.findViewById(R.id.last_message_hour)

        init {
            itemView.setOnClickListener { onItemClick?.invoke(messageList[adapterPosition]) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.chat_personrv, parent, false)
        return ChatViewHolder(view)
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    fun updateList(newList: List<Message>) {
        this.messageList = newList
        notifyDataSetChanged()
    }

    //TODO IMAGE DLA UŻYTKOWNIKA
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val loggedInUid = FirebaseAuth.getInstance().currentUser?.uid
        val senderUid = messageList[position].senderId
        val receiverUid = messageList[position].receiverId
        val uid = if (senderUid != loggedInUid) senderUid else receiverUid
        Utilities.databaseFetch.fetchUserProfileImageUrlByUid(uid){ imageUrl ->
            if(imageUrl != null) {
                Glide.with(holder.itemView.context)
                    .load(imageUrl)
                    .placeholder(R.drawable.man)
                    .error(R.drawable.baseline_10k_24)
                    .into(holder.image)
            }
        }
        holder.lastMessageText.text = messageList[position].messageText
        holder.lastMessageHour.text = messageList[position].timestamp

        if (receiverUid == Utilities.getCurrentUserUid()){
            holder.receiverName.text = messageList[position].senderName
        }
        if (receiverUid != Utilities.getCurrentUserUid()){
            holder.receiverName.text = messageList[position].receiverName
        }
    }
}
