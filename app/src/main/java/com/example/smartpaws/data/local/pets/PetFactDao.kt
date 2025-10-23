package com.example.smartpaws.data.local.pets

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PetFactDao {

    // Contar registros (para seeds)
    @Query("SELECT COUNT(*) FROM pet_facts")
    suspend fun count(): Int

    @Query("SELECT * FROM pet_facts WHERE type = :petType ORDER BY RANDOM() LIMIT 1")
    fun getRandomFactByType(petType: String): Flow<PetFactEntity?>

    @Query("SELECT * FROM pet_facts")
    fun getAllFacts(): Flow<List<PetFactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFact(fact: PetFactEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFacts(facts: List<PetFactEntity>)

    @Delete
    suspend fun deleteFact(fact: PetFactEntity)

    @Query("SELECT * FROM pet_facts WHERE id = :id")
    suspend fun getFactById(id: Long): PetFactEntity?
}