package com.example.musicapp.models.users

data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String,
    val reNewPassword: String
)