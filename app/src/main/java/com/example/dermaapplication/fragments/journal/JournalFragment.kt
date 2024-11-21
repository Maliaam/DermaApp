package com.example.dermaapplication.fragments.journal

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dermaapplication.R
import com.example.dermaapplication.Utilities
import com.example.dermaapplication.adapters.JournalRecordsAdapter
import com.example.dermaapplication.items.JournalRecord
import com.google.android.material.card.MaterialCardView
import java.util.Date
import java.util.Locale

class JournalFragment : Fragment() {

    private lateinit var addJournalRecord: MaterialCardView
    private lateinit var removeJournalRecord: MaterialCardView
    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private val recordsList = mutableListOf<JournalRecord>()
    private val journalAdapter by lazy { JournalRecordsAdapter(recordsList) }


    private fun initializeViews(view: View) {
        addJournalRecord = view.findViewById(R.id.journal_add_new_record)
        removeJournalRecord = view.findViewById(R.id.journal_remove_record)
    }
    private fun toggleDeleteMode(){
        journalAdapter.isDeleteMode = !journalAdapter.isDeleteMode
        journalAdapter.notifyDataSetChanged()
    }

    private fun setupOnClickListeners() {
        addJournalRecord.setOnClickListener {
            val newRecord = JournalRecord(
                id = 1,
                userUID = Utilities.getCurrentUserUid(),
                recordTitle = "Wpis #1",
                date = Date(),
                imageUrls = listOf(),
                frontPins = null,
                backPins = null,
                surveyResponses = null,
                additionalNotes = null
            )

            Utilities.databaseFetch.addJournalRecordToDatabase(
                newRecord,
                onSuccess = {
                    Toast.makeText(
                        requireContext(),
                        "Wpis został pomyślnie dodany do dziennika!",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onFailure = { exception ->
                    Toast.makeText(
                        requireContext(),
                        "Wystąpił błąd podczas dodawania wpisu: ${exception.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            )
        }
        removeJournalRecord.setOnClickListener {
            toggleDeleteMode()
        }
    }


    /**
     * Filtruje oraz aktualizuje listę wpisów dziennika na podstawie wprowadzonego zapytania
     * w SearchView.
     *
     * @param query Zapytanie wyszukiwania wpisane przez użytkownika do SearchView.
     */
    private fun filterSearch(query: String?) = if (!query.isNullOrEmpty()) {
        val filteredQuery = ArrayList<JournalRecord>()
        val lowerCaseQuery = query.lowercase(Locale.ROOT)
        for (data in recordsList) {
            if (data.recordTitle.lowercase(Locale.ROOT).contains(lowerCaseQuery)) {
                filteredQuery.add(data)
            }
        }
        journalAdapter.setFilteredList(filteredQuery)
    } else {
        recyclerView.visibility = View.VISIBLE
        journalAdapter.setFilteredList(recordsList)
    }

    /**
     * Konfiguruje RecyclerView oraz jego adapter.
     */
    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.journal_RecyclerView)
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager =
                LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.VERTICAL,
                    false
                )
            this.adapter = journalAdapter
        }
    }

    private fun initializeOnItemClick() {
        journalAdapter.onItemClick = { record ->
            Toast.makeText(requireContext(), record.recordTitle, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSearchView(view: View) {
        searchView = view.findViewById(R.id.journal_searchView)
        searchView.apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    recyclerView.visibility = View.VISIBLE
                    filterSearch(query)
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    filterSearch(newText)
                    return true
                }
            })
            setOnQueryTextFocusChangeListener { _, hasFocus ->
                if (!hasFocus) recyclerView.visibility = View.GONE
                else recyclerView.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_journal, container, false)

        initializeViews(view)
        setupRecyclerView(view)
        setupSearchView(view)
        setupOnClickListeners()
        initializeOnItemClick()


        Utilities.databaseFetch.fetchJournalRecords { records ->
            if (records.isNotEmpty()) {
                recordsList.clear()
                recordsList.addAll(records) // records powinno być listą, a nie tablicą
                journalAdapter.notifyDataSetChanged()
            }
        }
        return view
    }

}