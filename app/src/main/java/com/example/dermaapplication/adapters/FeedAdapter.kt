package com.example.dermaapplication.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dermaapplication.R
import com.example.dermaapplication.items.Disease

/**
 * Adapter odpowiedzialny za wyświetlanie listy chorób za pomocą RecyclerView.
 * Każdy wyświetlany element posiada nazwę choroby oraz obraz.
 *
 * @property diseasesList Lista obiektów Disease reprezentująca dermatozy, które mają być wyświetlone
 *                        w RecyclerView.
 */
class FeedAdapter(private var diseasesList: List<Disease>) :
    RecyclerView.Adapter<FeedAdapter.FeedViewHolder>() {

        var onItemClick: ((Disease) -> Unit)? = null
    /**
     * Odpowiedzialny za przechowywanie oraz łączenie widoków dla każdego elementu listy.
     *
     * @property diseaseName TextView reprezentujący nazwę choroby.
     * @property diseaseImage ImageView reprezentujący obraz choroby.
     */
    inner class FeedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val diseaseName: TextView = itemView.findViewById(R.id.feed_disease_name)
        val diseaseImage: ImageView = itemView.findViewById(R.id.feed_disease_image)

        init {
            itemView.setOnClickListener { onItemClick?.invoke(diseasesList[adapterPosition]) }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_feed_recyclerview, parent,
            false
        )
        return FeedViewHolder(view)
    }

    /**
     * Zwraca liczbę elementów znajdujących się w liście przekazanej do adaptera.
     */
    override fun getItemCount(): Int {
        return diseasesList.size
    }

    /**
     * Łączy dane z odpowiedniego miejsca na liście z widokami ViewHoldera.
     *
     * @param holder ViewHolder.
     * @param position Pozycja elementu w liście przekazanej do adaptera.
     */
    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        val disease = diseasesList[position]

        if (disease.images.isNotEmpty()) {
            val imageUrl = disease.images[0]

            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.baseline_10k_24)
                .into(holder.diseaseImage)
        } else {
            holder.diseaseImage.setImageResource(R.drawable.baseline_10k_24)
        }

        holder.diseaseName.text = disease.name
    }

}