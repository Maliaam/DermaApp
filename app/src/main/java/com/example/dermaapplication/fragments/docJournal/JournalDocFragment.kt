package com.example.dermaapplication.fragments.docJournal

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dermaapplication.MainActivity
import com.example.dermaapplication.R
import com.example.dermaapplication.Utilities
import com.example.dermaapplication.fragments.journal.RecordFragment
import com.example.dermaapplication.fragments.journal.adapters.JournalRecordsAdapter
import com.example.dermaapplication.interfaces.JournalRecordsCallback
import com.example.dermaapplication.items.JournalRecord
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore

class JournalDocFragment : Fragment(), JournalRecordsCallback {

    private lateinit var addJournal: MaterialCardView
    private lateinit var removeJournal: MaterialCardView
    private lateinit var journalsDisplay: RecyclerView
    private val recordsList = mutableListOf<JournalRecord>()
    private val docJournalAdapter by lazy { JournalRecordsAdapter(recordsList) }

    private fun initializeViews(view: View) {
        addJournal = view.findViewById(R.id.journal_doc_add_new_record)
        removeJournal = view.findViewById(R.id.journal_doc_remove_record)
        removeJournal.visibility = View.GONE
        journalsDisplay = view.findViewById(R.id.journal_doc_RecyclerView)

        journalsDisplay.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = docJournalAdapter
        }
    }

    private fun setupOnClickListeners() {
        addJournal.setOnClickListener { buildDialog() }
        removeJournal.setOnClickListener {}
        docJournalAdapter.onItemClick = { record ->
            val bundle = Bundle().apply {
                putSerializable("record", record)
            }
            val recordFragment = RecordFragment().apply {
                arguments = bundle
            }
            Utilities.currentJournalRecord = record
            (activity as MainActivity).replaceFragment(recordFragment)
        }
    }

    override fun onJournalRecordsFetched(journalRecords: List<JournalRecord>) {
        recordsList.clear()
        recordsList.addAll(journalRecords)
        docJournalAdapter.notifyDataSetChanged()
    }

    override fun onError(message: String) {
        Toast.makeText(requireContext(),message,Toast.LENGTH_SHORT).show()
    }

    private fun fetchDoctorJournalRecords() {
        Utilities.databaseFetch.fetchDoctorJournalRecords(this)
    }

    private fun buildDialog() {
        val inputEditText = EditText(requireContext()).apply {
            hint = "Wprowadź kod pacjenta"
            inputType = InputType.TYPE_CLASS_TEXT
            setPadding(50, 30, 50, 30)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Dodaj dziennik pacjenta")
            .setMessage("Wprowadź kod dostępu pacjenta, aby połączyć dziennik.")
            .setView(inputEditText)
            .setPositiveButton("Połącz") { dialog, _ ->
                val accessCode = inputEditText.text.toString().trim()
                if (accessCode.isNotEmpty()) {
                    linkJournalWithCode(accessCode)
                } else {
                    Toast.makeText(requireContext(), "Kod nie może być pusty!", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Anuluj") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun linkJournalWithCode(accessCode: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("journals").document(accessCode).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    Utilities.databaseFetch.addJournalToDoctor(accessCode)
                    Toast.makeText(requireContext(), "Połączono dziennik z kodem: $accessCode", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Nie znaleziono dziennika z tym kodem.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_journal_doc, container, false)
        initializeViews(view)
        setupOnClickListeners()
        fetchDoctorJournalRecords()
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        journalsDisplay.adapter = null
    }
}
