package com.example.dermaapplication.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.dermaapplication.R

/**
 * Fragment umożliwiający przedstawienie oraz wykonanie ankiety użytkownikowi. Jest odpowiedzialny
 * za dynamiczne wyświetlanie pytań i odpowiedzi pobranych z bazy danych Firebase.
 */
class FragmentQuestionnaire : Fragment() {
    private lateinit var questionText: TextView
    private lateinit var yesButton: Button
    private lateinit var noButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_question, container, false)





        return view
    }

    /**
     * Funkcja do dynamicznego wyświetlania odpowiedzi w zależności od pytania.
     * Przewidywane odpowiedzi : TAK, NIE, NIEWIEM, Przedział czasowy
     * Ma za zadanie ukrywać niepotrzebne widoki odpowiedzi, a ukazywać potrzebne.
     */
    private fun adjustAnswerTypeToQuestion(){
    }
}