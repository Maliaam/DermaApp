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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessageAdapter(private var messageList: List<Message>) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
    var onItemClick: ((Message) -> Unit)? = null

    private val messageLEFT = 0
    private val messageRIGHT = 1

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.messageText)
        val timestamp: TextView = itemView.findViewById(R.id.messageTime)
        val profileImage: ImageView? = itemView.findViewById(R.id.message_profile_image)

        init {
            itemView.setOnClickListener { onItemClick?.invoke(messageList[adapterPosition]) }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == messageRIGHT) {
            val view = inflater.inflate(R.layout.message_display_right, parent, false)
            MessageViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.message_display, parent, false)
            MessageViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.messageText.text = messageList[position].messageText

        val fullTimeStamp = messageList[position].timestamp
        val cutTimeStamp = try {
            val originalFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = originalFormat.parse(fullTimeStamp)
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(date ?: Date())
        } catch (e: Exception) {
            fullTimeStamp
        }
        holder.timestamp.text = cutTimeStamp

        if (getItemViewType(position) == messageLEFT && holder.profileImage != null) {
            var getImageUid: String? = ""
            getImageUid = if (messageList[position].senderId != Utilities.getCurrentUserUid()) {
                messageList[position].senderId
            } else {
                messageList[position].receiverId
            }
            Utilities.databaseFetch.fetchUserProfileImageUrlByUid(getImageUid) { imageUrl ->
                if (imageUrl != null) {
                    Glide.with(holder.itemView.context)
                        .load(imageUrl)
                        .placeholder(R.drawable.man)
                        .error(R.drawable.man)
                        .into(holder.profileImage)
                }
            }
        }
    }

    override fun getItemViewType(position: Int) =
        if (messageList[position].senderId == Utilities.getCurrentUserUid()) messageRIGHT else messageLEFT
}