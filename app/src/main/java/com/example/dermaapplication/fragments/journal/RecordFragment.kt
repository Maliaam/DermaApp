package com.example.dermaapplication.fragments.journal

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dermaapplication.MainActivity
import com.example.dermaapplication.R
import com.example.dermaapplication.Utilities
import com.example.dermaapplication.fragments.journal.adapters.JournalImagesAdapter
import com.example.dermaapplication.fragments.journal.adapters.JournalNotesAdapter
import com.example.dermaapplication.items.JournalRecord
import com.example.dermaapplication.items.Note
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class RecordFragment : Fragment() {
    private lateinit var recordTitle: TextView
    private lateinit var imagesRecyclerView: RecyclerView
    private lateinit var notesRecyclerView: RecyclerView
    private lateinit var notesAdapter: JournalNotesAdapter
    private lateinit var editNotesButton: ImageView
    private lateinit var goToAnalyse: MaterialCardView
    private var record: JournalRecord? = null

    private fun initializeViews(view: View) {
        recordTitle = view.findViewById(R.id.record_title)
        imagesRecyclerView = view.findViewById(R.id.record_images_recyclerview)
//        notesRecyclerView = view.findViewById(R.id.record_notes_recyclerview)
        editNotesButton = view.findViewById(R.id.record_edit_notes)
        goToAnalyse = view.findViewById(R.id.rightBottomLayout)
    }

    private fun initializeData() {
        record = arguments?.getSerializable("record") as? JournalRecord
        recordTitle.text = record?.recordTitle
    }

    private fun setupImagesRecyclerView(){
        record?.let {
            val adapter = JournalImagesAdapter(it)
            imagesRecyclerView.layoutManager = GridLayoutManager(context,3)
            imagesRecyclerView.adapter = adapter
        }
    }
    private fun setupNotesRecyclerView(){
        record?.let {
            notesAdapter = JournalNotesAdapter(it.additionalNotes ?: mutableListOf())
            notesRecyclerView.layoutManager = LinearLayoutManager(context)
            notesRecyclerView.adapter= notesAdapter
        }
    }

    private fun setupOnClickListeners() {
        editNotesButton.setOnClickListener {
            setupNoteDialog(requireContext()) { newNote ->
                val updatedNotes = record?.additionalNotes?.toMutableList() ?: mutableListOf()
                updatedNotes.add(newNote)
                record?.additionalNotes = updatedNotes

                record?.documentId?.let { documentId ->
                    Utilities.databaseFetch.updateJournalRecordNote(documentId, updatedNotes,
                        onSuccess = {
                            Log.d("Firestore", "Notatki zaktualizowane pomyślnie")
                            notesAdapter.updateNotes(updatedNotes)
                        },
                        onFailure = { exception ->
                            Log.e("Firestore", "Błąd podczas aktualizacji notatek", exception)
                        })
                }
            }
        }
        goToAnalyse.setOnClickListener {
            val analyseFragment = AnalyseFragment()
            analyseFragment.setTargetFragment(this,REQUEST_CODE)
            (activity as MainActivity).replaceFragment(AnalyseFragment())
        }
    }

    private fun setupNoteDialog(context: Context, onNoteAdded: (Note) -> Unit) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.layout_dialog, null)
        val noteEditText = dialogView.findViewById<EditText>(R.id.note_edit_text)

        MaterialAlertDialogBuilder(context)
            .setTitle("Dodaj notatkę")
            .setView(dialogView)
            .setCancelable(true)
            .setPositiveButton("Zapisz") { dialog, _ ->
                val noteDate = Utilities.getCurrentTime("short")
                val noteContent = noteEditText.text.toString()
                if(noteContent.isNotEmpty()){
                    val newNote = Note(noteDate,noteContent)
                    onNoteAdded(newNote)
                    dialog.dismiss()
                } else {
                    Toast.makeText(context, "Notatka nie może być pusta", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Anuluj") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_add_record, container, false)


        initializeViews(view)
        initializeData()
        setupImagesRecyclerView()
//        setupNotesRecyclerView()
        setupOnClickListeners()
        return view
    }
    companion object{
        const val REQUEST_CODE = 101
    }
}