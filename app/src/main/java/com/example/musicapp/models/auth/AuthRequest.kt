package com.example.musicapp.models.auth

data class SignUpRequest(
    val username: String,
    val email: String,
    val password: String,
    val rePassword: String
)

data class LoginRequest(
    val username: String,
    val password: String
)