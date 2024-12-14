package com.example.dermaapplication.fragments.journal

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dermaapplication.MainActivity
import com.example.dermaapplication.R
import com.example.dermaapplication.fragments.journal.adapters.JournalImageGridAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * Fragment do wyświetlania galerii zdjęć danego wpisu dziennika
 */
class ImagesGalleryFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: JournalImageGridAdapter
    private lateinit var tittleTextView: TextView
    private lateinit var goBack: ImageButton
    private lateinit var selectionButton: View
    private var imageUrls: List<String> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_images, container, false)
        recyclerView = view.findViewById(R.id.record_images_fragmentRV)
        tittleTextView = view.findViewById(R.id.gallery_text)
        selectionButton = view.findViewById(R.id.close_selection_button)
        goBack = view.findViewById(R.id.gallery_previousFragment)

        imageUrls = arguments?.getStringArrayList("imageUrls") ?: emptyList()
        selectionButton.setOnClickListener {
            exitSelectionMode()
        }
        goBack.setOnClickListener {
            fragmentManager?.popBackStack()
        }
        setupRecyclerView()
        return view
    }

    private fun setupRecyclerView() {

        adapter = JournalImageGridAdapter(
            imageUrls,
            onImageLongClick = {},
            onImageClick = { position ->

                    Log.e("GoToImage", position.toString())
                    val bundle = Bundle().apply {
                        putString("imageUrl", imageUrls[position])
                    }
                    val imageViewFragment = ImageViewFragment().apply {
                        arguments = bundle
                    }
                    (activity as MainActivity).replaceFragment(imageViewFragment)
            },
            onSelectedCountChanged = { count ->
                toggleSelectionMode()
            },
            onSelectionModeChanged = {},
        )
        recyclerView.layoutManager = GridLayoutManager(context, 3)
        recyclerView.adapter = adapter
        val decoration = object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                super.getItemOffsets(outRect, view, parent, state)
                outRect.left = 1
                outRect.right = 1
                outRect.top = 1
                outRect.bottom = 1
            }
        }
        recyclerView.addItemDecoration(decoration)
    }

    private fun updateTitle(selectedCount: Int) {
        tittleTextView.text = when {
            selectedCount > 0 -> "$selectedCount wybranych elementów"
            else -> "GALERIA"
        }
    }

    private fun toggleSelectionMode() {
        selectionButton.visibility = if (adapter.isSelectionMode) View.VISIBLE else View.GONE
        updateTitle(adapter.selectedItems.size)
    }

    private fun exitSelectionMode() {
        selectionButton.visibility = View.GONE
        adapter.clearSelection()
        updateTitle(0)
    }

    // todo remove images
}


