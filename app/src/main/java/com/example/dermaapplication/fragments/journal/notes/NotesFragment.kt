package com.example.dermaapplication.fragments.journal.notes

import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dermaapplication.R
import com.example.dermaapplication.Utilities
import com.example.dermaapplication.fragments.journal.adapters.NotesAdapter
import com.example.dermaapplication.items.JournalRecord
import com.example.dermaapplication.items.Note
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class NotesFragment : Fragment() {
    private lateinit var notesRecyclerView: RecyclerView
    private lateinit var adapter: NotesAdapter
    private val notes = mutableListOf<Note>()
    private lateinit var addNote: ImageView
    private lateinit var sortNotes: ImageView
    private lateinit var helpNotes: ImageView
    private lateinit var previousFragment: ImageView
    private var record: JournalRecord? = null
    private var isSortedAscending = false

    private fun initializeData(){
        record = arguments?.getSerializable("record") as? JournalRecord
    }

    private fun initializeViews(view: View) {
        notesRecyclerView = view.findViewById(R.id.notesRecyclerView)
        addNote = view.findViewById(R.id.notes_add_button)
        sortNotes = view.findViewById(R.id.notes_sort_button)
        helpNotes = view.findViewById(R.id.notes_help_button)
        previousFragment = view.findViewById(R.id.notes_previousFragment)
    }

    private fun setupOnClickLListeners() {
        previousFragment.setOnClickListener {
            fragmentManager?.popBackStack()
        }
        addNote.setOnClickListener {
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_note, null)
            val noteInput = dialogView.findViewById<EditText>(R.id.noteInput)

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Dodaj notatkę")
                .setView(dialogView)
                .setPositiveButton("Dodaj") { _, _ ->
                    val noteContent = noteInput.text.toString()
                    if (noteContent.isNotEmpty()) {
                        val newNote = Note(
                            date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                            content = noteContent
                        )
                        saveNoteToDatabase(newNote)
                    }
                }
                .setNegativeButton("Anuluj", null)
                .show()
        }
        sortNotes.setOnClickListener {
            isSortedAscending = !isSortedAscending
            sortNotesList()
        }
        helpNotes.setOnClickListener {
            Utilities.infoDialogBuilder(
                requireContext(),
                "Informacje dodatkowe",
                resources.getString(R.string.notes_help),
                "Rozumiem"
            )
        }
    }

    private fun loadNotesFromRecord(notesList: List<Note>){
        notes.clear()
        notes.addAll(notesList)
        notes.sortByDescending { it.date }
    }

    private fun sortNotesList() {
        if (isSortedAscending) {
            notes.sortBy { it.date }
        }else {
            notes.sortByDescending { it.date }
        }
        adapter.notifyDataSetChanged()
    }

    private fun saveNoteToDatabase(newNote: Note){
        val documentId = record?.documentId!!
        Log.e("NotesFragment",documentId)
        val recordRef = FirebaseFirestore.getInstance().collection("journals")
            .document(documentId)

        FirebaseFirestore.getInstance().runTransaction { transaction ->
            val snapshot = transaction.get(recordRef)
            val currentNotes = snapshot.get("additionalNotes") as? List<Note> ?: mutableListOf()
            val updatedNotes = currentNotes.toMutableList().apply {
                add(newNote)
            }
            transaction.update(recordRef, "additionalNotes", updatedNotes)
        }.addOnSuccessListener {
            notes.add(newNote)
            sortNotesList()
            adapter.notifyDataSetChanged()
        }

    }
    private fun setupRecyclerView(){
        adapter = NotesAdapter(notes)
        notesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        notesRecyclerView.adapter = adapter
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notes, container, false)
        initializeViews(view)
        setupRecyclerView()
        setupOnClickLListeners()
        initializeData()
        record?.additionalNotes?.let { loadNotesFromRecord(it) }


        return view
    }
}