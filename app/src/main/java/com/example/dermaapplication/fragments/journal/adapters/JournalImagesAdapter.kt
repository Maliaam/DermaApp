package com.example.dermaapplication.fragments.journal.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dermaapplication.MainActivity
import com.example.dermaapplication.R
import com.example.dermaapplication.fragments.journal.ImageViewFragment
import com.example.dermaapplication.items.JournalRecord

class JournalImagesAdapter(
    private val record: JournalRecord,
    private val maxSlots: Int = 6
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_IMAGE = 1
    private val VIEW_TYPE_PLACEHOLDER = 2


    private val items: List<Any>

    init {
        items = mutableListOf<Any>().apply {
            addAll(record.imageUrls)
            while (size < maxSlots) add(Unit)
        }
    }


    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivImage: ImageView = itemView.findViewById(R.id.ivImage)
    }


    inner class PlaceholderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun getItemViewType(position: Int): Int {
        return if (items[position] is String) VIEW_TYPE_IMAGE else VIEW_TYPE_PLACEHOLDER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_IMAGE -> {
                val view = inflater.inflate(R.layout.layout_record_image, parent, false)
                ImageViewHolder(view)
            }

            else -> {
                val view = inflater.inflate(R.layout.layout_record_empty_image, parent, false)
                PlaceholderViewHolder(view)
            }
        }
    }

    override fun getItemCount(): Int = maxSlots

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val imageUrl = record.imageUrls.getOrNull(position)

        if (imageUrl != null) {
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .into(holder.itemView.findViewById(R.id.ivImage))

            holder.itemView.findViewById<ImageView>(R.id.ivImage).setOnClickListener {
                val context = holder.itemView.context
                val imageViewFragment = ImageViewFragment()
                val bundle = Bundle()
                bundle.putString("imageUrl", imageUrl)
                imageViewFragment.arguments = bundle

                if (context is MainActivity) {
                    context.replaceFragment(imageViewFragment)
                }
            }
        } else {
            val imageView = holder.itemView.findViewById<ImageView>(R.id.ivImage)
            imageView?.setImageResource(R.drawable.registration_icon_user)
        }
    }


}
