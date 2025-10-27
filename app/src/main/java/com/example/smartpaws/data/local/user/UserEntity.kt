package com.example.smartpaws.data.local.user

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val rol: String = "USER",
    val name: String,              // Obligatorio
    val email: String,             // Obligatorio
    val phone: String,    // Obligatorio
    val password: String,         // Obligatorio
    val profileImagePath: String? = "drawable://larry" //Ruta de la foto de perfil se actualiza para que sea por defecto

)


