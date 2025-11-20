package com.example.smartpaws.data.remote.dto

data class UserDetailDto(
    val id: Long,
    val rol: String,
    val name: String,
    val email: String,
    val phone: String,
    val profileImagePath: String?
)