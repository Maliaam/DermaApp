package com.example.dermaapplication.fragments.journal

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dermaapplication.MainActivity
import com.example.dermaapplication.R
import com.example.dermaapplication.Utilities
import com.example.dermaapplication.fragments.journal.adapters.JournalRecordsAdapter
import com.example.dermaapplication.items.JournalRecord
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Locale

/**
 * Fragment odpowiedzialny za wyświetlanie i zarządzanie wpisami w dzienniku.
 *
 * Funkcjonalności:
 * - Wyświetlanie listy wpisów w RecyclerView z możliwością przeglądania szczegółów.
 * - Dodawanie nowych wpisów do dziennika poprzez dialog.
 * - Usuwanie wpisów z listy w trybie usuwania.
 * - Wyszukiwanie wpisów w czasie rzeczywistym za pomocą SearchView.
 * - Pobieranie danych wpisów z bazy danych Firebase i dynamiczne ich wyświetlanie.
 *
 * Kluczowe komponenty:
 * - `RecyclerView` z adapterem do wyświetlania listy wpisów.
 * - `SearchView` do filtrowania wyników na podstawie tytułu wpisu.
 * - `MaterialCardView` do obsługi dodawania i usuwania wpisów.
 *
 * Fragment jest integralną częścią aplikacji i współpracuje z bazą danych Firebase oraz elementami
 * interfejsu użytkownika.
 */

class JournalFragment : Fragment() {

    private lateinit var addJournalRecord: MaterialCardView
    private lateinit var removeJournalRecord: MaterialCardView
    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private val recordsList = mutableListOf<JournalRecord>()
    private val journalAdapter by lazy { JournalRecordsAdapter(recordsList) }

    /**
     * Inicjalizuje widoki fragmentu.
     * @param view Widok główny fragmentu, używany do znajdowania elementów.
     */
    private fun initializeViews(view: View) {
        addJournalRecord = view.findViewById(R.id.journal_add_new_record)
        removeJournalRecord = view.findViewById(R.id.journal_remove_record)
    }

    /**
     * Przełącza flagę usuwania wpisów dziennika.
     * Gdy tryb usuwania jest aktywny, użytkownik może usuwać elementy z listy.
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun toggleDeleteMode() {
        journalAdapter.isDeleteMode = !journalAdapter.isDeleteMode
        journalAdapter.notifyDataSetChanged()
    }

    /**
     * Wyświetla dialog umożliwiający dodanie nowego wpisu do dziennika.
     * Użytkownik wpisuje tytuł, który jest przekazywany do callbacku po kliknięciu "OK".
     *
     * @param context Context wymagany do wyświetlenia dialogu.
     * @param onTitleEntered Callback uruchamiany po wprowadzeniu tytułu przez użytkownika.
     */
    private fun showAddRecordDialog(context: Context, onTitleEntered: (String) -> Unit) {
        val editText = EditText(context).apply {
            hint = "Wpisz tytuł"
            setPadding(16, 16, 16, 16)
            filters = arrayOf(InputFilter.LengthFilter(14))
        }
        MaterialAlertDialogBuilder(context)
            .setTitle("Dodaj wpis")
            .setView(editText)
            .setPositiveButton("OK") { dialog, _ ->
                val title = editText.text.toString()
                if (title.isNotEmpty()) {
                    onTitleEntered(title)
                }
                dialog.dismiss()
                journalAdapter.notifyDataSetChanged()
            }.setNegativeButton("Anuluj") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    /**
     * Konfiguruje nasłuchiwanie na kliknięcia przycisków.
     * Obsługuje dodawanie nowych wpisów oraz przełączanie trybu usuwania.
     */
    private fun setupOnClickListeners() {
        addJournalRecord.setOnClickListener {
            showAddRecordDialog(requireContext()) { title ->
                val newRecord = JournalRecord(
                    userUID = Utilities.getCurrentUserUid(),
                    recordTitle = title,
                    date = "Data dodania: " + Utilities.getCurrentTime("short"),
                    imageUrls =  mutableListOf(),
                    frontPins = null,
                    backPins = null,
                    surveyResponses = null,
                    additionalNotes = null,
                    documentId = null
                )

                Utilities.databaseFetch.addJournalRecordToDatabase(
                    newRecord,
                    onSuccess = { documentId ->
                        newRecord.documentId = documentId

                        recordsList.add(newRecord)
                        journalAdapter.notifyItemInserted(recordsList.size - 1)
                        Toast.makeText(requireContext(), "Dodano wpis!", Toast.LENGTH_SHORT).show()
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
        }

        removeJournalRecord.setOnClickListener {
            toggleDeleteMode()
        }
    }

    /**
     * Filtruje i aktualizuje listę wpisów dziennika w oparciu o zapytanie użytkownika.
     * Jeśli zapytanie jest puste, przywracana jest oryginalna lista.
     *
     * @param query Tekst zapytania wprowadzony przez użytkownika w SearchView.
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
     * Konfiguruje RecyclerView oraz przypisuje mu adapter.
     * Ustawia układ listy na pionowy.
     *
     * @param view Widok fragmentu, używany do odnalezienia RecyclerView.
     */
    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById<RecyclerView>(R.id.journal_RecyclerView).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = journalAdapter
        }
    }

    /**
     * Ustawia obsługę kliknięcia na elementy listy dziennika.
     * Wyświetla komunikat Toast z tytułem wybranego wpisu.
     */
    private fun initializeOnItemClick() {
        journalAdapter.onItemClick = { record ->
            val bundle = Bundle().apply {
                putSerializable("record",record)
            }
            val recordFragment = RecordFragment().apply {
                arguments = bundle
            }
            Utilities.currentJournalRecord = record
            (activity as MainActivity).replaceFragment(recordFragment)
        }
        journalAdapter.onDeleteClick = { record ->
            showDeleteConfirmationDialog(record)
        }
    }

    /**
     * Konfiguruje SearchView, umożliwiając wyszukiwanie wpisów w dzienniku.
     * Obsługuje zdarzenia wprowadzania tekstu oraz zmiany focusu na SearchView.
     *
     * @param view Widok fragmentu używany do odnalezienia SearchView.
     */
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
    /**
     * Funkcja wyświetla okno dialogowe, w którym użytkownik musi potwierdzić zamiar usunięcia wpisu.
     * Po potwierdzeniu wpis jest usuwany z bazy danych Firebase na podstawie `documentId`.
     * Jeśli operacja zakończy się sukcesem, wpis jest również usuwany z lokalnej listy `recordsList`,
     * a adapter `RecyclerView` zostaje powiadomiony o zmianie, co powoduje odświeżenie widoku.
     *
     * @param record Obiekt `JournalRecord`, który ma zostać usunięty.
     */

    private fun showDeleteConfirmationDialog(record: JournalRecord) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Potwierdzenie usunięcia")
            .setMessage("Czy na pewno chcesz usunąć ten wpis?")
            .setPositiveButton("Tak") { dialog, _ ->
                record.documentId?.let {
                    Utilities.databaseFetch.removeJournalByDocumentId(it, onSuccess = {
                        val position = recordsList.indexOf(record)
                        if (position != -1) {
                            recordsList.removeAt(position)
                            journalAdapter.notifyItemRemoved(position)
                            Toast.makeText(requireContext(), "Wpis został usunięty", Toast.LENGTH_SHORT).show()
                        }
                    }, onFailure = { exception ->
                        Toast.makeText(requireContext(), "Błąd: ${exception.message}", Toast.LENGTH_SHORT).show()
                    })
                }
                dialog.dismiss()
            }
            .setNegativeButton("Anuluj") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * Pobiera dane wpisów z bazy danych.
     */
    private fun fetchJournalRecord() {
        Utilities.databaseFetch.fetchJournalRecords { records ->
            if (records.isNotEmpty()) {
                recordsList.clear()
                recordsList.addAll(records)
            }
            journalAdapter.notifyDataSetChanged()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_journal, container, false)

        initializeViews(view)
        setupRecyclerView(view)
        setupSearchView(view)
        setupOnClickListeners()
        initializeOnItemClick()
        fetchJournalRecord()
        return view
    }
}