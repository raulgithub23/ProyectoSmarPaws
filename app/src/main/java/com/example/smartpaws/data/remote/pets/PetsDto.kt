package com.example.smartpaws.data.remote.pets

data class PetsDto(
    val id: Long? = null,

    val userId: Long,

    val name: String,

    val especie: String,

    val fechaNacimiento: String? = null,

    val peso: Float? = null,

    val genero: String? = null,

    val color: String? = null,

    val notas: String? = null
)