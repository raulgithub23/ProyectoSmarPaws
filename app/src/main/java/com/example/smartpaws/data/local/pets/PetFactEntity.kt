package com.example.smartpaws.data.local.pets

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pet_facts")
data class PetFactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val type: String, // "cat" o "dog"
    val fact: String,
    val title: String = "¿Sabias que?"
)