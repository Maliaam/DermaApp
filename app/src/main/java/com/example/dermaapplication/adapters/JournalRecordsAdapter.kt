package com.example.dermaapplication.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dermaapplication.R
import com.example.dermaapplication.items.JournalRecord

/**
 * Adapter odpowiedzialny za wyświetlenie wpisów użytkownika do jego dziennika.
 * @param   journalRecords Lista obiektów JournalRecords reprezentująca wpisy do dziennika.
 * @property onItemClick   Listener inicjalizowany przy utworzeniu adaptera, nasłuchuje kliknięcia dla
 *                         każdego elementu listy. Przyjmuje obiekt JournalRecord jako parametr.
 * @property onDeleteClick Listener inicjalizowany przy utworzeniu adaptera, nasłuchuje kliknięcia
 *                         dla każdego elementu listy. Przyjmuje obiekt JournalRecord jako parametr.
 * @property isDeleteMode  Flaga wskazująca, czy tryb usuwania wpisów jest aktywny.
 */
class JournalRecordsAdapter(private var journalRecords: List<JournalRecord>) :
    RecyclerView.Adapter<JournalRecordsAdapter.JournalViewHolder>() {

    /* Listener wywoływany  po kliknięciu w element RecyclerView */
    var onItemClick: ((JournalRecord) -> Unit)? = null

    /* Listener wywoływany po kliknięciu w ikonę kosza */
    var onDeleteClick: ((JournalRecord) -> Unit)? = null

    /* Flaga wskazująca, czy tryb usuwania wpisów jest aktywny */
    var isDeleteMode: Boolean = false

    /**
     * ViewHolder odpowiedzialny za przechowywanie oraz łączenie widoków dla każdego elementu listy.
     *
     * @property journalRecord TextView reprezentujący tytuł wpisu do dziennika.
     * @property deleteIcon Ikona reprezentująca kosz do usuwania wpisów.
     * @property dateText TextView reprezentujący datę dodanego wpisu do dziennika.
     */
    inner class JournalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val journalRecord: TextView = itemView.findViewById(R.id.journal_recordTitle)
        val deleteIcon: ImageView = itemView.findViewById(R.id.journal_remove_record_icon)
        val dateText: TextView = itemView.findViewById(R.id.journal_record_date)

        init {
            itemView.setOnClickListener {
                if (!isDeleteMode) {
                    onItemClick?.invoke(journalRecords[adapterPosition])
                }
            }
            deleteIcon.setOnClickListener {
                onDeleteClick?.invoke(journalRecords[adapterPosition])
            }
        }
    }

    /**
     * Ustawia listę przefiltrowanych wpisów dziennika w adapterze.
     * Aktualizuje wewnętrzną listę wpisów 'JournalRecord' w adapterze na podstawie podanej listy
     * przefiltrowanych wpisów.
     *
     * @param journalRecordsList Lista wpisów które mają być wyświetlane w RecyclerView.
     */
    fun setFilteredList(journalRecordsList: List<JournalRecord>) {
        this.journalRecords = journalRecordsList
        notifyDataSetChanged()
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
        holder.deleteIcon.visibility = if (isDeleteMode) View.VISIBLE else View.GONE
        holder.dateText.text = journalRecords[position].date.toString()
    }
}