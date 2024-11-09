package com.example.dermaapplication.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.dermaapplication.R
import com.example.dermaapplication.Utilities

class UserFeedFragment : Fragment() {
    private lateinit var doctorImage: ImageView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_user_feed, container, false)
//
//
//        doctorImage = view.findViewById(R.id.storage)
//        val imageRef = Utilities.storage.child("doctorsImages/female_doctor.jpg")
//
//        imageRef.downloadUrl.addOnSuccessListener { url ->
//            Glide.with(view.context).load(url).into(doctorImage)
//        }.addOnFailureListener {exception ->
//            Log.e("FirebaseStorage", "Error loading image", exception)
//        }





        return view
    }

}