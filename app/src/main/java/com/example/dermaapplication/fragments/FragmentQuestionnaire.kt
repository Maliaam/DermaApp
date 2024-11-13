package com.example.dermaapplication.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.dermaapplication.R
import com.example.dermaapplication.Utilities
import com.example.dermaapplication.items.Question

/**
 * Fragment umożliwiający przedstawienie oraz wykonanie ankiety użytkownikowi. Jest odpowiedzialny
 * za dynamiczne wyświetlanie pytań i odpowiedzi pobranych z bazy danych Firebase.
 */
class FragmentQuestionnaire : Fragment() {
    private lateinit var questionText: TextView
    private lateinit var yesButton: Button
    private lateinit var noButton: Button
    private lateinit var backButton: Button
    private lateinit var spinner: Spinner
    private lateinit var dotContainer: LinearLayout
    private val questionsList = ArrayList<Question>()
    private val userAnswers = mutableListOf<String?>()
    private var currentQuestionIndex = 0
    private var totalQuestionsNumber =  0
    private var isUserInteracting = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_question, container, false)

        questionText = view.findViewById(R.id.questionText)
        yesButton = view.findViewById(R.id.buttonYes)
        noButton = view.findViewById(R.id.buttonNo)
        backButton = view.findViewById(R.id.buttonPrevious)
        spinner = view.findViewById(R.id.answersSpinner)
        dotContainer = view.findViewById(R.id.dotContainer)


        Utilities.databaseFetch.fetchQuestions { questions ->
            questionsList.clear()
            questionsList.addAll(questions)
            userAnswers.clear()
            userAnswers.addAll(List(questionsList.size) { null })
            totalQuestionsNumber = questionsList.size
            initializeDots(totalQuestionsNumber)
            displayQuestion(currentQuestionIndex)
            updateDotIndicator(0)
        }


        yesButton.setOnClickListener { onAnswerSelected("Tak") }
        noButton.setOnClickListener { onAnswerSelected("Nie") }
        backButton.setOnClickListener { goBack() }

        return view
    }

    /**
     * Funkcja do dynamicznego wyświetlania odpowiedzi w zależności od ich ilości.
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun adjustAnswerTypeToQuestion(question: Question) {
        val answers = question.answers
        isUserInteracting = false
        if (answers.size >= 3) {
            yesButton.visibility = View.GONE
            noButton.visibility = View.GONE
            spinner.visibility = View.VISIBLE

            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, answers)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter

            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (isUserInteracting) {
                        val selectedAnswer = answers[position]
                        userAnswers[currentQuestionIndex] = selectedAnswer
                        onAnswerSelected(selectedAnswer)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?){} }
        } else {
            yesButton.visibility = View.VISIBLE
            noButton.visibility = View.VISIBLE
            spinner.visibility = View.GONE
        }
        spinner.setOnTouchListener { _, _ ->
            isUserInteracting = true
            false
        }
    }

    /**
     * Funkcja do wyświetlania pytania na podstawie indeksu i odpowiednich widoków odpowiedzi.
     */
    private fun displayQuestion(index: Int) {
        if (index >= questionsList.size) return

        val question = questionsList[index]
        questionText.text = question.question
        adjustAnswerTypeToQuestion(question)
    }

    private fun onAnswerSelected(answer: String) {
        userAnswers[currentQuestionIndex] = answer
        Log.d("FragmentQuestionnaire",userAnswers.toString())

        currentQuestionIndex++
        if (currentQuestionIndex < questionsList.size) {
            displayQuestion(currentQuestionIndex)
            updateDotIndicator(currentQuestionIndex)
        } else
            Toast.makeText(requireContext(), "Ankieta zakończona", Toast.LENGTH_SHORT).show()
    }

    /**
     * Cofa do poprzedniego pytania
     */
    private fun goBack() {
        if (currentQuestionIndex > 0) {
            currentQuestionIndex--
            displayQuestion(currentQuestionIndex)
            updateDotIndicator(currentQuestionIndex)
        }
    }
    private fun initializeDots(count: Int){
        dotContainer.removeAllViews()
        for(i in 0 until count){
            val dot = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(16,16).apply {
                    marginStart = 8
                    marginEnd = 8
                }
                background = ContextCompat.getDrawable(context,R.drawable.dot_inactive)
            }
            dotContainer.addView(dot)
        }
    }
    private fun updateDotIndicator(currentIndex: Int){
        for(i in 0 until dotContainer.childCount){
            val dot = dotContainer.getChildAt(i)
            val drawableRes = if(i == currentIndex) R.drawable.dot_active else R.drawable.dot_inactive
            dot.background = context?.let { ContextCompat.getDrawable(it,drawableRes) }
        }
    }
}