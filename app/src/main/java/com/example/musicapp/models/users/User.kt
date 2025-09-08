package com.example.musicapp.models.users

import com.example.musicapp.models.artists.Artist
import com.example.musicapp.models.playlists.Playlist
import com.example.musicapp.models.songs.Song

data class UserData(
    val _id: String,
    val username: String,
    val email: String,
    val avatar: String,
    val playlist: List<Playlist>,
    val follow_songs: List<Song>,
    val follow_artists: List<Artist>
)