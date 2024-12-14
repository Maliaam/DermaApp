package com.example.dermaapplication.fragments.journal.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dermaapplication.R
import com.example.dermaapplication.fragments.journal.surveys.SurveyListFragment
import com.example.dermaapplication.items.Survey
import com.example.dermaapplication.items.SurveyItem

class SurveyListAdapter(
    private val isDeleteMode: () -> Boolean
) : RecyclerView.Adapter<SurveyListAdapter.SurveyViewHolder>() {

    private var surveys = mutableListOf<Survey>()
    private val expandedPositions = mutableSetOf<Int>()
    var onItemDelete: ((Survey) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SurveyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_survey, parent, false)
        return SurveyViewHolder(view)
    }

    override fun onBindViewHolder(holder: SurveyViewHolder, position: Int) {
        val survey = surveys[position]
        val isExpanded = expandedPositions.contains(position)

        holder.bind(survey, isExpanded)

/*        holder.itemView.setOnClickListener {
            if (isExpanded) expandedPositions.remove(position) else expandedPositions.add(position)
            notifyItemChanged(position)
        }*/

        holder.itemView.setOnClickListener {
            if(isDeleteMode()){
                onItemDelete?.invoke(survey)
            } else {
                if (isExpanded) expandedPositions.remove(position) else expandedPositions.add(position)
                notifyItemChanged(position)
            }
        }
    }

    override fun getItemCount(): Int = surveys.size

    fun updateSurveys(newSurveys: List<Survey>?) {
        surveys = newSurveys?.toMutableList() ?: mutableListOf() // Jeśli newSurveys jest null, użyj pustej listy
        notifyDataSetChanged()
    }



    class SurveyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val titleView: TextView = itemView.findViewById(R.id.surveyTitle)
        private val dateView: TextView = itemView.findViewById(R.id.surveyDate)
        private val dropdownIcon: ImageView = itemView.findViewById(R.id.dropdownIcon)
        private val questionsContainer: LinearLayout = itemView.findViewById(R.id.questionsContainer)

        fun bind(survey: Survey, isExpanded: Boolean) {
            titleView.text = survey.title
            dateView.text = survey.date

            dropdownIcon.setImageResource(if (isExpanded) R.drawable.ic_drop_top else R.drawable.ic_drop_down)

            questionsContainer.removeAllViews()
            if (isExpanded) {
                survey.items.forEach { item ->
                    val questionAnswerView = LayoutInflater.from(itemView.context)
                        .inflate(R.layout.item_question_answer, questionsContainer, false) as View
                    questionAnswerView.findViewById<TextView>(R.id.question).text = item.question
                    questionAnswerView.findViewById<TextView>(R.id.answer).text = item.answer
                    questionsContainer.addView(questionAnswerView)
                }
                questionsContainer.visibility = View.VISIBLE
            } else {
                questionsContainer.visibility = View.GONE
            }
        }
    }
}
