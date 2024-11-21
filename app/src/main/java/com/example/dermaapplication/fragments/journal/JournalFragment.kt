package com.example.dermaapplication.fragments.journal

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.dermaapplication.R
import com.example.dermaapplication.adapters.JournalRecordsAdapter
import com.example.dermaapplication.items.JournalRecord

class JournalFragment : Fragment() {


    private val recordsList = mutableListOf<JournalRecord>()
    private val adapter by lazy { JournalRecordsAdapter(recordsList) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_journal,container,false)





        return view
    }

}