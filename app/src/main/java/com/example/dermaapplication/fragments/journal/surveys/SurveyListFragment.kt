package com.example.dermaapplication.fragments.journal.surveys

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dermaapplication.R
import com.example.dermaapplication.Utilities
import com.example.dermaapplication.fragments.journal.adapters.SurveyListAdapter
import com.example.dermaapplication.items.Survey
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SurveyListFragment : Fragment() {

    private lateinit var surveyAdapter: SurveyListAdapter
    private lateinit var recyclerView: RecyclerView

    private lateinit var goBack: ImageButton
    private lateinit var settingsExpand: ImageButton

    private var settingsExpanded = false
    private lateinit var settings: MaterialCardView
    private lateinit var deleteLayout: LinearLayout
    private lateinit var deleteModeButton: RadioButton
    var deleteModeFlag = false


    private fun setupOnClickListeners() {
        goBack.setOnClickListener {
            fragmentManager?.popBackStack()
        }
        settingsExpand.setOnClickListener {
            if(settingsExpanded){
                settings.visibility = View.GONE
            } else {
                settings.visibility = View.VISIBLE
            }
            settingsExpanded = !settingsExpanded
        }
        deleteModeButton.setOnCheckedChangeListener { buttonView, isChecked ->
            deleteModeFlag = isChecked

        }
    }

    /**
     * Metoda odpowiedzialna za tworzenie widoku fragmentu.
     * Odbiera dane przekazane przez poprzedni fragment w Bundle
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_survey_list, container, false)

        recyclerView = view.findViewById(R.id.surveyRecyclerView)
        goBack = view.findViewById(R.id.survey_previousFragment)
        settingsExpand = view.findViewById(R.id.survey_settings_expand)
        settings = view.findViewById(R.id.survey_settings)
        deleteLayout = view.findViewById(R.id.survey_deleteModeLayout)
        deleteModeButton = view.findViewById(R.id.survey_deleteMode)


        setupRecyclerView()
        loadSurveysFromBundle()
        setupOnClickListeners()

        return view
    }

    /**
     * Metoda odpowiedzialna za konfigurację RecyclerView
     * Tworzy adapter i ustawia go na RecyclerView
     */
    private fun setupRecyclerView() {
        surveyAdapter = SurveyListAdapter {
            deleteModeFlag
        }
        surveyAdapter.onItemDelete = { survey ->
//            showDeleteConfirmationDialog(survey,arguments?.getSerializable("documentId") as String)
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = surveyAdapter
    }

    /**
     * Metoda odbierająca dane przekazane z Bundle
     * Tworzy obiekt Survey na podstawie odpowiedzi i ustawia je w RecyclerView
     */
    private fun loadSurveysFromBundle() {
        // Odbierz listę odpowiedzi ankiet (SurveyItem) przekazanych w Bundle
        val surveyResponses = arguments?.getSerializable("surveyResponses") as? List<Survey>
        surveyAdapter.updateSurveys(surveyResponses)
    }
//    private fun showDeleteConfirmationDialog(survey: Survey, documentId: String){
//        MaterialAlertDialogBuilder(requireContext())
//            .setTitle("Potwierdzenie usunięcia")
//            .setMessage("Czy na pewno chcesz usunąć ankietę?")
//            .setPositiveButton("Tak") { _, _ ->
//                Utilities.databaseFetch.deleteSurveyFromJournal(
//                    documentId,
//                    survey,
//                    onSuccess = {
//                        val updatedSurveys = surveyAdapter.getSurveys().toMutableList()
//                        updatedSurveys.remove(survey)
//                        surveyAdapter.updateSurveys(updatedSurveys)
//                    },
//                    onFailure = {}
//                )
//            }
//            .setNegativeButton("Anuluj"){ dialog , _ ->
//                dialog.dismiss()
//            }
//            .show()
//    }
}

