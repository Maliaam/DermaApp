package com.example.dermaapplication.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dermaapplication.MainActivity
import com.example.dermaapplication.R
import com.example.dermaapplication.Utilities
import com.example.dermaapplication.adapters.FeedAdapter
import com.example.dermaapplication.fragments.registration.LoginFragment
import com.example.dermaapplication.fragments.registration.RegistrationFragment
import com.example.dermaapplication.fragments.wikiFragments.SkinDiseaseDetailedFragment
import com.example.dermaapplication.items.Disease


class HomeFragment : Fragment() {
    private lateinit var loginButton: CardView
    private lateinit var registerButton: CardView
    private val diseasesList = ArrayList<Disease>()
    private val diseasesAdapter by lazy { FeedAdapter(diseasesList) }
    private lateinit var otherDiseasesText: TextView
    private lateinit var recyclerView: RecyclerView

    private fun initializeViews(view: View){
        loginButton = view.findViewById(R.id.feed_loginCardView)
        registerButton = view.findViewById(R.id.feed_registerCardView)
        otherDiseasesText = view.findViewById(R.id.feed_home_others)
        recyclerView = view.findViewById(R.id.feed_home_recyclerView)
    }
    private fun setupOnClickListeners(){
        loginButton.setOnClickListener {
            (activity as MainActivity).replaceFragment(LoginFragment())
        }
        registerButton.setOnClickListener {
            (activity as MainActivity).replaceFragment(RegistrationFragment())
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
    }
    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById<RecyclerView?>(R.id.feed_home_recyclerView).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL,
                false
            )
            this.adapter = diseasesAdapter
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        initializeViews(view)
        setupOnClickListeners()
        Utilities.databaseFetch.fetchDiseases { diseases ->
            diseasesList.clear()
            diseasesList.addAll(diseases)
            diseasesAdapter.notifyDataSetChanged()
        }
        setupRecyclerView(view)

        return view
    }

}