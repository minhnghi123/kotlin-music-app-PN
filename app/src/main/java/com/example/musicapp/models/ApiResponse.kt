package com.example.musicapp.models
//tao 1 class response
// data class la class chi chua du lieu
// T la kieu du lieu dong
data class ApiListResponse<T>(
    val success: Boolean,
    val data: List<T>
)