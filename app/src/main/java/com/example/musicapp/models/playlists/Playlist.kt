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


data class CreatePlaylistRequest(
    val title: String,
    val description: String? = null,
    val songs: List<String>? = null,
    val coverImage: String
)

data class AddToPlaylistRequest(
    val playlist: String,
    val song: String
)


data class PlaylistResponse(
    val success: Boolean,
    val data: List<Playlist>
)

data class CreatePlaylistResponse(
    val code: String,
    val playlist: Playlist
)

data class AddToPlaylistResponse(
    val success: Boolean,
    val message: String,
    val songs: List<String>? = null
)