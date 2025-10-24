package com.example.smartpaws.data.local.user

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): UserEntity?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int

    @Query("SELECT * FROM users ORDER BY id ASC")
    suspend fun getAll(): List<UserEntity>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Long): UserEntity?

    @Update
    suspend fun update(user: UserEntity)

    // ========== FUNCIONES PARA ADMINISTRACIÓN ==========

    @Delete
    suspend fun delete(user: UserEntity)

    // Obtener usuarios por rol
    @Query("SELECT * FROM users WHERE rol = :role ORDER BY name ASC")
    suspend fun getUsersByRole(role: String): List<UserEntity>

    // Contar usuarios por rol
    @Query("SELECT COUNT(*) FROM users WHERE rol = :role")
    suspend fun countByRole(role: String): Int

    // Buscar usuarios por nombre
    @Query("SELECT * FROM users WHERE name LIKE '%' || :query || '%' OR email LIKE '%' || :query || '%' ORDER BY name ASC")
    suspend fun searchUsers(query: String): List<UserEntity>
}