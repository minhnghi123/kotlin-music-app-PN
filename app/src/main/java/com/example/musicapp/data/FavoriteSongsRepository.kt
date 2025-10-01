package com.example.musicapp.data

import com.example.musicapp.models.auth.ApiResponse
import com.example.musicapp.models.favorites.FavoriteSongsResponse
import com.example.musicapp.models.songs.Song
import com.example.musicapp.models.songs.SongListResponse
import com.example.musicapp.models.songs.SongResponse
import com.example.musicapp.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FavoriteSongsRepository {
    
    fun getFavoriteSongs(onResult: (List<Song>?, String?, Set<String>?) -> Unit) {
        ApiClient.api.getFavoriteSongs().enqueue(object : Callback<FavoriteSongsResponse> {
            override fun onResponse(
                call: Call<FavoriteSongsResponse>,
                response: Response<FavoriteSongsResponse>
            ) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        onResult(body.data.songs, null, body.data.favoriteSongIds.toSet())
                    } else {
                        onResult(null, "API returned success=false", null)
                    }
                } else {
                    onResult(null, "API error ${response.code()}", null)
                }
            }

            override fun onFailure(call: Call<FavoriteSongsResponse>, t: Throwable) {
                onResult(null, t.message ?: "Network error", null)
            }
        })
    }

    fun getFavoriteSongById(songId: String, onResult: (Song?, String?) -> Unit) {
        ApiClient.api.getFavoriteSongById(songId).enqueue(object : Callback<SongResponse> {
            override fun onResponse(
                call: Call<SongResponse>,
                response: Response<SongResponse>
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
                }
            }

            override fun onFailure(call: Call<SongResponse>, t: Throwable) {
                onResult(null, t.message ?: "Network error")
            }
        })
    }

    fun addFavoriteSong(songId: String, onResult: (Boolean, String?) -> Unit) {
        ApiClient.api.addFavoriteSong(songId).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(
                call: Call<ApiResponse>,
                response: Response<ApiResponse>
            ) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        onResult(true, body.message)
                    } else {
                        onResult(false, body?.message ?: "Failed to add favorite")
                    }
                } else {
                    onResult(false, "API error ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                onResult(false, t.message ?: "Network error")
            }
        })
    }

    fun removeFavoriteSong(songId: String, onResult: (Boolean, String?) -> Unit) {
        ApiClient.api.removeFavoriteSong(songId).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(
                call: Call<ApiResponse>,
                response: Response<ApiResponse>
            ) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        onResult(true, body.message)
                    } else {
                        onResult(false, body?.message ?: "Failed to remove favorite")
                    }
                } else {
                    onResult(false, "API error ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                onResult(false, t.message ?: "Network error")
            }
        })
    }

    fun removeAllFavoriteSongs(onResult: (Boolean, String?) -> Unit) {
        ApiClient.api.removeAllFavoriteSongs().enqueue(object : Callback<ApiResponse> {
            override fun onResponse(
                call: Call<ApiResponse>,
                response: Response<ApiResponse>
            ) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        onResult(true, body.message)
                    } else {
                        onResult(false, body?.message ?: "Failed to remove all favorites")
                    }
                } else {
                    onResult(false, "API error ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                onResult(false, t.message ?: "Network error")
            }
        })
    }
}



