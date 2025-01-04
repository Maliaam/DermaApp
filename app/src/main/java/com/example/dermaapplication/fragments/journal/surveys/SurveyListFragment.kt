package com.example.dermaapplication.fragments.journal.surveys

import android.graphics.Matrix
import android.graphics.RectF
import android.media.Image
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dermaapplication.R
import com.example.dermaapplication.fragments.journal.adapters.SurveyListAdapter
import com.example.dermaapplication.items.Pin
import com.example.dermaapplication.items.Survey
import com.google.android.material.card.MaterialCardView

class SurveyListFragment : Fragment() {

    private lateinit var surveyAdapter: SurveyListAdapter
    private lateinit var recyclerView: RecyclerView

    private lateinit var goBack: ImageButton
    private lateinit var settingsExpand: ImageButton

    private lateinit var frontBodyView: FrameLayout
    private lateinit var backBodyView: FrameLayout

    private var settingsExpanded = false
    private lateinit var settings: MaterialCardView
    private lateinit var deleteLayout: LinearLayout
    private lateinit var deleteModeButton: RadioButton
    private var deleteModeFlag = false


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
        frontBodyView = view.findViewById(R.id.survey_frontView)
        backBodyView = view.findViewById(R.id.survey_backView)

        setupRecyclerView()
        loadSurveysFromBundle()
        setupOnClickListeners()
        loadPinsFromBundle()

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
        val surveyResponses = arguments?.getSerializable("surveyResponses") as? List<Survey>
        surveyAdapter.updateSurveys(surveyResponses)
    }
    private fun loadPinsFromBundle(){
        val frontPins = arguments?.getSerializable("frontPins") as? List<Pin>
        val backPins = arguments?.getSerializable("backPins") as? List<Pin>

        frontPins?.forEach { pin ->
            addPin(frontBodyView, pin)
        }
        backPins?.forEach { pin ->
            addPin(backBodyView, pin)
        }


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
private fun addPin(container: FrameLayout, pin: Pin) {
    val pinView = ImageView(requireContext()).apply {
        layoutParams = FrameLayout.LayoutParams(60, 60).apply {
            setMargins(pin.x.toInt() - 30, pin.y.toInt() - 45, 0, 0)
        }
        setImageResource(R.drawable.pin_image)
        scaleType = ImageView.ScaleType.CENTER_INSIDE
    }
    container.addView(pinView)
}
    private fun getImageBounds(imageView: ImageView): RectF {
        val drawable = imageView.drawable ?: return RectF()
        val matrix = imageView.imageMatrix
        val values = FloatArray(9)
        matrix.getValues(values)
        val left = values[Matrix.MTRANS_X]
        val top = values[Matrix.MTRANS_Y]
        val width = drawable.intrinsicWidth * values[Matrix.MSCALE_X]
        val height = drawable.intrinsicHeight * values[Matrix.MSCALE_Y]
        return RectF(left, top, left + width, top + height)
    }

}

