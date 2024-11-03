package com.example.dermaapplication.fragments.wikiFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.dermaapplication.R

class SkinDiseaseDetailedFragment : Fragment() {
    private lateinit var diseaseName: TextView
    private lateinit var diseaseDescription: TextView
    private lateinit var diseasePreviousImage: ImageView
    private lateinit var diseaseNextImage: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_skin_disease_detailed, container,
            false)

        diseaseName = view.findViewById(R.id.wiki_detailed_diseaseName)
        diseaseDescription = view.findViewById(R.id.wiki_detailed_diseaseDescription)

        diseasePreviousImage = view.findViewById(R.id.leftArrow)
        diseaseNextImage = view.findViewById(R.id.rightArrow)

        diseaseName.text = arguments?.getString("diseaseName")
        diseaseDescription.text = arguments?.getString("diseaseDescription")





        return view
    }
}