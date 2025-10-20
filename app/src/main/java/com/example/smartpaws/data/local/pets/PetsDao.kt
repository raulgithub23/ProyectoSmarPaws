package com.example.smartpaws.data.local.pets

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface PetsDao {
    @Insert
    suspend fun insert(pet: PetsEntity): Long

    @Insert
    suspend fun insertAll(pets: List<PetsEntity>)

    @Query("SELECT * FROM pets")
    suspend fun getAllPets(): List<PetsEntity>

    @Query("SELECT * FROM pets WHERE userId = :userId")
    suspend fun getPetsByUser(userId: Long): List<PetsEntity>

    @Query("SELECT * FROM pets WHERE id = :petId")
    suspend fun getPetById(petId: Long): PetsEntity?

    @Update
    suspend fun update(pet: PetsEntity)

    @Delete
    suspend fun delete(pet: PetsEntity)

    @Query("SELECT COUNT(*) FROM pets")
    suspend fun count(): Int
}