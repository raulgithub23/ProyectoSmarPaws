package com.example.smartpaws.data.repository

import com.example.smartpaws.data.remote.AuthApiService
import com.example.smartpaws.data.remote.RemoteModule
import com.example.smartpaws.data.remote.dto.ForgotPasswordRequest
import com.example.smartpaws.data.remote.dto.LoginRequest
import com.example.smartpaws.data.remote.dto.RegisterRequest
import com.example.smartpaws.data.remote.dto.ResetPasswordByEmailRequest
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

    suspend fun updateUser(userId: Long, name: String, phone: String): Result<UserDto> {
        return try {
            val request = UpdateProfileRequest(name, phone)
            val response = api.updateUserProfile(userId, request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(
                IllegalStateException("Error al actualizar perfil: ${e.message}")
            )
        }
    }

    suspend fun updateProfileImage(userId: Long, imagePath: String): Result<UserDto> {
        return try {
            val request = UpdateImageRequest(imagePath)
            val response = api.updateProfileImage(userId, request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(
                IllegalStateException("Error al actualizar imagen: ${e.message}")
            )
        }
    }

    // METODO SIMPLIFICADO: Verificar email
    suspend fun requestPasswordReset(email: String): Result<String> {
        return try {
            val request = ForgotPasswordRequest(email)
            val response = api.forgotPassword(request)
            if (response.success) {
                Result.success(response.message)
            } else {
                Result.failure(IllegalStateException(response.message))
            }
        } catch (e: Exception) {
            Result.failure(
                IllegalStateException("Error al verificar email: ${e.message}")
            )
        }
    }

    // METODO SIMPLIFICADO: Cambiar contraseña directamente con email
    suspend fun resetPasswordByEmail(email: String, newPassword: String): Result<String> {
        return try {
            val request = ResetPasswordByEmailRequest(email, newPassword)
            val response = api.resetPasswordByEmail(request)
            if (response.success) {
                Result.success(response.message)
            } else {
                Result.failure(IllegalStateException(response.message))
            }
        } catch (e: Exception) {
            Result.failure(
                IllegalStateException("Error al restablecer contraseña: ${e.message}")
            )
        }
    }
}