package com.example.smartpaws.data.local.appointment

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface AppointmentDao {

    @Insert
    suspend fun insert(appointment: AppointmentEntity): Long

    @Update
    suspend fun update(appointment: AppointmentEntity)

    @Delete
    suspend fun delete(appointment: AppointmentEntity)

    @Query("SELECT * FROM appointments")
    suspend fun getAll(): List<AppointmentEntity>

    @Query("SELECT * FROM appointments WHERE userId = :userId")
    suspend fun getByUser(userId: Long): List<AppointmentEntity>

    @Query("SELECT * FROM appointments WHERE doctorId = :doctorId")
    suspend fun getByDoctor(doctorId: Long): List<AppointmentEntity>

    @Query("SELECT * FROM appointments WHERE petId = :petId")
    suspend fun getByPet(petId: Long): List<AppointmentEntity>

    @Query("SELECT COUNT(*) FROM appointments")
    suspend fun count(): Int
}