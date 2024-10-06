package com.example.dermaapplication.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dermaapplication.R
import com.example.dermaapplication.items.Disease

class DiseaseAdapter(var mList: List<Disease>): RecyclerView.Adapter<DiseaseAdapter.DiseaseViewHolder>() {
    inner class DiseaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val image: ImageView = itemView.findViewById(R.id.disease_recyclerView_image)
        val text: TextView = itemView.findViewById(R.id.disease_recyclerView_text)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiseaseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.home_search_item,parent,
            false)
        return DiseaseViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun onBindViewHolder(holder: DiseaseViewHolder, position: Int) {
        holder.image.setImageResource(R.drawable.baseline_10k_24)
    }
}