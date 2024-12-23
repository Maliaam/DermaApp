package com.example.dermaapplication.fragments.journal

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dermaapplication.MainActivity
import com.example.dermaapplication.R
import com.example.dermaapplication.Utilities
import com.example.dermaapplication.fragments.journal.adapters.JournalImagesAdapter
import com.example.dermaapplication.fragments.journal.adapters.JournalNotesAdapter
import com.example.dermaapplication.fragments.journal.surveys.SurveyListFragment
import com.example.dermaapplication.items.JournalRecord
import com.example.dermaapplication.items.Note
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class RecordFragment : Fragment() {
    private lateinit var recordTitle: TextView
    private lateinit var imagesRecyclerView: RecyclerView
    private lateinit var shareButton: ImageButton
    private lateinit var notesAdapter: JournalNotesAdapter
    private lateinit var editNotesButton: ImageView
    private lateinit var goToAnalyse: MaterialCardView
    private lateinit var goToImagesFragment: CardView
    private lateinit var goToRecordSurvey: CardView
    private lateinit var goToRecordNotes: CardView
    private var record: JournalRecord? = null

    private fun initializeViews(view: View) {
        recordTitle = view.findViewById(R.id.record_title)
        imagesRecyclerView = view.findViewById(R.id.record_images_recyclerview)
        editNotesButton = view.findViewById(R.id.record_edit_notes)
        goToAnalyse = view.findViewById(R.id.rightBottomLayout)
        goToImagesFragment = view.findViewById(R.id.record_images)
        goToRecordSurvey = view.findViewById(R.id.record_survey)
        goToRecordNotes = view.findViewById(R.id.record_notes)
        shareButton = view.findViewById(R.id.journal_share)
    }

    private fun initializeData() {
        record = arguments?.getSerializable("record") as? JournalRecord
        recordTitle.text = record?.recordTitle
    }

    private fun setupImagesRecyclerView() {
        record?.let {
            val adapter = JournalImagesAdapter(it)
            imagesRecyclerView.layoutManager = GridLayoutManager(context, 3)
            imagesRecyclerView.adapter = adapter
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
            val cameraFragment = CameraFragment()
            cameraFragment.setTargetFragment(this, REQUEST_CODE)
            (activity as MainActivity).replaceFragment(CameraFragment())
        }
        goToImagesFragment.setOnClickListener {
            if (!record?.imageUrls.isNullOrEmpty()) {
                val imageUrls = record?.imageUrls ?: emptyList()
                val galleryFragment = ImagesGalleryFragment()
                val bundle = Bundle().apply {
                    putStringArrayList("imageUrls", ArrayList(imageUrls))
                }
                galleryFragment.arguments = bundle

                (activity as MainActivity).replaceFragment(galleryFragment)
            } else {
                Toast.makeText(context, "Brak obrazów do wyświetlenia", Toast.LENGTH_SHORT).show()
            }
        }

        goToRecordSurvey.setOnClickListener {
            val surveyResponses = record?.surveyResponses ?: emptyList()
            val surveyListFragment = SurveyListFragment()
            val bundle = Bundle().apply {
                putSerializable("surveyResponses",ArrayList(surveyResponses))
                putString("documentId",record?.documentId)
            }
            surveyListFragment.arguments = bundle
            (activity as MainActivity).replaceFragment(surveyListFragment)
        }
        goToRecordNotes.setOnClickListener {
            val bundle = Bundle().apply {
            }
        }
        shareButton.setOnClickListener {
            val uniqueCode = record?.documentId


            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Udostępnij kod")
                .setMessage("Kod dostępu dziennika:\n\n" +
                        "$uniqueCode\n\n" +
                        "Prześlij ten kod lekarzowi, aby mógł uzyskać dostęp do Twojego dziennika.")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .setNeutralButton("Kopiuj kod") { _, _ ->
                    if (uniqueCode != null) {
                        copyToClipboard(uniqueCode)
                    }
                }
                .show()
        }
    }

    private fun copyToClipboard(code: String) {
        val clipboard = ContextCompat.getSystemService(requireContext(), ClipboardManager::class.java)
        val clip = ClipData.newPlainText("Generated Code", code)
        clipboard?.setPrimaryClip(clip)
        Toast.makeText(requireContext(), "Kod skopiowany do schowka", Toast.LENGTH_SHORT).show()
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
                if (noteContent.isNotEmpty()) {
                    val newNote = Note(noteDate, noteContent)
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
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_record, container, false)


        initializeViews(view)
        initializeData()
        setupImagesRecyclerView()
        setupOnClickListeners()
        return view
    }

    companion object {
        const val REQUEST_CODE = 101
    }
}