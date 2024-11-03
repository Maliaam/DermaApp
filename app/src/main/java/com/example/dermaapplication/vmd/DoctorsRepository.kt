package com.example.dermaapplication.vmd

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.dermaapplication.Utilities
import com.example.dermaapplication.items.Users

/**
 * Klasa odpowiedzialna za pobieranie i przechowywanie danych o doktorach w bazie danych Firebase.
 * Używana jest jako repozytorium, które dostarcza dane do innych komponentów aplikacji poprzez
 * obiekty LiveData.
 */
class DoctorsRepository {

    // Przechowuje listę obiektów typu 'Users' reprezentujących doktorów
    private val doctors = MutableLiveData<List<Users>>()

    // Przechowuje listę UID doktorów
    private val doctorsUIDList = MutableLiveData<List<String>>()

    // Przechowuje listę UID użytkowników
    private val usersUIDList = MutableLiveData<List<String>>()

    /**
     * Inicjalizacja wstępnych funkcji do pobrania wiadomości z bazy danych w celu przekazania ich
     * do zmiennych.
     */
    init {
        fetchDoctors()
        fetchDoctorsID()
        fetchUsersID()
    }

    /**
     * Pobiera listę doktorów w kolekcji "doctors" z bazy danych Firebase, a następnie aktualizuje
     * listę obiektów typu 'Users'. Dane są aktualizowane na bieżąco w przypadku jakiejś zmiany.
     */
    private fun fetchDoctors() {
        Utilities.firestore.collection("doctors").addSnapshotListener { value, error ->
            if (error != null) {
                Log.e("DEBUGGING", "Error fetching doctors", error)
                return@addSnapshotListener
            }
            val doctorList = value?.documents?.mapNotNull { documentSnapshot ->
                documentSnapshot.toObject(Users::class.java)
            } ?: emptyList()
            doctors.value = doctorList
        }
    }

    /**
     * Pobiera listę UID doktorów w kolekcji "doctors" z bazy danych Firebase, a następnie
     * aktualizuje listę 'doctorsUIDList'.
     */
    private fun fetchDoctorsID() {
        Utilities.firestore.collection("doctors").get().addOnSuccessListener { value ->
            val uidList = value.documents.mapNotNull { it.getString("uid") }
            doctorsUIDList.value = uidList
            Log.d("DoctorsRepository", uidList.toString())
        }
    }

    /**
     * Pobiera listę UID użytkowników w kolekcji "users" z bazy danych Firebase, a następnie
     * aktualizuje listę 'usersUIDList'.
     */
    private fun fetchUsersID() {
        Utilities.firestore.collection("users").get().addOnSuccessListener { value ->
            val uidList = value.documents.mapNotNull { it.getString("uid") }
            usersUIDList.value = uidList
        }
    }

    /**
     * Zwraca listę obietków 'Users' reprezentujących doktorów.
     *
     * @return LiveData<List<Users>> z listą obiektów klasy Users.
     */
    fun getDoctors(): LiveData<List<Users>> = doctors

    /**
     * Zwraca listę Stringów, które reprezentują UID lekarzy.
     *
     * @return LiveData<List<String>> z listą UID lekarzy.
     */
    fun getDoctorsUIDs(): LiveData<List<String>> = doctorsUIDList

    /**
     * Zwraca listę Stringów, które reprezentują UID użytkowników
     *
     * @return LiveData<List<String>> z listą UID użytkowników.
     */
    fun getUsersUIDs(): LiveData<List<String>> = usersUIDList
}