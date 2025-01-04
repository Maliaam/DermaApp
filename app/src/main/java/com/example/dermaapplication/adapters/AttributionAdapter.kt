package com.example.dermaapplication.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dermaapplication.R

class AttributionAdapter(private var mList: List<String>) : RecyclerView.Adapter<AttributionAdapter.AttributionViewHolder>() {

    inner class AttributionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val text: TextView = itemView.findViewById(R.id.tvAttribution)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttributionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_attribution, parent, false)
        return AttributionViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun onBindViewHolder(holder: AttributionViewHolder, position: Int) {
        holder.text.text = mList[position]
    }
}
