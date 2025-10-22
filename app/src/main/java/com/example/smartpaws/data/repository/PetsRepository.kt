package com.example.smartpaws.data.repository

import com.example.smartpaws.data.local.pets.PetsDao
import com.example.smartpaws.data.local.pets.PetsEntity

class PetsRepository(
    private val petsDao: PetsDao
) {
    // Insertar una mascota
    suspend fun insertPet(pet: PetsEntity): Result<Long> {
        return try {
            val id = petsDao.insert(pet)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Insertar múltiples mascotas
    suspend fun insertAllPets(pets: List<PetsEntity>): Result<Unit> {
        return try {
            petsDao.insertAll(pets)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener todas las mascotas
    suspend fun getAllPets(): Result<List<PetsEntity>> {
        return try {
            val pets = petsDao.getAllPets()
            Result.success(pets)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener mascotas por usuario
    suspend fun getPetsByUser(userId: Long): Result<List<PetsEntity>> {
        return try {
            val pets = petsDao.getPetsByUser(userId)
            Result.success(pets)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener mascota por ID
    suspend fun getPetById(petId: Long): Result<PetsEntity?> {
        return try {
            val pet = petsDao.getPetById(petId)
            Result.success(pet)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Actualizar mascota
    suspend fun updatePet(pet: PetsEntity): Result<Unit> {
        return try {
            petsDao.update(pet)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Eliminar mascota
    suspend fun deletePet(pet: PetsEntity): Result<Unit> {
        return try {
            petsDao.delete(pet)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Contar mascotas
    suspend fun countPets(): Result<Int> {
        return try {
            val count = petsDao.count()
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}