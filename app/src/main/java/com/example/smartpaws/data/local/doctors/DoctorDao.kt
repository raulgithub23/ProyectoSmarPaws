package com.example.smartpaws.data.local.doctors

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface DoctorDao {

    @Insert
    suspend fun insert(doctor: DoctorEntity): Long

    @Insert
    suspend fun insertSchedules(schedules: List<DoctorScheduleEntity>)

    //TRAE SOLO A UN DOCTOR Y LOS HORARIOS QUE TIENE
    @Transaction
    @Query("SELECT * FROM doctors WHERE id = :doctorId")
    suspend fun getDoctorWithSchedules(doctorId: Long): DoctorWithSchedules?

    //TRAE TODOS LOS DOCTORES Y SUS HORARIOS
    @Transaction
    @Query("SELECT * FROM doctors")
    suspend fun getAllDoctorsWithSchedules(): List<DoctorWithSchedules>

    //TRAE SOLO A LA TABLA DOCTORES
    @Query("SELECT * FROM doctors")
    suspend fun getAllDoctors(): List<DoctorEntity>

    //TRAE SOLO LOS HORARIOS DE UN DOCTOR
    @Query("SELECT * FROM doctor_schedules WHERE doctorId = :doctorId")
    suspend fun getSchedulesByDoctor(doctorId: Long): List<DoctorScheduleEntity>

    //CUENTA LOS DOCTORES
    @Query("SELECT COUNT(*) FROM doctors")
    suspend fun count(): Int

    //SE PODRÁ MODIFICAR ELDOCTOR
    @Update
    suspend fun update(doctor: DoctorEntity)

    // SE PODRÁ ELIMINAR EL DOCTOR
    @Delete
    suspend fun delete(doctor: DoctorEntity)

    // Borra todos los horarios de un doctor específico
    @Query("DELETE FROM doctor_schedules WHERE doctorId = :doctorId")
    suspend fun deleteSchedulesForDoctor(doctorId: Long)

    // Transacción para actualizar horarios: borra los viejos e inserta los nuevos
    @Transaction
    suspend fun updateSchedules(doctorId: Long, newSchedules: List<DoctorScheduleEntity>) {
        deleteSchedulesForDoctor(doctorId)
        // Aseguramos que los nuevos horarios tengan el ID de doctor correcto y ID de entidad 0
        val schedulesWithId = newSchedules.map { it.copy(id = 0L, doctorId = doctorId) }
        insertSchedules(schedulesWithId)
    }
}