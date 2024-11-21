package com.example.dermaapplication.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dermaapplication.R
import com.example.dermaapplication.items.JournalRecord

/**
 * Adapter odpowiedzialny za wyświetlenie wpisów użytkownika do jego dziennika.
 * @property journalRecords Lista obiektów JournalRecords reprezentująca wpisy do dziennika.
 * @property onItemClick listener inicjalizowany przy utworzeniu adaptera, nasłuchuje kliknięcia dla
 *           każdego elementu listy. Przyjmuje obiekt JournalRecord jako parametr.
 */
class JournalRecordsAdapter(private val journalRecords: List<JournalRecord>) :
    RecyclerView.Adapter<JournalRecordsAdapter.JournalViewHolder>() {

    var onItemClick: ((JournalRecord) -> Unit)? = null

    /**
     * Odpowiedzialny za przechowywanie oraz łączenie widoków dla każdego elementu listy.
     *
     * @property journalRecord TextView reprezentujący tytuł wpisu do dziennika.
     */
    inner class JournalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val journalRecord: TextView = itemView.findViewById(R.id.journal_recordTitle)

        init {
            itemView.setOnClickListener { onItemClick?.invoke(journalRecords[adapterPosition]) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JournalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_journal_record, parent, false)
        return JournalViewHolder(view)
    }

    /**
     * Zwraca liczbę elementów znajdujących się w liście przekazanej do adaptera.
     */
    override fun getItemCount(): Int = journalRecords.size

    /**
     * Łączy dane z odpowiedniego miejsca na liście z widokami ViewHoldera,
     *
     * @param holder ViewHolder.
     * @param position Pozycja elementu w liście przekazanej do adaptera.
     */
    override fun onBindViewHolder(holder: JournalViewHolder, position: Int) {
        holder.journalRecord.text = journalRecords[position].recordTitle
    }
}