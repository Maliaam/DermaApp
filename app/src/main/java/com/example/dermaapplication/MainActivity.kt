package com.example.dermaapplication

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dermaapplication.adapters.DiseaseAdapter
import com.example.dermaapplication.fragments.FragmentQuestionnaire
import com.example.dermaapplication.fragments.HomeFragment
import com.example.dermaapplication.fragments.LoginFragment
import com.example.dermaapplication.fragments.wikiFragments.SkinDiseasesFragment
import com.example.dermaapplication.fragments.SpecialistsFragment
import com.example.dermaapplication.fragments.chatFragments.ChatMenuFragment
import com.example.dermaapplication.items.Disease
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import java.util.Locale

/**
 * Główna klasa aplikacji, zarządza interfejsem użytkownika oraz umożliwia nawigację pomiędzy
 * fragmentami. Odpowiada za obsługę głownego oraz dolnego menu nawigacyjnego.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val mList = ArrayList<Disease>()
    private val adapter by lazy { DiseaseAdapter(mList) }
    private lateinit var menuButton: ImageView
    private lateinit var navigation: NavigationView
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var headerUserName: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Utilities.getCurrentUserUid() != "") {
            Utilities.auth.signOut()
        }

        // Inicjalizacja UI
        setupRecyclerView()
        setupMainMenu()
        setupBottomMenu()
    }

    /**
     * Inicjalizuje dolne menu nawigacyjne i obsługuje wybór jego elementów.
     */
    private fun setupBottomMenu() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_home -> {
                    replaceFragment(HomeFragment())
                    true
                }

                R.id.wiki -> {
                    replaceFragment(SkinDiseasesFragment())
                    true
                }

                R.id.ankieta -> {
                    replaceFragment(FragmentQuestionnaire())
                    true
                }

                R.id.chat -> {
                    replaceFragment(ChatMenuFragment())
                    true
                }

                else -> false
            }
        }
    }

    /**
     * Inicjalizuje menu główne i obsługuje wybór jego elementów.
     */
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


        //TODO: Edytować menu nawigacyjne w zależności od typu konta
        navigation.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_login -> {
                    replaceFragment(LoginFragment())
                }

                R.id.menu_home -> {
                    replaceFragment(HomeFragment())
                }

                R.id.menu_chat -> {
                    replaceFragment(ChatMenuFragment())
                }

                R.id.menu_skin -> {
                    replaceFragment(SkinDiseasesFragment())
                }

                R.id.menu_ankieta -> {
                    replaceFragment(FragmentQuestionnaire())
                }

                R.id.menu_specialists -> {
                    replaceFragment(SpecialistsFragment())
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

    /**
     * Inicjalizacja RecyclerView z adapterem i potrzebnymi danymi.
     */
    private fun setupRecyclerView() {
        recyclerView = findViewById<RecyclerView>(R.id.home_search_recyclerView).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
            visibility = View.GONE
        }
    }

    /**
     * Aktualizuje kolor ikon w menu nawigacyjnym w zależności od wybranego elementu.
     *
     * @param menuItem Element menu, którego kolor ma zostać zmieniony.
     * @param color Kolor, na który zmieniony zostaje zabarwienie ikony.
     */
    private fun updateIconColor(menuItem: MenuItem, color: Int) {
        val icon = menuItem.icon
        icon?.let {
            it.setTint(color)
            it.invalidateSelf()
        }
    }

    /**
     * Zmienia aktualnie wyświetlany fragment na inny.
     *
     * @param fragment Klasa fragmentu, na który ma zostać zmieniony aktualny fragment.
     */
    fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    /**
     * Ukrywa dolne menu nawigacyjne oraz FloatingActionButton.
     */
    fun hideBottomNav() {
        findViewById<BottomAppBar>(R.id.bottomAppBar).visibility = View.GONE
        findViewById<FloatingActionButton>(R.id.fab_user).visibility = View.GONE
        findViewById<BottomNavigationView>(R.id.bottomNavigationView).visibility = View.GONE
    }

    /**
     * Pokazuje dolne menu nawigacyjne oraz FloatingActionButton.
     */
    fun showBottomNav() {
        findViewById<BottomAppBar>(R.id.bottomAppBar).visibility = View.VISIBLE
        findViewById<FloatingActionButton>(R.id.fab_user).visibility = View.VISIBLE
        findViewById<BottomNavigationView>(R.id.bottomNavigationView).visibility = View.VISIBLE
    }
}
