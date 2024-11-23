package com.example.dermaapplication.fragments.journal

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.dermaapplication.R


class ImageViewFragment : Fragment() {

    private lateinit var expandImageView: ImageView
    private lateinit var backButton: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_image_view, container, false)

        expandImageView = view.findViewById(R.id.expand_image)
        backButton = view.findViewById(R.id.btnBack)

        val imageUrl = arguments?.getString("imageUrl")
        imageUrl?.let {
            Glide.with(this)
                .load(it)
                .into(expandImageView)
        }

        backButton.setOnClickListener {
            fragmentManager?.popBackStack()
        }

        return view
    }
}
