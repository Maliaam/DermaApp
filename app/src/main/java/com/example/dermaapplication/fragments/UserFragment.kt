package com.example.dermaapplication.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.dermaapplication.R
import com.example.dermaapplication.Utilities
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage

class UserFragment : Fragment() {

    private lateinit var profileImageView: ImageView
    private var selectedImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_user, container, false)

        profileImageView = view.findViewById(R.id.userFeed_profileImage)
        profileImageView.setOnClickListener {
            openGallery()
        }


        Utilities.databaseFetch.loadUserProfileImage { imageUrl ->
            if (!imageUrl.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.image_default_man)
                    .into(profileImageView)
            }
        }

        return view
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }


    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                selectedImageUri = result.data!!.data


                Glide.with(requireContext())
                    .load(selectedImageUri)
                    .into(profileImageView)

                selectedImageUri?.let { uploadUserProfilePhoto(it) }
            } else {
                Toast.makeText(requireContext(), "Nie wybrano zdjęcia.", Toast.LENGTH_SHORT).show()
            }
        }

    // Funkcja przesyłająca zdjęcie profilowe do Firebase Storage
    private fun uploadUserProfilePhoto(uri: Uri) {
        val userUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val storageReference = FirebaseStorage.getInstance().reference.child(
            "users/$userUid/profileImage/profileImage.jpg"
        )
        val uploadTask = storageReference.putFile(uri)
        uploadTask.addOnSuccessListener {
            storageReference.downloadUrl.addOnSuccessListener { downloadedUrl ->
                Utilities.databaseFetch.saveImageUrlToFirestore(downloadedUrl.toString())
                Toast.makeText(requireContext(), "Zdjęcie zostało przesłane!", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Nie udało się przesłać zdjęcia.", Toast.LENGTH_SHORT).show()
        }
    }
}
