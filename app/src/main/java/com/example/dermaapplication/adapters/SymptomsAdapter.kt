package com.example.dermaapplication.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dermaapplication.R

class SymptomsAdapter(private val symptoms: List<String>) :
    RecyclerView.Adapter<SymptomsAdapter.SymptomViewHolder>() {

    inner class SymptomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val symptomText: TextView = itemView.findViewById(R.id.symptomText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SymptomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_item_symptom, parent, false)

        return SymptomViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: SymptomViewHolder, position: Int) {
        holder.symptomText.text = "• ${symptoms[position]}"
    }

    override fun getItemCount(): Int = symptoms.size
}