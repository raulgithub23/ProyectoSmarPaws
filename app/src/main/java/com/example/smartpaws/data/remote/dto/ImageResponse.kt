package com.example.smartpaws.data.remote.dto

data class ImageResponse(
    val success: Boolean,
    val fileName: String?,
    val contentType: String?,
    val fileSize: Long?,
    val imageBase64: String?,
    val message: String?
)