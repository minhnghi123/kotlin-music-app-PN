package com.example.musicapp.models.artists

import com.example.musicapp.models.songs.SongForArtist
import com.example.musicapp.models.playlists.Playlist

data class ArtistDetailResponse(
    val success: Boolean,
    val artist: Artist,
    val songs: List<SongForArtist>,
    val followArtistsIds: List<String>,
    val individualPlaylists: List<Playlist>,
    val favoriteSongIds: List<String>
)
