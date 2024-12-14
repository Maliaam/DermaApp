package com.example.dermaapplication.fragments.journal.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dermaapplication.R

class JournalImageGridAdapter(
    private val images: List<String>,
    private val onImageLongClick: (Int) -> Unit,
    private val onImageClick: (Int) -> Unit,
    private val onSelectedCountChanged: (Int) -> Unit,
    private val onSelectionModeChanged: (Boolean) -> Unit
) : RecyclerView.Adapter<JournalImageGridAdapter.ImageGridViewHolder>() {

    var selectedItems = mutableListOf<Int>()
    var isSelectionMode = false

    @SuppressLint("ClickableViewAccessibility")
    inner class ImageGridViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val checkMark: ImageView = itemView.findViewById(R.id.image_check_mark)
        val ovalIndicator: ImageView = itemView.findViewById(R.id.image_oval)

        init {
            var startTime = 0L


            itemView.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> startTime = System.currentTimeMillis()
                    MotionEvent.ACTION_UP -> {
                        val elapsedTime = System.currentTimeMillis() - startTime
                        val position = adapterPosition
                        if (position == RecyclerView.NO_POSITION) return@setOnTouchListener false

                        if (elapsedTime > 500) {
                            if (!isSelectionMode) {
                                isSelectionMode = true
                                onSelectionModeChanged(true)
                            }
                            toggleSelection(position)
                        } else {
                            if (isSelectionMode) {
                                toggleSelection(position)
                            } else {
                                onImageClick(position)
                            }
                        }
                    }
                }
                true
            }
        }
    }




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageGridViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return ImageGridViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageGridViewHolder, position: Int) {
        val imageUrl = images[position]

        Glide.with(holder.imageView.context)
            .load(imageUrl)
            .into(holder.imageView)


        holder.ovalIndicator.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
        holder.checkMark.visibility = if (selectedItems.contains(position)) View.VISIBLE else View.GONE


        holder.itemView.setOnClickListener {
            if (isSelectionMode) {
                toggleSelection(position)
            } else {
                onImageLongClick(position)
            }
        }
    }

    override fun getItemCount(): Int = images.size


    private fun toggleSelection(position: Int) {
        if (selectedItems.contains(position)) {
            selectedItems.remove(position)
        } else {
            selectedItems.add(position)
        }
        onSelectedCountChanged(selectedItems.size)


        if (selectedItems.isEmpty()) {
            isSelectionMode = false
            onSelectionModeChanged(false)
        }

        notifyDataSetChanged()
    }

    fun clearSelection() {
        selectedItems.clear()
        isSelectionMode = false
        onSelectionModeChanged(false)
        notifyDataSetChanged()
        onSelectedCountChanged(0)
    }
}
