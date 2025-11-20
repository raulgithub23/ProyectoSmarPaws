package com.example.smartpaws.data.repository

import com.example.smartpaws.data.local.doctors.DoctorEntity
import com.example.smartpaws.data.local.doctors.DoctorScheduleEntity
import com.example.smartpaws.data.local.doctors.DoctorWithSchedules
import com.example.smartpaws.data.remote.DoctorApiService
import com.example.smartpaws.data.remote.RemoteModule
import com.example.smartpaws.data.remote.dto.CreateDoctorRequest
import com.example.smartpaws.data.remote.dto.ScheduleDto
import com.example.smartpaws.data.remote.dto.UpdateSchedulesRequest

class DoctorRepository {

    private val api: DoctorApiService = RemoteModule.createDoctorService(DoctorApiService::class.java)

    suspend fun getAllDoctorsWithSchedules(): List<DoctorWithSchedules> {
        return try {
            val doctorsDto = api.getAllDoctors()
            doctorsDto.map { dto ->
                val doctor = DoctorEntity(
                    id = dto.id,
                    name = dto.name,
                    specialty = dto.specialty,
                    email = dto.email,
                    phone = dto.phone
                )
                val schedules = dto.schedules.map { scheduleDto ->
                    DoctorScheduleEntity(
                        id = scheduleDto.id ?: 0L,
                        doctorId = dto.id,
                        dayOfWeek = scheduleDto.dayOfWeek,
                        startTime = scheduleDto.startTime,
                        endTime = scheduleDto.endTime
                    )
                }
                DoctorWithSchedules(doctor, schedules)
            }
        } catch (e: Exception) {
            android.util.Log.e("DoctorRepository", "Error obteniendo doctores", e)
            emptyList()
        }
    }

    suspend fun getDoctorWithSchedules(doctorId: Long): Result<DoctorWithSchedules> {
        return try {
            val dto = api.getDoctorById(doctorId)
            val doctor = DoctorEntity(
                id = dto.id,
                name = dto.name,
                specialty = dto.specialty,
                email = dto.email,
                phone = dto.phone
            )
            val schedules = dto.schedules.map { scheduleDto ->
                DoctorScheduleEntity(
                    id = scheduleDto.id ?: 0L,
                    doctorId = dto.id,
                    dayOfWeek = scheduleDto.dayOfWeek,
                    startTime = scheduleDto.startTime,
                    endTime = scheduleDto.endTime
                )
            }
            Result.success(DoctorWithSchedules(doctor, schedules))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDoctorByEmail(email: String): Result<DoctorWithSchedules> {
        return try {
            val dto = api.getDoctorByEmail(email)
            val doctor = DoctorEntity(
                id = dto.id,
                name = dto.name,
                specialty = dto.specialty,
                email = dto.email,
                phone = dto.phone
            )
            val schedules = dto.schedules.map { scheduleDto ->
                DoctorScheduleEntity(
                    id = scheduleDto.id ?: 0L,
                    doctorId = dto.id,
                    dayOfWeek = scheduleDto.dayOfWeek,
                    startTime = scheduleDto.startTime,
                    endTime = scheduleDto.endTime
                )
            }
            Result.success(DoctorWithSchedules(doctor, schedules))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createDoctorWithSchedules(
        name: String,
        specialty: String,
        email: String,
        phone: String? = null,
        schedules: List<DoctorScheduleEntity>
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
            Result.success(response.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun hasDoctors(): Boolean {
        return try {
            api.countDoctors() > 0
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateSchedules(doctorId: Long, newSchedules: List<DoctorScheduleEntity>): Result<Unit> {
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
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteDoctor(doctor: DoctorEntity): Result<Unit> {
        return try {
            api.deleteDoctor(doctor.id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}