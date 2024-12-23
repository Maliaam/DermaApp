package com.example.dermaapplication.fragments.questionnaire

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dermaapplication.MainActivity
import com.example.dermaapplication.R
import com.example.dermaapplication.Utilities
import com.example.dermaapplication.fragments.UserFeedFragment
import com.example.dermaapplication.fragments.journal.adapters.JournalRecordsAdapter
import com.example.dermaapplication.items.JournalRecord
import com.example.dermaapplication.items.Pin
import com.example.dermaapplication.items.Question
import com.example.dermaapplication.items.Survey
import com.example.dermaapplication.items.SurveyItem
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    private lateinit var addTextView: TextView
    private lateinit var yesNoLayout: LinearLayout
    private lateinit var journalsRV: RecyclerView
    private lateinit var help: ImageView
    private val recordsList = mutableListOf<JournalRecord>()
    private val questionnaireEndAdapter by lazy { JournalRecordsAdapter(recordsList) }
    private val questionsList = ArrayList<Question>()
    private val userAnswers = mutableListOf<SurveyItem>()
    private lateinit var questionnaireProgressBar: ProgressBar
    private var currentQuestionNumber = 0
    private var totalQuestionsNumber = 0
    private var currentQuestionId = 0
    private var isUserInteracting = false

    /**
     * Inicjalizuje widoki fragmentu.
     * @param view Widok główny fragmentu, używany do znajdowania elementów.
     */
    private fun initializeViews(view: View) {
        questionText = view.findViewById(R.id.questionText)
        yesButton = view.findViewById(R.id.buttonYes)
        noButton = view.findViewById(R.id.buttonNo)
        backButton = view.findViewById(R.id.buttonPrevious)
        nextButton = view.findViewById(R.id.buttonNext)
        spinner = view.findViewById(R.id.answersSpinner)
        yesNoLayout = view.findViewById(R.id.yesNoLayout)
        loadingProgressBar = view.findViewById(R.id.progressBar)
        progressText = view.findViewById(R.id.progressText)
        content = view.findViewById(R.id.contentLayout)
        questionnaireEndButton = view.findViewById(R.id.endQuestionnaire)
        questionnaireProgressBar = view.findViewById(R.id.progressbar_questionnaire)
        journalsRV = view.findViewById(R.id.questionnaireEnd_recyclerview)
        addTextView = view.findViewById(R.id.addAnswers)
        help = view.findViewById(R.id.help_questionnaire)
    }

    private fun setupOnClickListeners() {
        yesButton.setOnClickListener { onAnswerSelected("tak") }
        noButton.setOnClickListener { onAnswerSelected("nie") }
        questionnaireEndAdapter.onItemClick = { record ->
            setupTitleDialog(
                requireContext(),
                onTitleAdded = { title ->
                record.documentId?.let { documentId ->
                    createSurveyAndSave(documentId, title)
                }
            })
        }
        help.setOnClickListener {
            val question = questionsList[currentQuestionNumber]
            val additionalHelperInfo = question.additionalInfo
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Dodatkowe informacje")
                    .setMessage(additionalHelperInfo)
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = questionnaireEndAdapter
        }
    }

    /**
     * Ładuje wszystkie dzienniki użytkownika w końcowym widoku ankiety
     */
    private fun fetchJournalRecord() {
        Utilities.databaseFetch.fetchJournalRecords { records ->
            if (records.isNotEmpty()) {
                recordsList.clear()
                recordsList.addAll(records)
            }
            questionnaireEndAdapter.notifyDataSetChanged()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_question, container, false)
        initializeViews(view)
        setupOnClickListeners()
        setupRecyclerView(journalsRV)
        fetchJournalRecord()

        Utilities.databaseFetch.fetchQuestions { questions ->
            startLoading()
            questionsList.clear()
            questionsList.addAll(questions.sortedWith(compareBy({ it.theme }, { it.id })))
            totalQuestionsNumber = questionsList.size
            displayQuestion(currentQuestionNumber)
            initializeProgressBar(questionsList.size)
            updateProgressBar(0)
        }
        return view
    }

    /**
     * Wyświetla pytanie na podstawie jego indeksu w liście pytań.
     * Funkcja aktualizuje widok pytania na ekranie, ustawiając tekst pytania oraz dostosowując typ
     * odpowiedzi (przyciski "tak/nie" lub rozwijane menu) do liczby dostępnych opcji odpowiedzi.
     * Dodatkowo ustawia element help w zależności, czy to pytanie posiada dodatkowe informacje.
     *
     * @param currentQuestionNumber Indeks pytania w liście `questionsList`, które ma zostać wyświetlone.
     */
    private fun displayQuestion(currentQuestionNumber: Int) {
        val question = questionsList[currentQuestionNumber]
        Log.e("Survey",question.toString())
        help.visibility = if (question.additionalInfo == null) View.GONE else View.VISIBLE
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
        val currentQuestion = questionsList[currentQuestionNumber]
        val surveyItem = SurveyItem(
            question = currentQuestion.question,
            answer = answer
        )
        if (userAnswers.size > currentQuestionNumber) {
            userAnswers[currentQuestionNumber] = surveyItem
        } else {
            userAnswers.add(surveyItem)
        }
        val nextQuestionNumber = getNextQuestion(answer)
        if (nextQuestionNumber < questionsList.size) {
            currentQuestionNumber = nextQuestionNumber
            displayQuestion(currentQuestionNumber)
            updateProgressBar(currentQuestionNumber)
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
                        val surveyItem = SurveyItem(
                            question = question.question,
                            answer = selectedAnswer
                        )
                        userAnswers.add(surveyItem)
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

        lifecycleScope.launch {
            for (progress in 0..100) {
                delay(10)
                fakeProgressBar(progress)
            }
            loadingProgressBar.visibility = View.GONE
            progressText.visibility = View.GONE
            content.visibility = View.VISIBLE
        }
    }

    /**
     * Funkcja wyświetlająca komunikat kończący ankietę.
     * Informuje użytkownika o zakończeniu wypełniania ankiety.
     */
    private fun showEndOfQuestionnaire() {
        Toast.makeText(context, "Dziękujemy za wypełnienie ankiety!", Toast.LENGTH_SHORT).show()
        journalsRV.visibility = View.VISIBLE
        yesNoLayout.visibility = View.GONE
        loadingProgressBar.visibility = View.GONE
        content.visibility = View.GONE
        if (FirebaseAuth.getInstance().currentUser == null) {
            questionText.text = "Ankieta zakończona. Zarejestruj się aby móc zapistwać ankiety."
        } else {
            questionText.visibility = View.GONE
            addTextView.visibility = View.VISIBLE
        }
        //todo jak nie ma wpisów a zalogowany to możliwość dodania
    }

    private fun createSurveyAndSave(documentId: String,surveyTitle: String): Survey {
        val date = Utilities.getCurrentTime("short")

//        val frontPins = (bundle.getSerializable("frontPins") as? List<Pin>) ?: emptyList()
//        val backPins = (bundle.getSerializable("backPins") as? List<Pin>) ?: emptyList()




        val survey = Survey(
            title = surveyTitle,
            date = date,
            items = userAnswers.toList()
        )
        Utilities.databaseFetch.updateJournalSurveys(
            documentId = documentId,
            surveys = listOf(survey),
            title = surveyTitle,
            date = date,
            onSuccess = {
                Toast.makeText(context, "Odpowiedzi zostały zapisane", Toast.LENGTH_SHORT).show()
                (activity as MainActivity).replaceFragment(UserFeedFragment())
            },
            onFailure = { exception ->
                Log.e("Survey", "Błąd zapisywania odpowiedzi: ${exception.message}")
                Toast.makeText(context, "Wystąpił błąd podczas zapisywania", Toast.LENGTH_SHORT)
                    .show()
            }
        )

        return survey
    }


    /** Ustawia maksymalną wartość ProgressBar na podstawie liczby pytań. */
    private fun initializeProgressBar(count: Int) {
        questionnaireProgressBar.max = count
        questionnaireProgressBar.progress = 0
    }

    /** Aktualizuje wartość ProgressBar na podstawie aktualnego indeksu pytania. */
    private fun updateProgressBar(currentIndex: Int) {
        questionnaireProgressBar.progress = currentIndex + 1
    }

    private fun setupTitleDialog(
        context: Context,
        onTitleAdded: (String) -> Unit
        ){
        val dialogView = LayoutInflater.from(context).inflate(R.layout.layout_dialog, null)
        val titleEditText = dialogView.findViewById<EditText>(R.id.note_edit_text)

        MaterialAlertDialogBuilder(context)
            .setTitle("Wpisz tytuł dziennika")
            .setView(dialogView)
            .setCancelable(true)
            .setPositiveButton("Zapisz") { dialog, _ ->
                val title = titleEditText.text.toString().trim()
                if (title.isNotEmpty()) {
                    onTitleAdded(title)
                    dialog.dismiss()
                } else {
                    Toast.makeText(context, "Tytuł nie może być pusty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Anuluj") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
