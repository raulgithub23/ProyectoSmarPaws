package com.example.smartpaws.data.local.doctors

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "doctors")
data class DoctorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val specialty: String,
    val phone: String? = null,
    val email: String,
    val string: String,
    val string1: String,
)

