package com.example.musicapp.ui.library

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.R

class LibraryAdapter(
    private val items: List<LibraryItem>,
    private val onClick: (LibraryItem) -> Unit
) : RecyclerView.Adapter<LibraryAdapter.LibraryViewHolder>() {

    inner class LibraryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgIcon: ImageView = itemView.findViewById(R.id.imgIcon)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)

        fun bind(item: LibraryItem) {
            imgIcon.setImageResource(item.iconRes)
            tvTitle.text = item.title
            itemView.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LibraryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_library, parent, false)
        return LibraryViewHolder(view)
    }

    override fun onBindViewHolder(holder: LibraryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
