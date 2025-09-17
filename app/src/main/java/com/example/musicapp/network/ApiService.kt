package com.example.musicapp.network

import com.example.musicapp.models.auth.ApiResponse
import com.example.musicapp.models.auth.LoginRequest
import com.example.musicapp.models.auth.SignUpRequest
import com.example.musicapp.models.playlists.AddToPlaylistRequest
import com.example.musicapp.models.playlists.AddToPlaylistResponse
import com.example.musicapp.models.playlists.CreatePlaylistRequest
import com.example.musicapp.models.playlists.CreatePlaylistResponse
import com.example.musicapp.models.playlists.PlaylistResponse
import com.example.musicapp.models.songs.ApiListResponse
import com.example.musicapp.models.songs.Song
import com.example.musicapp.models.songs.SongResponse
import com.example.musicapp.models.users.ChangePasswordRequest
import com.example.musicapp.models.users.UpdateMeRequest
import com.example.musicapp.models.users.UserResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {
//  Phan cho authentication
    @POST("auth/sign-up")
    fun signUp(@Body request: SignUpRequest): Call<ApiResponse>

    @POST("auth/login")
    fun login(@Body request: LoginRequest): Call<ApiResponse>
    @POST("auth/logout")
    fun logout(): Call<ApiResponse>

//  Phan cho thong tin ca nhan
    @GET("user/me")
    fun getUserProfile(): Call<UserResponse>
    @Multipart
    @PUT("user/me")
    fun updateMe(
        @Part avatar: MultipartBody.Part?,
        @Part("username") username: RequestBody,
        @Part("email") email: RequestBody
    ): Call<UserResponse>

    @PUT("user/me/change-password")
    fun changePassword(@Body request: ChangePasswordRequest): Call<UserResponse>

//    Phan cho Song
    @GET("/music/songs")
    fun getSongs():Call<ApiListResponse<Song>>
    @GET("/music/songs/{id}")
    fun getSongDetail(@Path("id") id:String): Call<SongResponse>



//    Phan cho Playlist
    @GET("user/me/playlists")
    suspend fun getMyPlaylists(): PlaylistResponse

    @POST("playlist/create-playlist")
    suspend fun createPlaylist(@Body body: CreatePlaylistRequest): CreatePlaylistResponse

    @PATCH("playlist/add-playlist")
    suspend fun addToPlaylist(@Body body: AddToPlaylistRequest): Response<AddToPlaylistResponse>

}