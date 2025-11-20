package com.example.smartpaws.data.repository

import com.example.smartpaws.data.remote.RemoteModule
import com.example.smartpaws.data.remote.pets.PetsApiService
import com.example.smartpaws.data.remote.pets.PetsDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException

class PetsRepository(
    private val api: PetsApiService =
        RemoteModule.createPetsService(PetsApiService::class.java)
) {

    suspend fun insertPet(pet: PetsDto): Result<Long> = try {
        val response = api.createPet(pet)
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!.id ?: 0L)
        } else {
            Result.failure(HttpException(response))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun insertAllPets(pets: List<PetsDto>): Result<Unit> = try {
        pets.forEach { pet ->
            val response = api.createPet(pet)
            if (!response.isSuccessful) throw HttpException(response)
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getAllPets(): Result<List<PetsDto>> = try {
        val response = api.getAllPets()
        if (response.isSuccessful) {
            Result.success(response.body() ?: emptyList())
        } else {
            Result.failure(HttpException(response))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getPetsByUser(userId: Long): Result<List<PetsDto>> = try {
        val response = api.getPetsByUserId(userId)
        if (response.isSuccessful) {
            Result.success(response.body() ?: emptyList())
        } else {
            // Si es 204 (No Content) devolvemos lista vacía, sino error
            if (response.code() == 204) Result.success(emptyList())
            else Result.failure(HttpException(response))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getPetById(petId: Long): Result<PetsDto?> = try {
        val response = api.getPetById(petId)
        if (response.isSuccessful) {
            Result.success(response.body())
        } else {
            if (response.code() == 404) Result.success(null)
            else Result.failure(HttpException(response))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updatePet(pet: PetsDto): Result<Unit> = try {
        // Necesitamos el ID para la URL, y el cuerpo es el DTO
        val id = pet.id ?: throw Exception("El ID de la mascota es nulo")
        val response = api.updatePet(id, pet)

        if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            Result.failure(HttpException(response))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deletePet(pet: PetsDto): Result<Unit> = try {
        val id = pet.id ?: throw Exception("El ID de la mascota es nulo")
        val response = api.deletePet(id)

        if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            Result.failure(HttpException(response))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun observePetsByUser(userId: Long): Flow<Result<List<PetsDto>>> = flow {
        emit(getPetsByUser(userId))
    }
}