package com.example.smartpaws.data.local.user

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,              // Obligatorio
    val email: String,             // Obligatorio
    val phone: String,    // Opcional
    val password: String           // Obligatorio
)


