package com.example.dermaapplication.fragments.wikiFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import androidx.viewpager.widget.ViewPager
import com.example.dermaapplication.R
import com.example.dermaapplication.Utilities
import com.example.dermaapplication.adapters.ImagePagerAdapter
import com.example.dermaapplication.adapters.SymptomsAdapter

class SkinDiseaseDetailedFragment : Fragment() {
    private lateinit var diseaseName: TextView
    private lateinit var diseaseDescription: TextView
    private lateinit var diseasePreviousImage: ImageView
    private lateinit var diseaseNextImage: ImageView
    private lateinit var viewPager: ViewPager

    private lateinit var symptomsRecyclerView: RecyclerView

    private val urlList = mutableListOf<String>()
    private val imgAdapter by lazy { ImagePagerAdapter(urlList) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_skin_disease_detailed, container, false)

        diseaseName = view.findViewById(R.id.wiki_detailed_diseaseName)
        diseaseDescription = view.findViewById(R.id.wiki_detailed_diseaseDescription)

        diseasePreviousImage = view.findViewById(R.id.leftArrow)
        diseaseNextImage = view.findViewById(R.id.rightArrow)

        symptomsRecyclerView = view.findViewById(R.id.symptomsRecyclerView)
        symptomsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewPager = view.findViewById(R.id.imageCarousel)
        viewPager.adapter = imgAdapter

        val diseaseNameText = arguments?.getString("diseaseName")
        val diseaseDescriptionText = arguments?.getString("diseaseDescription")
        diseaseName.text = diseaseNameText
        diseaseDescription.text = diseaseDescriptionText

        Utilities.databaseFetch.fetchDisease(diseaseNameText!!) { diseaseInfo ->
            if (diseaseInfo != null) {
                urlList.clear()
                urlList.addAll(diseaseInfo.images)
                val symptomsAdapter = SymptomsAdapter(diseaseInfo.symptoms)
                symptomsRecyclerView.adapter = symptomsAdapter
                imgAdapter.notifyDataSetChanged()
            }
        }

        diseasePreviousImage.setOnClickListener {
            val currentItem = viewPager.currentItem
            if (currentItem > 0) {
                viewPager.currentItem = currentItem - 1
            }
        }
        diseaseNextImage.setOnClickListener {
            val currentItem = viewPager.currentItem
            if (currentItem < urlList.size - 1) {
                viewPager.currentItem = currentItem + 1
            }
        }
        return view
    }
}