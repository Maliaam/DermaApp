package com.example.dermaapplication.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dermaapplication.R
import com.example.dermaapplication.items.Users

class DoctorsChatAdapter(var doctorsList: List<Users>) : RecyclerView.Adapter<DoctorsChatAdapter.DoctorsChatViewHolder>() {
    var onItemClick: ((Users) -> Unit)? = null

    inner class DoctorsChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.chat_doc_img)
        val name: TextView = itemView.findViewById(R.id.chat_doc_name)

        init {
            itemView.setOnClickListener { onItemClick?.invoke(doctorsList[adapterPosition]) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorsChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.doc_chatrv, parent, false)
        return DoctorsChatViewHolder(view)
    }

    override fun getItemCount(): Int {
        return doctorsList.size
    }

    override fun onBindViewHolder(holder: DoctorsChatViewHolder, position: Int) {
        Glide.with(holder.itemView.context)
            .load(doctorsList[position].profileImageUrl)
            .placeholder(R.drawable.man)
            .error(R.drawable.man)
            .into(holder.image)
        holder.name.text = doctorsList[position].name
    }
}