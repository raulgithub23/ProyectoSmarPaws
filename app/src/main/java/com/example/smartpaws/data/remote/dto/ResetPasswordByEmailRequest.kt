package com.example.smartpaws.data.remote.dto

data class ResetPasswordByEmailRequest(
    val email: String,
    val newPassword: String
)