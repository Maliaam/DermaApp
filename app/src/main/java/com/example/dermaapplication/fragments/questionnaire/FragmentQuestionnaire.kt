package com.example.dermaapplication.fragments.questionnaire

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
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.dermaapplication.R
import com.example.dermaapplication.Utilities
import com.example.dermaapplication.items.Question

/**
 * Fragment umożliwiający przedstawienie oraz wykonanie ankiety użytkownikowi.
 * Odpowiada za dynamiczne wyświetlanie pytań i odpowiedzi pobieranych z Firebase.
 */
class FragmentQuestionnaire : Fragment() {
    private lateinit var questionText: TextView
    private lateinit var yesButton: Button
    private lateinit var noButton: Button
    private lateinit var backButton: Button
    private lateinit var nextButton: Button
    private lateinit var questionnaireEndButton: Button
    private lateinit var spinner: Spinner
    private lateinit var content: ConstraintLayout
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var dotContainer: LinearLayout
    private lateinit var yesNoLayout: LinearLayout
    private val questionsList = ArrayList<Question>()
    private val userAnswers = mutableMapOf<Int, String>()
    private var currentQuestionNumber = 0
    private var totalQuestionsNumber = 0
    private var currentQuestionId = 0
    private var isUserInteracting = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_question, container, false)

        questionText = view.findViewById(R.id.questionText)
        yesButton = view.findViewById(R.id.buttonYes)
        noButton = view.findViewById(R.id.buttonNo)
        backButton = view.findViewById(R.id.buttonPrevious)
        nextButton = view.findViewById(R.id.buttonNext)
        spinner = view.findViewById(R.id.answersSpinner)
        dotContainer = view.findViewById(R.id.dotContainer)
        yesNoLayout = view.findViewById(R.id.yesNoLayout)
        loadingProgressBar = view.findViewById(R.id.progressBar)
        progressText = view.findViewById(R.id.progressText)
        content = view.findViewById(R.id.contentLayout)
        questionnaireEndButton = view.findViewById(R.id.endQuestionnaire)

        Utilities.databaseFetch.fetchQuestions { questions ->
            startLoading()

            questionsList.clear()
            questionsList.addAll(questions.sortedWith(compareBy({ it.theme }, { it.id })))
            totalQuestionsNumber = questionsList.size


            displayQuestion(currentQuestionNumber)
            initializeDots(questionsList.size)
            updateDotIndicator(0)
        }

        yesButton.setOnClickListener { onAnswerSelected("tak") }
        noButton.setOnClickListener { onAnswerSelected("nie") }
//        backButton.setOnClickListener { goBack() }

        return view
    }

    /**
     * Wyświetla pytanie na podstawie jego indeksu w liście pytań.
     * Funkcja aktualizuje widok pytania na ekranie, ustawiając tekst pytania oraz dostosowując typ
     * odpowiedzi (przyciski "tak/nie" lub rozwijane menu) do liczby dostępnych opcji odpowiedzi.
     *
     * @param currentQuestionNumber Indeks pytania w liście `questionsList`, które ma zostać wyświetlone.
     */
    private fun displayQuestion(currentQuestionNumber: Int) {
        val question = questionsList[currentQuestionNumber]
        Log.d("Survey", question.toString())
        currentQuestionId = question.id
        questionText.text = question.question
        adjustAnswerTypeToQuestion(question)
    }

    /**
     * Funkcja odpowiedzialna za ustalenie, które pytanie powinno zostać wyświetlone następnie,
     * bazując na udzielonej odpowiedzi.
     * Jeśli odpowiedź na pytanie jest zgodna z oczekiwaną, funkcja zwraca indeks pytania wskazanego
     * w `nextQuestion`.
     * Jeśli odpowiedź była niezgodna z oczekiwaną lub brak jest następnego pytania, usuwa pytanie i
     * przechodzi do kolejnego w sekwencji.
     *
     * @param currentAnswer Odpowiedź udzielona przez użytkownika na bieżące pytanie.
     * @return Indeks następnego pytania w liście `questionsList`.
     */
    private fun getNextQuestion(currentAnswer: String): Int {
        val currentQuestion = questionsList[currentQuestionNumber]
        if (currentQuestion.nextQuestion != null) {
            val nextQuestionId = currentQuestion.nextQuestion
            val nextQuestion = questionsList.find { it.id == nextQuestionId }
            if (currentAnswer == currentQuestion.expectedAnswer && nextQuestion != null) {
                return questionsList.indexOf(nextQuestion)
            } else {
                questionsList.removeAll { it.id == nextQuestionId }
            }
        }
        return currentQuestionNumber + 1
    }

    /**
     * Funkcja wykonuje się po wybraniu odpowiedzi przez użytkownika. Zapisuje odpowiedź, ustala
     * następne pytanie do wyświetlenia i w zależności od tego, czy istnieje następne pytanie,
     * wyświetla je lub kończy ankietę.
     * Zmienia stan interakcji użytkownika na `false`.
     *
     * @param answer Odpowiedź wybrana przez użytkownika ("tak" lub "nie").
     */
    private fun onAnswerSelected(answer: String) {
        userAnswers[currentQuestionNumber] = answer
        val nextQuestionNumber = getNextQuestion(answer)
        Log.d("Survey", "Next Question: $nextQuestionNumber")

        if (nextQuestionNumber < questionsList.size) {
            currentQuestionNumber = nextQuestionNumber
            displayQuestion(currentQuestionNumber)
            updateDotIndicator(currentQuestionNumber)
        } else {
            showEndOfQuestionnaire()
        }
        isUserInteracting = false
    }

    /**
     * Funkcja dostosowuje typ odpowiedzi w zależności od liczby dostępnych opcji odpowiedzi dla
     * pytania.
     * Jeśli odpowiedzi jest więcej niż 3, wyświetlany jest spinner z rozwijanym menu.
     * W przeciwnym przypadku wyświetlane są przyciski "tak/nie".
     * Zmienia także widoczność komponentów UI w zależności od tego, jaki typ odpowiedzi jest wymagany.
     *
     * @param question Obiekt reprezentujący pytanie, do którego należy dostosować typ odpowiedzi.
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun adjustAnswerTypeToQuestion(question: Question) {
        val answers = question.answers.toMutableList()
        answers.add(0, "Wybierz odpowiedź")

        questionText.visibility = View.VISIBLE
        if (answers.size > 3) {
            yesNoLayout.visibility = View.GONE
            spinner.visibility = View.VISIBLE

            val adapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, answers)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter

            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    if (position > 0 && isUserInteracting) {
                        val selectedAnswer = answers[position]
                        userAnswers[currentQuestionNumber] = selectedAnswer
                        onAnswerSelected(selectedAnswer)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            spinner.setOnTouchListener { _, _ ->
                isUserInteracting = true
                false
            }
        } else {
            yesNoLayout.visibility = View.VISIBLE
            spinner.visibility = View.GONE
        }
    }


    /**
     * Symuluje pasek postępu podczas ładowania pytań.
     * Wyświetla postęp w postaci liczb i paska.
     *
     * @param progress Wartość postępu.
     */
    private fun fakeProgressBar(progress: Int) {
        loadingProgressBar.progress = progress
        progressText.text = getString(R.string.loading_progress, progress)
    }

    /** Rozpoczyna proces ładowania pytań z symulowanym paskiem postępu. */
    private fun startLoading() {
        loadingProgressBar.visibility = View.VISIBLE
        progressText.visibility = View.VISIBLE
        content.visibility = View.GONE

        Thread {
            for (progress in 0..100) {
                Thread.sleep(10)
                requireActivity().runOnUiThread {
                    fakeProgressBar(progress)
                }
            }

            requireActivity().runOnUiThread {
                loadingProgressBar.visibility = View.GONE
                progressText.visibility = View.GONE
                content.visibility = View.VISIBLE
            }
        }.start()
    }

    /**
     * Funkcja wyświetlająca komunikat kończący ankietę.
     * Informuje użytkownika o zakończeniu wypełniania ankiety.
     * TODO(Zmienić tą funkcję)
     */
    private fun showEndOfQuestionnaire() {
        // Funkcja wyświetlająca komunikat kończący ankietę
        Toast.makeText(context, "Dziękujemy za wypełnienie ankiety!", Toast.LENGTH_SHORT).show()
    }

    /** Inicjalizuje kropki postępu na podstawie liczby pytań.*/
    private fun initializeDots(count: Int) {
        dotContainer.removeAllViews()
        for (i in 0 until count) {
            val dot = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(16, 16).apply {
                    marginStart = 8
                    marginEnd = 8
                }
                background = ContextCompat.getDrawable(context, R.drawable.dot_inactive)
            }
            dotContainer.addView(dot)
        }
    }

    /** Aktualizuje wygląd kropek, podświetlając aktualnie aktywną. */
    private fun updateDotIndicator(currentIndex: Int) {
        for (i in 0 until dotContainer.childCount) {
            val dot = dotContainer.getChildAt(i)
            val drawableRes =
                if (i == currentIndex) R.drawable.dot_active else R.drawable.dot_inactive
            dot.background = context?.let { ContextCompat.getDrawable(it, drawableRes) }
        }
    }
}
