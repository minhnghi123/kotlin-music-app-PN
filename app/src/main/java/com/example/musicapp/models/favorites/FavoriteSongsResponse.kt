package com.example.musicapp.models.favorites

import com.example.musicapp.models.songs.Song

data class FavoriteSongsResponse(
    val success: Boolean,
    val data: FavoriteSongsData,
    val message: String?
)

data class FavoriteSongsData(
    val songs: List<Song>,
    val favoriteSongIds: List<String>,
    val total: Int
)




