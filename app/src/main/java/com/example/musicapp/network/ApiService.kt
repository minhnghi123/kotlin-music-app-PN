package com.example.musicapp.network

import com.example.musicapp.models.auth.ApiResponse
import com.example.musicapp.models.auth.LoginRequest
import com.example.musicapp.models.auth.SignUpRequest
import com.example.musicapp.models.songs.ApiListResponse
import com.example.musicapp.models.songs.Song
import com.example.musicapp.models.songs.SongResponse
import com.example.musicapp.models.users.ChangePasswordRequest
import com.example.musicapp.models.users.UpdateMeRequest
import com.example.musicapp.models.users.UserResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
//    authentication
    @POST("auth/sign-up")
    fun signUp(@Body request: SignUpRequest): Call<ApiResponse>

    @POST("auth/login")
    fun login(@Body request: LoginRequest): Call<ApiResponse>
    @POST("auth/logout")
    fun logout(): Call<ApiResponse>

// Lay thong tin cua minh
    @GET("user/me")
    fun getUserProfile(): Call<UserResponse>
    @PUT("user/me")
    fun updateMe(@Body request: UpdateMeRequest): Call<UserResponse>

    @PUT("user/me/change-password")
    fun changePassword(@Body request: ChangePasswordRequest): Call<UserResponse>

//    Lay tat ca bai hat
    @GET("/music/songs")
    fun getSongs():Call<ApiListResponse<Song>>
//    Lay chi tiet 1 bai hat
    @GET("/music/songs/{id}")
    fun getSongDetail(@Path("id") id:String): Call<SongResponse>




}