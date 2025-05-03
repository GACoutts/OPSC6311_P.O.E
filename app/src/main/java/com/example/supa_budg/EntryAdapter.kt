package com.example.supa_budg

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.example.supa_budg.data.Entry

class EntryAdapter(
    private var entries: List<Entry>,
    private val categoryNameMap: Map<Int, String>
    ) : RecyclerView.Adapter<EntryAdapter.EntryViewHolder>() {

    inner class EntryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgPhoto: ImageView = itemView.findViewById(R.id.imgEntryPhoto)
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvNotes: TextView = itemView.findViewById(R.id.tvNotes)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_entry, parent, false)
        return EntryViewHolder(view)
    }

    override fun onBindViewHolder(holder: EntryViewHolder, position: Int) {
        val entry = entries[position]
        holder.tvAmount.text = "R ${entry.amount}"
        holder.tvDate.text = entry.createdDateFormat
        holder.tvCategory.text = categoryNameMap[entry.categoryid] ?: "Unknown Category"
        holder.tvNotes.text = entry.notes

        if (!entry.photoUri.isNullOrBlank()) {
            holder.imgPhoto.setImageURI(Uri.parse(entry.photoUri))
        } else {
            holder.imgPhoto.setImageResource(R.drawable.ic_placeholder)
        }
    }

    override fun getItemCount(): Int = entries.size

    fun updateEntries(newEntries: List<Entry>) {
        entries = newEntries
        notifyDataSetChanged()
    }

}
