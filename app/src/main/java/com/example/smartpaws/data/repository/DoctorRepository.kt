package com.example.smartpaws.data.repository

import android.util.Log
import com.example.smartpaws.data.remote.DoctorApiService
import com.example.smartpaws.data.remote.RemoteModule
import com.example.smartpaws.data.remote.dto.CreateDoctorRequest
import com.example.smartpaws.data.remote.dto.DoctorDto
import com.example.smartpaws.data.remote.dto.ScheduleDto
import com.example.smartpaws.data.remote.dto.UpdateSchedulesRequest

class DoctorRepository(
    private val api: DoctorApiService = RemoteModule.createDoctorService(DoctorApiService::class.java)
) {

    suspend fun getAllDoctorsWithSchedules(): List<DoctorDto> {
        return try {
            api.getAllDoctors()
        } catch (e: Exception) {
            Log.e("DoctorRepository", "Error obteniendo doctores", e)
            emptyList()
        }
    }

    suspend fun getDoctorWithSchedules(doctorId: Long): Result<DoctorDto> {
        return try {
            val dto = api.getDoctorById(doctorId)
            Result.success(dto)
        } catch (e: Exception) {
            Log.e("DoctorRepository", "Error obteniendo doctor por ID: $doctorId", e)
            Result.failure(e)
        }
    }

    suspend fun getDoctorByEmail(email: String): Result<DoctorDto> {
        return try {
            val dto = api.getDoctorByEmail(email)
            Result.success(dto)
        } catch (e: Exception) {
            Log.e("DoctorRepository", "Error obteniendo doctor por email: $email", e)
            Result.failure(e)
        }
    }

    suspend fun createDoctorWithSchedules(
        name: String,
        specialty: String,
        email: String,
        phone: String? = null,
        schedules: List<ScheduleDto>
    ): Result<Long> {
        return try {
            val scheduleDtos = schedules.map {
                ScheduleDto(
                    id = null,
                    dayOfWeek = it.dayOfWeek,
                    startTime = it.startTime,
                    endTime = it.endTime
                )
            }
            val request = CreateDoctorRequest(name, specialty, email, phone, scheduleDtos)
            val response = api.createDoctor(request)
            Log.d("DoctorRepository", "Doctor creado exitosamente con ID: ${response.id}")
            Result.success(response.id)
        } catch (e: Exception) {
            Log.e("DoctorRepository", "Error creando doctor", e)
            Result.failure(e)
        }
    }

    suspend fun hasDoctors(): Boolean {
        return try {
            val count = api.countDoctors()
            count > 0
        } catch (e: Exception) {
            Log.e("DoctorRepository", "Error verificando existencia de doctores", e)
            false
        }
    }

    suspend fun updateSchedules(doctorId: Long, newSchedules: List<ScheduleDto>): Result<Unit> {
        return try {
            val scheduleDtos = newSchedules.map {
                ScheduleDto(
                    id = null,
                    dayOfWeek = it.dayOfWeek,
                    startTime = it.startTime,
                    endTime = it.endTime
                )
            }
            val request = UpdateSchedulesRequest(scheduleDtos)
            api.updateSchedules(doctorId, request)
            Log.d("DoctorRepository", "Horarios actualizados para doctor ID: $doctorId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("DoctorRepository", "Error actualizando horarios del doctor $doctorId", e)
            Result.failure(e)
        }
    }

    suspend fun deleteDoctor(doctorId: Long): Result<Unit> {
        return try {
            api.deleteDoctor(doctorId)
            Log.d("DoctorRepository", "Doctor eliminado ID: $doctorId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("DoctorRepository", "Error eliminando doctor $doctorId", e)
            Result.failure(e)
        }
    }
}