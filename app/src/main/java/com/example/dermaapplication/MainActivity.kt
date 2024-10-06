package com.example.dermaapplication

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dermaapplication.adapters.DiseaseAdapter
import com.example.dermaapplication.database.DatabaseFetch
import com.example.dermaapplication.items.Disease
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private val mList = ArrayList<Disease>()
    private val adapter by lazy { DiseaseAdapter(mList) }
    private lateinit var databaseFetch: DatabaseFetch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        databaseFetch = DatabaseFetch()

        // Ustawianie parametrów dla obiektów
        setupRecyclerView()
        setupSearchView()

        fetchDiseasesFromDatabase()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawerMenu)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById<RecyclerView>(R.id.home_search_recyclerView).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
            visibility = View.GONE // Ustawiam początkowo na GONE - żeby nie był widoczny
        }
    }

    private fun setupSearchView() {
        searchView = findViewById<SearchView>(R.id.home_searchView).apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    recyclerView.visibility = View.VISIBLE
                    filterSearch(query)
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    adapter.setFilteredList(mList)
                    filterSearch(newText)
                    return true
                }
            })
            setOnQueryTextFocusChangeListener{_, hasFocus ->
                if(hasFocus){
                    recyclerView.visibility = View.VISIBLE
                }
            }
        }
    }


    private fun filterSearch(query: String?) {
        if (!query.isNullOrEmpty()) {
            val filteredQuery = ArrayList<Disease>()
            val lowerCaseQuery = query.lowercase(Locale.ROOT)
            for (data in mList) {
                if (data.name.lowercase(Locale.ROOT).contains(lowerCaseQuery)) {
                    filteredQuery.add(data)
                }
            }
                adapter.setFilteredList(filteredQuery)
        } else {
            recyclerView.visibility = View.VISIBLE
            adapter.setFilteredList(mList)
        }
    }


    private fun fetchDiseasesFromDatabase() {
        databaseFetch.fetchDiseasesNames().addOnCompleteListener { task: Task<List<String>> ->
            if (task.isSuccessful) {
                val diseasesNames: List<String> =
                    task.result ?: emptyList()
                mList.clear()
                for (name in diseasesNames) {
                    val disease = Disease(name)
                    mList.add(disease)
                }
                adapter.notifyDataSetChanged()
            } else {
                Toast.makeText(this, "Failed to fetch data", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
