package com.example.dermaapplication.fragments.docJournal

import android.os.Bundle
import android.text.InputType
import android.util.Log
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
import com.example.dermaapplication.items.JournalRecord
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class JournalDocFragment : Fragment() {

    private lateinit var addJournal: MaterialCardView
    private lateinit var removeJournal: MaterialCardView
    private lateinit var journalsDisplay: RecyclerView
    private val recordsList = mutableListOf<JournalRecord>()
    private val docJournalAdapter by lazy { JournalRecordsAdapter(recordsList) }

    private fun initializeViews(view: View) {
        addJournal = view.findViewById(R.id.journal_doc_add_new_record)
        removeJournal = view.findViewById(R.id.journal_doc_remove_record)
        journalsDisplay = view.findViewById(R.id.journal_doc_RecyclerView)

        journalsDisplay.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = docJournalAdapter
        }
    }

    private fun setupOnClickListeners() {
        addJournal.setOnClickListener { buildDialog() }

        removeJournal.setOnClickListener {
            Toast.makeText(requireContext(), "Funkcja usuwania w trakcie implementacji", Toast.LENGTH_SHORT).show()
        }

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

    private fun fetchDoctorJournalRecords() {
        val doctorUID = FirebaseAuth.getInstance().currentUser?.uid ?: return
        Log.d("FetchDoc", "Fetching doctor records for UID: $doctorUID")

        Utilities.firestore.collection("doctors")
            .whereEqualTo("uid", doctorUID)  // Filtrujemy po UID lekarza
            .get()
            .addOnSuccessListener { task ->
                if (task != null && !task.isEmpty) {
                    val doctorDocument = task.documents.firstOrNull()
                    val journalsIDs = doctorDocument?.get("journals") as? List<String> ?: emptyList()

                    Log.d("FetchDoc", "Doctor's Journal IDs: $journalsIDs")

                    if (journalsIDs.isNotEmpty()) {

                        Utilities.databaseFetch.fetchFilteredJournalRecords(journalsIDs) { journalRecords ->
                            val filteredRecords = journalRecords.filter { it.documentId in journalsIDs }
                            Log.d("FetchedJournals", "Fetched ${filteredRecords.size} journals")

                            recordsList.clear()
                            recordsList.addAll(filteredRecords)
                            docJournalAdapter.notifyDataSetChanged()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Brak powiązanych dzienników", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Błąd pobierania danych lekarza", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Błąd pobierania danych lekarza", Toast.LENGTH_SHORT).show()
                Log.e("FetchDocError", "Błąd pobierania danych lekarza")
            }
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
                    addJournalToDoctor(accessCode)
                    Toast.makeText(requireContext(), "Połączono dziennik z kodem: $accessCode", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Nie znaleziono dziennika z tym kodem.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Błąd połączenia", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addJournalToDoctor(journalCode: String) {
        val doctorId = FirebaseAuth.getInstance().currentUser?.uid
        if (doctorId == null) {
            Toast.makeText(requireContext(), "Błąd: użytkownik nie jest zalogowany.", Toast.LENGTH_SHORT).show()
            Log.e("AddJournalError", "Użytkownik nie jest zalogowany.") // Log dla błędu logowania
            return
        }

        // Wyszukaj dokument w kolekcji "doctors", który posiada pole "uid" równe doctorId
        val doctorsRef = FirebaseFirestore.getInstance().collection("doctors")
        doctorsRef.whereEqualTo("uid", doctorId).get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val doctorDocument = querySnapshot.documents.first()
                    val doctorRef = doctorDocument.reference



                    doctorRef.update("journals", FieldValue.arrayUnion(journalCode))
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Dziennik został pomyślnie dodany.", Toast.LENGTH_SHORT).show()
                            Log.d("AddJournal", "Journal added successfully")
                            fetchDoctorJournalRecords()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Błąd podczas dodawania dziennika", Toast.LENGTH_SHORT).show()
                            Log.e("AddJournalError", "Błąd podczas dodawania dziennika")
                        }
                } else {
                    Toast.makeText(requireContext(), "Błąd: Nie znaleziono dokumentu lekarza.", Toast.LENGTH_SHORT).show()
                    Log.e("AddJournalError", "Nie znaleziono dokumentu lekarza.")
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Błąd podczas wyszukiwania dokumentu lekarza", Toast.LENGTH_SHORT).show()
                Log.e("AddJournalError", "Błąd podczas wyszukiwania dokumentu lekarza", exception)
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
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
