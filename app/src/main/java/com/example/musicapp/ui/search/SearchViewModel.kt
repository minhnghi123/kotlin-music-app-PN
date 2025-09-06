package com.example.musicapp.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.musicapp.models.songs.Song
import com.example.musicapp.network.ApiClient
import com.example.musicapp.models.songs.ApiListResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
class SearchViewModel : ViewModel() {
    private val _songs = MutableLiveData<List<Song>>()
    val songs: LiveData<List<Song>> get() = _songs

    fun fetchSongs() {
        ApiClient.api.getSongs().enqueue(object : Callback<ApiListResponse<Song>> {
            override fun onResponse(
                call: Call<ApiListResponse<Song>>,
                response: Response<ApiListResponse<Song>>
            ) {
                if (response.isSuccessful) {
                    _songs.value = response.body()?.data ?: emptyList()
                }
            }

            override fun onFailure(call: Call<ApiListResponse<Song>>, t: Throwable) {
                _songs.value = emptyList()
                t.printStackTrace()
            }
        })
    }
}