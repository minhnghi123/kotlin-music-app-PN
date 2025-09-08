package com.example.musicapp.models.artists

data class Artist(
    val _id: String,
    val fullName: String,
    val country: String,
    val albums: List<String>,
    val songs: List<String>,
    val coverImage: String,
    val status: String,
    val deleted: Boolean
)