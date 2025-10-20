package com.example.smartpaws.data.local.pets

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.smartpaws.data.local.user.UserEntity

@Entity(
    tableName = "pets",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class PetsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val userId: Long,                      // Clave foránea
    val name: String,
    val especie: String,
    val fechaNacimiento: String? = null,   // Formato: "2020-05-15"
    val peso: Float? = null,
    val genero: String? = null,
    val color: String? = null,
    val notas: String? = null,
)