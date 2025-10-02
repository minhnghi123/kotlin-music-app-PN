package com.example.musicapp.models.songs

data class SongForArtist(
    val _id: String,
    val title: String,
    val artist: String,    // API trả string
    val album: String,
    val topic: List<String>,
    val fileUrl: String,
    val coverImage: String,
    val likes: List<String>,
    val lyrics: String?,
    val description: String?,
    val status: String,
    val deleted: Boolean,
    val createdAt: String,
    val updatedAt: String
)