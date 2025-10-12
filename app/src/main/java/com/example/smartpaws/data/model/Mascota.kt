package com.example.smartpaws.data.model

import java.time.LocalDateTime

data class Mascota(
    val id: Int,
    val nombre: String,
    val especie: String, // "Perro" o "Gato"
    val raza: String = "",
    val fechaNacimiento: String = "",
    val peso: Float = 0f,
    val genero: String = "", // "M" o "F"
    val color: String = "",
    val chip: String = "",
    val notas: String = "",
    val estado: Boolean = true, // true = activo, false = inactivo
    val creacion: LocalDateTime? = null,
    val modificacion: LocalDateTime? = null,
    val idUsuario: Int
)
