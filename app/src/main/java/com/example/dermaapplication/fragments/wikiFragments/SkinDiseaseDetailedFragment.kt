package com.example.dermaapplication.fragments.wikiFragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.example.dermaapplication.R
import com.example.dermaapplication.Utilities
import com.example.dermaapplication.adapters.ImagePagerAdapter
import com.example.dermaapplication.adapters.SymptomsAdapter

/**
 * Fragment wyświetlający użytkownikowi szczegółowe informacje oraz zdjęcia na temat danego
 * schorzenia skórnego.
 */
class SkinDiseaseDetailedFragment : Fragment() {
    private lateinit var diseaseName: TextView
    private lateinit var diseaseDescription: TextView
    private lateinit var diseasePreviousImage: ImageView
    private lateinit var diseaseNextImage: ImageView
    private lateinit var viewPager: ViewPager
    private lateinit var symptomsRecyclerView: RecyclerView
    private lateinit var imageCounter: TextView
    private val urlList = mutableListOf<String>()
    private val imgAdapter by lazy { ImagePagerAdapter(urlList) }

    /**
     * Inicjalizuje widoki w layout'cie.
     * Ustawia wszystkie zmienne widoków, które będą używane w tym fragmencie.
     */
    private fun initializeViews(view: View) {
        viewPager = view.findViewById(R.id.imageCarousel)
        diseaseName = view.findViewById(R.id.wiki_detailed_diseaseName)
        diseaseDescription = view.findViewById(R.id.wiki_detailed_diseaseDescription)
        diseasePreviousImage = view.findViewById(R.id.leftArrow)
        diseaseNextImage = view.findViewById(R.id.rightArrow)
        symptomsRecyclerView = view.findViewById(R.id.symptomsRecyclerView)
        imageCounter = view.findViewById(R.id.image_counter)
    }

    /**
     * Konfiguruje RecyclerView, który wyświetla listę symptomów danej choroby.
     * Ustawia LayoutManager na LinearLayoutManager, aby umożliwić przewijanie w pionie.
     */
    private fun setupSymptomsRecyclerView() {
        symptomsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    /**
     * Konfiguruje ViewPager, który wyświetla obrazy chorób.
     * Ustawia adapter (ImagePagerAdapter) oraz funkcjolaności nawigacji obrazami.
     */
    private fun setupImagePager() {
        viewPager.adapter = imgAdapter
        updateImageCounter()
        diseasePreviousImage.setOnClickListener { navigateImage(-1) }
        diseaseNextImage.setOnClickListener { navigateImage(1) }
    }

    /**
     * Odpowiedzialna za zmianę aktualnie wyświetlonego obrazu w ViewPager.
     * @param direction Określa czy obraz ma być przewinięty w lewo czy w prawo.
     */
    private fun navigateImage(direction: Int) {
        val currentItem = viewPager.currentItem
        val newItem = currentItem + direction
        if (newItem in 0 until urlList.size) {
            viewPager.currentItem = newItem
            updateImageCounter()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateImageCounter(){
        val currentItem = viewPager.currentItem
        val totalImages = urlList.size
        imageCounter.text = "${currentItem + 1}/$totalImages"
    }

    /**
     * Aktualizuje szczegóły choroby w interfejsie użytkownika.
     * Ustawia tekst w widokach diseaseName oraz diseaseDescription na podstawie przekazanych
     * argumentów w bundle.
     * Jeśli argumenty są null, to funkcja nie wykonuje żadnych operacji.
     */
    private fun updateDiseaseDetails() {
        val diseaseNameText = arguments?.getString("diseaseName") ?: return
        val diseaseDescriptionText = arguments?.getString("diseaseDescription") ?: return
        diseaseName.text = diseaseNameText
        diseaseDescription.text = diseaseDescriptionText
    }

    /**
     * Pobiera z bazy danych dane dotyczące choroby.
     * Na podstawie nazwy choroby pobiera url zdjęć oraz informacje o symptomach, a następnie
     * aktualizuje te informacje w widoku.
     */
    private fun fetchDiseaseData() {
        val diseaseNameText = arguments?.getString("diseaseName") ?: return
        Utilities.databaseFetch.fetchDisease(diseaseNameText) { diseaseInfo ->
            diseaseInfo?.let {
                urlList.clear()
                urlList.addAll(it.images)
                val symptomsAdapter = SymptomsAdapter(it.symptoms)
                symptomsRecyclerView.adapter = symptomsAdapter
                imgAdapter.notifyDataSetChanged()
                updateImageCounter()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_skin_disease_detailed, container, false)

        initializeViews(view)
        setupSymptomsRecyclerView()
        setupImagePager()
        updateDiseaseDetails()
        fetchDiseaseData()

        return view
    }

}