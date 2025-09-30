package com.example.musicapp.ui.suggestion

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.models.songs.Song

class SuggestionAdapter(
    private var items: List<Song>,
    private val onClick: (Song) -> Unit = {}
) : RecyclerView.Adapter<SuggestionAdapter.SuggestionVH>() {

    class SuggestionVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgCover: ImageView = itemView.findViewById(R.id.imgCover)
        val txtTitle: TextView = itemView.findViewById(R.id.txtTitle)
        val txtArtist: TextView = itemView.findViewById(R.id.txtArtist)
        val btnMore: ImageButton = itemView.findViewById(R.id.btnMore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song_suggestion, parent, false)
        return SuggestionVH(view)
    }

    override fun onBindViewHolder(holder: SuggestionVH, position: Int) {
        val song = items[position]
        holder.txtTitle.text = song.title
        holder.txtArtist.text = song.artist.fullName

        Glide.with(holder.itemView)
            .load(song.coverImage)
            .placeholder(R.drawable.ic_default_album_art)
            .into(holder.imgCover)

        holder.itemView.setOnClickListener { onClick(song) }

        holder.btnMore.setOnClickListener { view ->
            val popup = PopupMenu(view.context, view)
            popup.inflate(R.menu.song_item_menu)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_add_playlist -> {
                        Toast.makeText(view.context, "Thêm vào playlist", Toast.LENGTH_SHORT).show()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    override fun getItemCount() = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun submit(newItems: List<Song>) {
        items = newItems
        notifyDataSetChanged()
    }
}
