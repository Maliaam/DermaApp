package com.example.dermaapplication.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dermaapplication.R
import com.example.dermaapplication.items.Disease

/**
 * Adapter odpowiedzialny za wyświetlanie listy chorób za pomocą RecyclerView.
 * Każdy wyświetlany element posiada obraz, nazwę choroby oraz opis.
 *
 * @property diseasesList Lista obiektów Disease reprezentująca dermatozy, które mają być wyświetlone
 * w RecyclerView.
 * @property onItemClick Listener inicjalizowany przy utworzeniu adaptera, nasłuchuje kliknięcia dla
 * każdego elementu listy. Przyjmuje obiekt Disease jako parametr.
 */
class WikiDiseaseAdapter(var diseasesList: List<Disease>) :
    RecyclerView.Adapter<WikiDiseaseAdapter.WikiViewHolder>() {

    var onItemClick: ((Disease) -> Unit)? = null

    /**
     * Odpowiedzialny za przechowywanie oraz łączenie widoków dla każdego elementu listy.
     *
     * @property diseaseImage ImageView reprezentujący obraz choroby.
     * @property diseaseName TextView reprezentujący nazwę choroby.
     * @property diseaseDescription TextView reprezentujący opis choroby.
     */
    inner class WikiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val diseaseImage: ImageView = itemView.findViewById(R.id.wiki_skinMenu_diseaseImageRv)
        val diseaseName: TextView = itemView.findViewById(R.id.wiki_skinMenu_diseaseNameRv)
        val diseaseDescription: TextView =
            itemView.findViewById(R.id.wiki_skinMenu_diseaseDescriptionRv)

        init {
            itemView.setOnClickListener { onItemClick?.invoke(diseasesList[adapterPosition]) }
        }
    }

    /**
     * Ustawia listę przefiltrowanych chorób w adapterze. Aktualizuje wewnętrzną listę chorób
     * 'diseasesList' w adapterze na podstawie podanej listy przefiltrowanych chorób.
     *
     * @param diseasesList Lista chorób które mają być wyświetlane w RecyclerView.
     */
    fun setFilteredList(diseasesList: List<Disease>) {
        this.diseasesList = diseasesList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WikiViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.wiki_skin_disease_menu_rv,
            parent, false
        )
        return WikiViewHolder(view)
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
    override fun onBindViewHolder(holder: WikiViewHolder, position: Int) {
        holder.diseaseImage.setImageResource(R.drawable.baseline_10k_24)
        holder.diseaseName.text = diseasesList[position].name
        holder.diseaseDescription.text = diseasesList[position].description
    }
}