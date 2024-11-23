package com.example.dermaapplication.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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
import com.example.dermaapplication.fragments.wikiFragments.SkinDiseaseDetailedFragment
import com.example.dermaapplication.fragments.wikiFragments.SkinDiseasesFragment
import com.example.dermaapplication.items.Disease
import com.google.android.material.card.MaterialCardView

class UserFeedFragment : Fragment() {
    private lateinit var journalCardView: CardView
    private lateinit var encyclopediaCardView: CardView
    private lateinit var messagesCardView: CardView
    private val diseasesList = ArrayList<Disease>()
    private val diseasesAdapter by lazy { FeedAdapter(diseasesList) }
    private lateinit var recyclerView: RecyclerView
    private lateinit var userPicture: ImageView
    private lateinit var otherDiseasesText: TextView
    private lateinit var userNameTextView: TextView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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
        otherDiseasesText = view.findViewById(R.id.feed_others)
        userNameTextView = view.findViewById(R.id.feed_userName)

//        Utilities.getCurrentUserName { name ->
//            userNameTextView.text = "Witaj, + " + name + "!"
//        }

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
        otherDiseasesText.setOnClickListener {
            replaceFragment(SkinDiseasesFragment())
        }
        diseasesAdapter.onItemClick = { disease ->
            val bundle = Bundle().apply {
                putString("diseaseName",disease.name)
                putString("diseaseDescription",disease.description)
            }
            val skinDiseaseDetailedFragment = SkinDiseaseDetailedFragment().apply {
                arguments = bundle
            }
            (activity as? MainActivity)?.replaceFragment(skinDiseaseDetailedFragment)
        }


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