package com.example.musicapp.models.songs
data class Artist(
    val id: String,
    val fullName: String
)
data class Song(
    val _id: String,
    val title: String,
    val artist: Artist,
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