package com.example.musicapp.ui.artist

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.models.artists.Artist

class ArtistAdapter(
    private val artists: List<Artist>,
    private val onItemClick: ((Artist) -> Unit)? = null
) : RecyclerView.Adapter<ArtistAdapter.ArtistViewHolder>() {

    class ArtistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivCover: ImageView = itemView.findViewById(R.id.ivArtistCover)
        val tvName: TextView = itemView.findViewById(R.id.tvArtistName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_artist, parent, false)
        return ArtistViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        val artist = artists[position]

        // chỉ hiển thị tên
        holder.tvName.text = artist.fullName

        // load avatar
        Glide.with(holder.itemView.context)
            .load(artist.coverImage)
            .placeholder(R.drawable.ic_user)
            .circleCrop()
            .into(holder.ivCover)

        // click
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(artist)
        }
    }

    override fun getItemCount() = artists.size
}
