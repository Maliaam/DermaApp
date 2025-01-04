package com.example.dermaapplication.fragments.journal

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dermaapplication.MainActivity
import com.example.dermaapplication.R
import com.example.dermaapplication.Utilities
import com.example.dermaapplication.fragments.journal.adapters.JournalImagesAdapter
import com.example.dermaapplication.fragments.journal.adapters.NotesAdapter
import com.example.dermaapplication.fragments.journal.notes.NotesFragment
import com.example.dermaapplication.fragments.journal.surveys.SurveyListFragment
import com.example.dermaapplication.items.JournalRecord
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder


@Suppress("DEPRECATION")
class RecordFragment : Fragment() {
    private lateinit var recordTitle: TextView
    private lateinit var imagesRecyclerView: RecyclerView
    private lateinit var shareButton: ImageButton
    private lateinit var editNotesButton: ImageView
    private lateinit var goToCamera: MaterialCardView
    private lateinit var goToImagesFragment: CardView
    private lateinit var goToRecordSurvey: CardView
    private lateinit var goToRecordNotes: CardView
    private lateinit var doctorName: TextView
    private lateinit var doctorInfoLayout: LinearLayout
    private lateinit var doctorInfoLayoutEmpty: LinearLayout
    private lateinit var doctorImage: ImageView
    private var doctorFullName: String? = null
    private var record: JournalRecord? = null
    private var doctorUid: String? = null

    private fun initializeViews(view: View) {
        doctorImage = view.findViewById(R.id.doctor_image)
        recordTitle = view.findViewById(R.id.record_title)
        imagesRecyclerView = view.findViewById(R.id.record_images_recyclerview)
        editNotesButton = view.findViewById(R.id.record_edit_notes)
        goToCamera = view.findViewById(R.id.rightBottomLayout)
        goToImagesFragment = view.findViewById(R.id.record_images)
        goToRecordSurvey = view.findViewById(R.id.record_survey)
        goToRecordNotes = view.findViewById(R.id.record_notes)
        shareButton = view.findViewById(R.id.journal_share)
        doctorName = view.findViewById(R.id.journal_doctor_name)
        doctorInfoLayout = view.findViewById(R.id.doctor_info_layout)
        doctorInfoLayoutEmpty = view.findViewById(R.id.doctor_info_layout_empty)
    }

    private fun initializeData() {
        record = arguments?.getSerializable("record") as? JournalRecord
        recordTitle.text = record?.recordTitle
        doctorFullName = record?.doctorName
        doctorUid = record?.doctorUid
    }

    private fun setupImagesRecyclerView() {
        record?.let {
            val adapter = JournalImagesAdapter(it)
            imagesRecyclerView.layoutManager = GridLayoutManager(context, 3)
            imagesRecyclerView.adapter = adapter
        }
    }

    private fun setupDoctorInfo() {
        if (doctorFullName != null) {
            doctorName.text = doctorFullName
            doctorUid?.let { uid ->
                Utilities.databaseFetch.fetchUserProfileImageUrlByUid(uid) { imageUrl ->
                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.drawable.man)
                            .error(R.drawable.man)
                            .into(doctorImage)
                    }
                }
            }
            doctorInfoLayout.visibility = View.VISIBLE
        } else {
            doctorInfoLayout.visibility = View.GONE
            doctorInfoLayoutEmpty.visibility = View.VISIBLE
        }
    }


    private fun setupOnClickListeners() {
        goToCamera.setOnClickListener {
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
                putSerializable("frontPins",ArrayList(record?.frontPins ?: emptyList()))
                putSerializable("backPins",ArrayList(record?.backPins ?: emptyList()))
            }
            surveyListFragment.arguments = bundle
            (activity as MainActivity).replaceFragment(surveyListFragment)
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
        goToRecordNotes.setOnClickListener {
            val notesFragment = NotesFragment()
            val bundle = Bundle().apply {
                putSerializable("record", arguments?.getSerializable("record"))
            }
            notesFragment.arguments = bundle
            (activity as MainActivity).replaceFragment(notesFragment)

        }
    }

    private fun copyToClipboard(code: String) {
        val clipboard = ContextCompat.getSystemService(requireContext(), ClipboardManager::class.java)
        val clip = ClipData.newPlainText("Generated Code", code)
        clipboard?.setPrimaryClip(clip)
        Toast.makeText(requireContext(), "Kod skopiowany do schowka", Toast.LENGTH_SHORT).show()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_record, container, false)


        initializeViews(view)
        initializeData()
        setupImagesRecyclerView()
        setupOnClickListeners()
        setupDoctorInfo()
        return view
    }

    companion object {
        const val REQUEST_CODE = 101
    }
}