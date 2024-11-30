package com.example.dermaapplication.fragments.journal.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dermaapplication.R
import com.example.dermaapplication.items.Note

/**
 * Adapter odpowiedzialny za wyświetlanie notatek za pomocą RecyclerView.
 * @param notes Lista obiektów Note reprezentująca notatki w dzienniku.
 */
class JournalNotesAdapter(private var notes: List<Note>) :
    RecyclerView.Adapter<JournalNotesAdapter.NoteViewHolder>() {

    fun updateNotes(newNotes: List<Note>){
        notes = newNotes
        notifyDataSetChanged()
    }

    /**
     * ViewHolder odpowiedzialny za przechowywanie oraz łączenie widoków dla każdego elementu listy.
     *
     * @property noteDate TextView reprezentujący datę dodania notatki.
     * @property noteContent TextView reprezentujący tekst notatki.
     */
    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val noteDate: TextView = itemView.findViewById(R.id.note_date)
        val noteContent: TextView = itemView.findViewById(R.id.note_content)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.layout_item_note, parent, false)
        return NoteViewHolder(view)
    }

    /* Zwraca liczbę elementów znajdujących się w liście przekazanej do adaptera. */
    override fun getItemCount(): Int = notes.size

    /**
     * Łączy dane z odpowiedniego miejsca na liście z widokami ViewHoldera.
     * @param holder ViewHolder.
     * @param position Pozycja elementu w liście przekazanej do adaptera.
     */
    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.noteDate.text = notes[position].date
        holder.noteContent.text = notes[position].content
    }
}