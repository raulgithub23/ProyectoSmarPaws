package com.example.smartpaws.data.repository

import com.example.smartpaws.data.remote.AuthApiService
import com.example.smartpaws.data.remote.RemoteModule
import com.example.smartpaws.data.remote.dto.LoginRequest
import com.example.smartpaws.data.remote.dto.RegisterRequest
import com.example.smartpaws.data.remote.dto.UpdateImageRequest
import com.example.smartpaws.data.remote.dto.UpdateProfileRequest
import com.example.smartpaws.data.remote.dto.UpdateRoleRequest
import com.example.smartpaws.data.remote.dto.UserDto

class UserRepository(
    private val api: AuthApiService = RemoteModule.createAuthService(AuthApiService::class.java)
) {

    suspend fun login(email: String, password: String): Result<UserDto> {
        return try {
            val response = api.login(LoginRequest(email, password))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(
                IllegalArgumentException("Credenciales inválidas: ${e.message}")
            )
        }
    }

    suspend fun register(
        name: String,
        email: String,
        phone: String,
        password: String
    ): Result<Long> {
        return try {
            val request = RegisterRequest(
                name = name,
                email = email,
                phone = phone,
                password = password
            )

            // ⭐ CAMBIO: register devuelve UserDto, no RegisterResponse
            val response = api.register(request)
            Result.success(response.id)

        } catch (e: Exception) {
            Result.failure(
                IllegalStateException("Error al registrar: ${e.message}")
            )
        }
    }

    suspend fun getUserById(userId: Long): Result<UserDto> {
        return try {
            val response = api.getUserById(userId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(
                IllegalArgumentException("Usuario no encontrado: ${e.message}")
            )
        }
    }

    suspend fun getAllUsers(): Result<List<UserDto>> {
        return try {
            val response = api.getAllUsersDetailed("ADMIN")
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(
                IllegalStateException("Error al obtener usuarios: ${e.message}")
            )
        }
    }

    suspend fun searchUsers(query: String): Result<List<UserDto>> {
        return try {
            val response = api.searchUsers(query, "ADMIN")
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(
                IllegalStateException("Error en búsqueda: ${e.message}")
            )
        }
    }

    suspend fun getUsersByRole(role: String): Result<List<UserDto>> {
        return try {
            val response = api.getUsersByRole(role, "ADMIN")
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(
                IllegalStateException("Error al filtrar: ${e.message}")
            )
        }
    }

    suspend fun updateUserRole(userId: Long, newRole: String): Result<Unit> {
        return try {
            // ⭐ CAMBIO: updateUserRole devuelve UserDto, pero lo ignoramos
            api.updateUserRole(userId, UpdateRoleRequest(newRole), "ADMIN")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(
                IllegalStateException("Error al actualizar rol: ${e.message}")
            )
        }
    }

    suspend fun deleteUser(userId: Long): Result<Unit> {
        return try {
            api.deleteUser(userId, "ADMIN")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(
                IllegalStateException("Error al eliminar usuario: ${e.message}")
            )
        }
    }

    suspend fun updateUser(userId: Long, name: String, phone: String): Result<Unit> {
        return try {
            val request = UpdateProfileRequest(name, phone)
            api.updateUserProfile(userId, request)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(
                IllegalStateException("Error al actualizar perfil: ${e.message}")
            )
        }
    }

    // Se agrega este nuevo método
    suspend fun updateProfileImage(userId: Long, imagePath: String): Result<Unit> {
        return try {
            val request = UpdateImageRequest(imagePath)
            api.updateProfileImage(userId, request)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(
                IllegalStateException("Error al actualizar imagen: ${e.message}")
            )
        }
    }
}