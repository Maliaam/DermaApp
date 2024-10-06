package com.example.dermaapplication

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dermaapplication.adapters.DiseaseAdapter
import com.example.dermaapplication.items.Disease
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private val mList = ArrayList<Disease>()
    private val adapter by lazy { DiseaseAdapter(mList) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)


        // Ustawianie parametrów dla obiektów
        setupRecyclerView()
        setupSearchView()

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
        }
    }

    private fun setupSearchView() {
        searchView = findViewById<SearchView>(R.id.home_searchView).apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?) = false
                override fun onQueryTextChange(newText: String?) =
                    filterSearch(newText).let { true }
            })
        }
    }

    private fun filterSearch(query: String?) {
        if (query != null) {
            val filteredQuery = ArrayList<Disease>()
            for (data in mList) {
                if (data.name.lowercase(Locale.ROOT).contains(query)) {
                    filteredQuery.add(data)
                }
            }
            if (filteredQuery.isEmpty()) {
                Toast.makeText(this, "No Data found", Toast.LENGTH_SHORT).show()
            } else {
                adapter.setFilteredList(filteredQuery)
            }
        }
    }
}