package com.example.smartpaws.data.remote.dto

data class ImageUploadRequest(
    val fileName: String,
    val contentType: String,
    val imageBase64: String
)