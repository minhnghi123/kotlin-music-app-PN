package com.example.musicapp.network

import com.example.musicapp.models.ApiListResponse
import com.example.musicapp.models.Song
import com.example.musicapp.models.SongResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
//    Lay tat ca bai hat
    @GET("/music/songs")
    fun getSongs():Call<ApiListResponse<Song>>
//    Lay chi tiet 1 bai hat
    @GET("/music/songs/{id}")
    fun getSongDetail(@Path("id") id:String): Call<SongResponse>
}