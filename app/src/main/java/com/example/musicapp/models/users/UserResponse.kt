package com.example.musicapp.models.users

data class UserResponse(
    val success: Boolean,
    val message: String?,
    val data: UserData
)