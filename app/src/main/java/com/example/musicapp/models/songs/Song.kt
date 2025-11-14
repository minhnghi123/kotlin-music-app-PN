package com.example.musicapp.models.songs

import com.example.musicapp.models.artists.Artist

data class Song(
    val _id: String,
    val title: String,
    val artist: List<Artist>,
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