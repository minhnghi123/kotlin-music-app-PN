package com.example.musicapp.models.users

data class UpdateMeRequest(
    val username: String? = null,
    val email: String? = null,
    val avatar: String? = null
)