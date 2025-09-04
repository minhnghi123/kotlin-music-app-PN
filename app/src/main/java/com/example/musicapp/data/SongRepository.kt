package com.example.musicapp.data

import com.example.musicapp.models.songs.ApiListResponse
import com.example.musicapp.models.songs.Song
import com.example.musicapp.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SongRepository {
    fun fetchSongs(onResult: (List<Song>?, String?) -> Unit) {
        ApiClient.api.getSongs().enqueue(object : Callback<ApiListResponse<Song>> {
            override fun onResponse(
                call: Call<ApiListResponse<Song>>,
                response: Response<ApiListResponse<Song>>
            ) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        onResult(body.data, null)
                    } else {
                        onResult(null, "API returned success=false")
                    }
                } else {
                    onResult(null, "API error ${response.code()}")
//                    log
                    println("API error ${response}")
                }
            }

            override fun onFailure(call: Call<ApiListResponse<Song>>, t: Throwable) {
                onResult(null, t.message ?: "Network error")
            }
        })
    }
}