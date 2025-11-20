package com.example.smartpaws.data.repository

import com.example.smartpaws.data.local.user.UserEntity
import com.example.smartpaws.data.remote.AuthApiService
import com.example.smartpaws.data.remote.RemoteModule
import com.example.smartpaws.data.remote.dto.LoginRequest
import com.example.smartpaws.data.remote.dto.RegisterRequest
import com.example.smartpaws.data.remote.dto.UpdateRoleRequest

class UserRepository {

    private val api: AuthApiService = RemoteModule.create(AuthApiService::class.java)

    suspend fun login(email: String, password: String): Result<UserEntity> {
        return try {
            val response = api.login(LoginRequest(email, password))

            val user = UserEntity(
                id = response.id,
                rol = response.rol,
                name = response.name,
                email = response.email,
                phone = response.phone,
                password = "",
                profileImagePath = response.profileImagePath
            )

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(
                IllegalArgumentException("Credenciales inválidas: ${e.message}")
            )
        }
    }

    //Registro mediante API REST

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

    //NUEVO: Obtener usuario por ID

    suspend fun getUserById(userId: Long): UserEntity {
        return try {
            val response = api.getUserById(userId)

            UserEntity(
                id = response.id,
                rol = response.rol,
                name = response.name,
                email = response.email,
                phone = response.phone,
                password = "",
                profileImagePath = response.profileImagePath
            )
        } catch (e: Exception) {
            throw IllegalArgumentException("Usuario no encontrado: ${e.message}")
        }
    }

    //NUEVO: Obtener lista de usuarios (solo ADMIN)

    suspend fun getAllUsers(): List<UserEntity> {
        return try {
            val response = api.getAllUsersDetailed("ADMIN")
            response.map { dto ->
                UserEntity(
                    id = dto.id,
                    rol = dto.rol,
                    name = dto.name,
                    email = dto.email,
                    phone = dto.phone,
                    password = "",
                    profileImagePath = dto.profileImagePath
                )
            }
        } catch (e: Exception) {
            throw IllegalStateException("Error al obtener usuarios: ${e.message}")
        }
    }

    //Buscar usuarios
    suspend fun searchUsers(query: String): List<UserEntity> {
        return try {
            val response = api.searchUsers(query, "ADMIN")
            response.map { dto ->
                UserEntity(
                    id = dto.id,
                    rol = dto.rol,
                    name = dto.name,
                    email = dto.email,
                    phone = dto.phone,
                    password = "",
                    profileImagePath = dto.profileImagePath
                )
            }
        } catch (e: Exception) {
            throw IllegalStateException("Error en búsqueda: ${e.message}")
        }
    }

    //Filtrar por rol
    suspend fun getUsersByRole(role: String): List<UserEntity> {
        return try {
            val response = api.getUsersByRole(role, "ADMIN")
            response.map { dto ->
                UserEntity(
                    id = dto.id,
                    rol = dto.rol,
                    name = dto.name,
                    email = dto.email,
                    phone = dto.phone,
                    password = "",
                    profileImagePath = dto.profileImagePath
                )
            }
        } catch (e: Exception) {
            throw IllegalStateException("Error al filtrar: ${e.message}")
        }
    }

    //Actualizar por rol de usuario
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

    //Eliminar el usuario solo admin
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
    //Actualizar usuario (PLACEHOLDER)

    suspend fun updateUser(user: UserEntity) {
        // TODO: Crear endpoint PUT en Spring Boot
        throw NotImplementedError("Endpoint no disponible aún")
    }
}