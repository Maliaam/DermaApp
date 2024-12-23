package com.example.dermaapplication.fragments.wikiFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dermaapplication.MainActivity
import com.example.dermaapplication.R
import com.example.dermaapplication.Utilities
import com.example.dermaapplication.adapters.WikiDiseaseAdapter
import com.example.dermaapplication.items.Disease
import java.util.Locale

/**
 * Fragment przedstawiający wikipedie znajdujących się chorób w bazie danych Firebase.
 */
class SkinDiseasesFragment : Fragment() {
    private val diseasesList = ArrayList<Disease>()
    private val skinAdapter by lazy { WikiDiseaseAdapter(diseasesList) }
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_skin_diseases, container, false)

        setupRecyclerView(view)
        setupSearchView(view)
        initializeOnItemClick()


        Utilities.databaseFetch.fetchDiseases { diseases ->
            if (diseases.isNotEmpty()) {
                diseasesList.clear()
                diseasesList.addAll(diseases)
                skinAdapter.notifyDataSetChanged()
            }

        }
        return view
    }

    /**
     * Inicjalizacja onItemClick dla adapterów. Ustawia opowiedź na kliknięcie w item w RecyclerView.
     * Po kliknięciu w item, otwierany jest nowy fragment dotyczący danej dermatozy, do którego
     * przekazywane są dane poprzez obiekt Bundle.
     */
    private fun initializeOnItemClick() {
        skinAdapter.onItemClick = { disease ->
            val bundle = Bundle().apply {
                putString("diseaseName",disease.name)
                putString("diseaseDescription",disease.description)
            }
            val skinDiseaseDetailedFragment = SkinDiseaseDetailedFragment().apply {
                arguments = bundle
            }
            (activity as? MainActivity)?.replaceFragment(skinDiseaseDetailedFragment)
        }
    }

    /**
     * Konfiguruje RecyclerView oraz jego adapter
     */
    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.wiki_skinMenu_RecyclerView)
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(
                requireContext(), LinearLayoutManager.VERTICAL,
                false
            )
            this.adapter = skinAdapter
        }
    }

    /**
     * Inicjalizacja SearchView z adapterem i potrzebnymi danymi.
     */
    private fun setupSearchView(view: View) {
        searchView = view.findViewById(R.id.wiki_skinMenu_searchView)
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
                if (!hasFocus)
                    recyclerView.visibility = View.GONE
                else
                    recyclerView.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Filtruje oraz aktualizuje listę chorób na podstawie wprowadzonego zapytania w SearchView.
     *
     * @param query Zapytanie wyszukiwania wpisane przez użytkownika do SearchView.
     */
    private fun filterSearch(query: String?) = if (!query.isNullOrEmpty()) {
        val filteredQuery = ArrayList<Disease>()
        val lowerCaseQuery = query.lowercase(Locale.ROOT)
        for (data in diseasesList) {
            if (data.name.lowercase(Locale.ROOT).contains(lowerCaseQuery)) {
                filteredQuery.add(data)
            }
        }
        skinAdapter.setFilteredList(filteredQuery)
    } else {
        recyclerView.visibility = View.VISIBLE
        skinAdapter.setFilteredList(diseasesList)
    }
}
