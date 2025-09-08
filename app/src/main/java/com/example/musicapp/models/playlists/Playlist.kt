package com.example.musicapp.models.playlists

data class Playlist(
    val _id: String,
    val title: String,
    val description: String,
    val songs: List<String>,
    val user_id: String,
    val coverImage: String,
    val createdBy: String,
    val status: String,
    val deleted: Boolean
)