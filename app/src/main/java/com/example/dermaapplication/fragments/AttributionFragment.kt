package com.example.dermaapplication.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dermaapplication.R
import com.example.dermaapplication.adapters.AttributionAdapter

class AttributionFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_attribution, container, false)
        recyclerView = view.findViewById(R.id.rvAttributions)


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val attributions = listOf(
            "Skin diseases images - https://dermnetnz.org/images",
            "Login icons created by Pixel perfect - Flaticon",
            "Register icons created by waqasshah - Flaticon",
            "Message icons created by onlyhasbi - Flaticon",
            "Plus icons created by dmitri13 - Flaticon",
            "Back arrow icons created by Ilham Fitrotul Hayat - Flaticon",
            "Share icons created by Freepik - Flaticon",
            "More icons created by Kirill Kazachek - Flaticon",
            "Dropdown arrow icons created by Dave Gandy - Flaticon",
            "Correct icons created by Freepik - Flaticon",
            "Dermatology icons created by juicy_fish - Flaticon",
            "Change icons created by yoyonpujiono - Flaticon",
            "Tick icons created by kliwir art - Flaticon",
            "Pencil icons created by Pixel perfect - Flaticon",
            "Analyzing icons created by Ardiansyah - Flaticon",
            "Photo camera icons created by Kiranshastry - Flaticon",
            "Survey icons created by Smashicons - Flaticon",
            "Sticky notes icons created by Freepik - Flaticon",
            "Recycle bin icons created by lakonicon - Flaticon",
            "Close icons created by Pixel perfect - Flaticon",
            "Question icons created by Freepik - Flaticon",
            "Repeat icons created by Freepik - Flaticon",
            "Password icons created by Pixel perfect - Flaticon",
            "Email icons created by Freepik - Flaticon",
            "Search icons created by Chanut - Flaticon",
            "Questionnaire icons created by riajulislam - Flaticon",
            "Dictionary icons created by judanna - Flaticon",
            "Message icons created by Any Icon - Flaticon",
            "Explore icons created by KP Arts - Flaticon",
            "Notification icons created by Luch Phou - Flaticon",
            "Pointer icons created by Alfredo Creates - Flaticon",
            "Diary icons created by Three musketeers - Flaticon",
            "Obraz doktora - Freepik.com",
            "Stock man photo - Freepik.com by Kireyonok_Yuliya",
            "Remove icons created by Royyan Wijaya - Flaticon",
            "Edit icons created by Pixel perfect - Flaticon",
            "Next icons created by Roundicons - Flaticon",
            "Back icons created by Roundicons - Flaticon",
            "Message icons created by Freepik - Flaticon",
            "Camera icons created by Good Ware - Flaticon",
            "Pencil icons created by Pixel perfect - Flaticon",
            "Picture icons created by Pixel perfect - Flaticon"
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = AttributionAdapter(attributions)
    }
}
