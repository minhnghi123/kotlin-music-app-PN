package com.example.musicapp.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.musicapp.data.SongRepository
import com.example.musicapp.models.songs.Song

class SongViewModel : ViewModel() {
    private val repo = SongRepository()
    private val _songs = MutableLiveData<List<Song>>(emptyList())
    val songs: LiveData<List<Song>> = _songs

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadSongs() {
        repo.fetchSongs { list, err ->
            if (list != null) _songs.postValue(list)
            if (err != null) _error.postValue(err)
        }
    }
}