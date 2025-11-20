package com.example.smartpaws.data.remote.dto

data class RegisterRequest(
    val rol: String = "USER",
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
    val profileImagePath: String = "drawable://larry"
)

