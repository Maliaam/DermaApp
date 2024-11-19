package com.example.dermaapplication.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dermaapplication.MainActivity
import com.example.dermaapplication.R
import com.example.dermaapplication.Utilities
import com.example.dermaapplication.adapters.FeedAdapter
import com.example.dermaapplication.fragments.chatFragments.ChatMenuFragment
import com.example.dermaapplication.fragments.journal.JournalFragment
import com.example.dermaapplication.fragments.wikiFragments.SkinDiseasesFragment
import com.example.dermaapplication.items.Disease
import com.google.android.material.card.MaterialCardView

class UserFeedFragment : Fragment() {
    private lateinit var doctorImage: ImageView

    private lateinit var journalCardView: CardView
    private lateinit var encyclopediaCardView: CardView
    private lateinit var messagesCardView: CardView
    private val diseasesList = ArrayList<Disease>()
    private val diseasesAdapter by lazy { FeedAdapter(diseasesList) }
    private lateinit var recyclerView: RecyclerView
    private lateinit var userPicture: ImageView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_feed, container, false)

        Utilities.databaseFetch.fetchDiseases { diseases ->
            diseasesList.clear()
            diseasesList.addAll(diseases)
            diseasesAdapter.notifyDataSetChanged()

        }
        setupRecyclerView(view)
        journalCardView = view.findViewById(R.id.feed_journalCardView)
        encyclopediaCardView = view.findViewById(R.id.feed_encyclopediaCardView)
        messagesCardView = view.findViewById(R.id.feed_messagesCardView)
        userPicture = view.findViewById(R.id.user_image)

        journalCardView.setOnClickListener {
            replaceFragment(JournalFragment())
        }
        encyclopediaCardView.setOnClickListener {
            replaceFragment(SkinDiseasesFragment())
        }
        messagesCardView.setOnClickListener {
            replaceFragment(ChatMenuFragment())
        }
        userPicture.setOnClickListener {
            replaceFragment(UserFragment())
        }


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

    private fun replaceFragment(fragment: Fragment) {
        (activity as MainActivity).replaceFragment(fragment)
    }

    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById<RecyclerView?>(R.id.feed_recyclerView).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL,
                false
            )
            this.adapter = diseasesAdapter
        }
    }
}