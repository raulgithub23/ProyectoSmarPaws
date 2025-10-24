package com.example.smartpaws.data.repository

import com.example.smartpaws.data.local.user.UserDao
import com.example.smartpaws.data.local.user.UserEntity

class UserRepository(
    private val userDao: UserDao
) {
    // Login
    suspend fun login(email: String, pass: String): Result<UserEntity> {
        val user = userDao.getByEmail(email)
        return if (user != null && user.password == pass) {
            Result.success(user)
        } else {
            Result.failure(IllegalArgumentException("Credenciales Inválidas"))
        }
    }

    // Register - inicializa con rol USER por defecto
    suspend fun register(name: String, email: String, phone: String, pass: String): Result<Long> {
        val exists = userDao.getByEmail(email) != null
        if (exists) {
            return Result.failure(IllegalArgumentException("Correo ya existente"))
        }

        val id = userDao.insert(
            UserEntity(
                name = name,
                email = email,
                phone = phone,
                password = pass,
                rol = "USER"  // ⬅️ Todos inician como USER
            )
        )
        return Result.success(id)
    }

    suspend fun getUserById(userId: Long): UserEntity? {
        return userDao.getUserById(userId)
    }

    suspend fun updateUser(user: UserEntity) {
        userDao.update(user)
    }

    // ========== NUEVAS FUNCIONES PARA ADMINISTRACIÓN ==========

    // Obtener todos los usuarios (para panel admin)
    suspend fun getAllUsers(): List<UserEntity> {
        return userDao.getAll()
    }

    // Cambiar rol de un usuario
    suspend fun updateUserRole(userId: Long, newRole: String): Result<Unit> {
        return try {
            val user = userDao.getUserById(userId)
            if (user != null) {
                userDao.update(user.copy(rol = newRole))
                Result.success(Unit)
            } else {
                Result.failure(IllegalArgumentException("Usuario no encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Eliminar usuario (opcional)
    suspend fun deleteUser(userId: Long): Result<Unit> {
        return try {
            val user = userDao.getUserById(userId)
            if (user != null) {
                userDao.delete(user)
                Result.success(Unit)
            } else {
                Result.failure(IllegalArgumentException("Usuario no encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Verificar si un usuario es admin
    suspend fun isAdmin(userId: Long): Boolean {
        val user = userDao.getUserById(userId)
        return user?.rol == "ADMIN"
    }

    // Buscar usuarios (para el buscador del admin panel)
    suspend fun searchUsers(query: String): List<UserEntity> {
        return userDao.searchUsers(query)
    }

    // Obtener usuarios por rol (para filtros)
    suspend fun getUsersByRole(role: String): List<UserEntity> {
        return userDao.getUsersByRole(role)
    }
}