package com.example.smartpaws.data.repository

import com.example.smartpaws.data.local.doctors.DoctorDao
import com.example.smartpaws.data.local.doctors.DoctorEntity
import com.example.smartpaws.data.local.doctors.DoctorScheduleEntity
import com.example.smartpaws.data.local.doctors.DoctorWithSchedules

class DoctorRepository(
    private val doctorDao: DoctorDao
) {

    // Obtener todos los doctores con sus horarios
    suspend fun getAllDoctorsWithSchedules(): List<DoctorWithSchedules> {
        return try {
            val doctors = doctorDao.getAllDoctorsWithSchedules()
            android.util.Log.d("DoctorRepository", "Doctores obtenidos: ${doctors.size}")
            doctors
        } catch (e: Exception) {
            android.util.Log.e("DoctorRepository", "Error obteniendo doctores", e)
            emptyList()
        }
    }

    // Obtener un doctor específico con sus horarios
    suspend fun getDoctorWithSchedules(doctorId: Long): Result<DoctorWithSchedules> {
        return try {
            val doctor = doctorDao.getDoctorWithSchedules(doctorId)
            if (doctor != null) {
                Result.success(doctor)
            } else {
                Result.failure(IllegalArgumentException("Doctor no encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Crear doctor con sus horarios
    suspend fun createDoctorWithSchedules(
        name: String,
        specialty: String,
        email: String,
        phone: String? = null,
        schedules: List<DoctorScheduleEntity>
    ): Result<Long> {
        return try {
            val doctorId = doctorDao.insert(
                DoctorEntity(
                    name = name,
                    specialty = specialty,
                    email = email,
                    phone = phone
                )
            )

            // Insertar horarios asociados al doctor
            val schedulesWithDoctorId = schedules.map { it.copy(doctorId = doctorId) }
            doctorDao.insertSchedules(schedulesWithDoctorId)

            Result.success(doctorId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Verificar si hay doctores
    suspend fun hasDoctors(): Boolean {
        return try {
            doctorDao.count() > 0
        } catch (e: Exception) {
            false
        }
    }
}