package com.example.dermaapplication

import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dermaapplication.adapters.DiseaseAdapter
import com.example.dermaapplication.database.DatabaseFetch
import com.example.dermaapplication.fragments.ChatFragment
import com.example.dermaapplication.fragments.RegistrationFragment
import com.example.dermaapplication.items.Disease
import com.google.android.gms.tasks.Task
import com.google.android.material.navigation.NavigationView
import com.google.firebase.FirebaseApp
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private val mList = ArrayList<Disease>()
    private val adapter by lazy { DiseaseAdapter(mList) }
    private lateinit var databaseFetch: DatabaseFetch
    private lateinit var menuButton: ImageView
    private lateinit var navigation: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var headerUserName: TextView

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Ustawienie pełnego ekranu
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.insetsController?.apply {
            hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        databaseFetch = DatabaseFetch()

        // Ustawianie parametrów dla obiektów
        setupRecyclerView()
        setupSearchView()
        setupMainMenu()

        fetchDiseasesFromDatabase()
    }


    private fun setupMainMenu() {
        menuButton = findViewById(R.id.home_menu_openButton)
        navigation = findViewById(R.id.nav_view)
        drawerLayout = findViewById(R.id.drawerMenu)

        val headerView = navigation.getHeaderView(0)
        headerUserName = headerView.findViewById(R.id.headerUserName)
        val menu = navigation.menu

        menuButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        navigation.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_login -> {
//                    replaceFragment(RegistrationFragment())
                    replaceFragment(ChatFragment())
                }
                // TODO EDIT TRANSACTIONS
                R.id.menu_home -> {
                    Toast.makeText(this, "Home Page", Toast.LENGTH_SHORT).show()
                }
                R.id.menu_skin -> {
                    Toast.makeText(this, "Problemy skórne", Toast.LENGTH_SHORT).show()
                }
                R.id.menu_ankieta -> {
                    Toast.makeText(this, "Ankieta", Toast.LENGTH_SHORT).show()
                }
                R.id.menu_specialists -> {
                    Toast.makeText(this, "Specjaliści", Toast.LENGTH_SHORT).show()
                }
            }
            for (i in 0 until menu.size()) {
                val currentItem = menu.getItem(i)
                updateIconColor(currentItem, getColor((android.R.color.white)))
                currentItem.isChecked = false
            }
            menuItem.isChecked = true
            updateIconColor(menuItem, getColor(R.color.checked_state_color))
            drawerLayout.closeDrawer(GravityCompat.START)
            true
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
                    filterSearch(newText)
                    return true
                }
            })

            // Nasłuchiwanie na zmianę fokusu
            setOnQueryTextFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    recyclerView.visibility = View.GONE // Ukryj RecyclerView
                } else {
                    recyclerView.visibility = View.VISIBLE // Pokaż RecyclerView
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

    /**
     * Metoda służąca do zmiany koloru itemu w nawigacji
     *
     * @param menuItem - przycisk w nawigacji menu
     * @param color - kolor, na który ma zmienić się ikona
     */
    private fun updateIconColor(menuItem: MenuItem, color: Int) {
        val icon = menuItem.icon
        icon?.let {
            it.setTint(color)
            it.invalidateSelf() // Wymuszenie ponownego odrysowania ikony
        }
    }

    /**
     * Metoda służąca do zamiany fragmentów.
     *
     * @param Fragment - fragment do zmiany
     */
    private fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}
