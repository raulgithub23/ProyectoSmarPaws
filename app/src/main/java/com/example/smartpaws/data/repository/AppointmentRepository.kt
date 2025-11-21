package com.example.smartpaws.data.repository

import com.example.smartpaws.data.remote.RemoteModule
import com.example.smartpaws.data.remote.appointments.AppointmentApiService
import com.example.smartpaws.data.remote.appointments.AppointmentRequestDto
import com.example.smartpaws.data.remote.appointments.AppointmentResponseDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException

class AppointmentRepository(
    private val api: AppointmentApiService =
        RemoteModule.createAppointmentService(AppointmentApiService::class.java)
) {


    suspend fun getAppointmentsByUser(userId: Long): Result<List<AppointmentResponseDto>> = try {
        val response = api.getAppointmentsByUser(userId)
        if (response.isSuccessful) {
            Result.success(response.body() ?: emptyList())
        } else {
            Result.failure(HttpException(response))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun observeAppointmentsByUser(userId: Long): Flow<Result<List<AppointmentResponseDto>>> = flow {
        emit(getAppointmentsByUser(userId))
    }

    suspend fun getUpcomingAppointmentsByUser(userId: Long): Result<List<AppointmentResponseDto>> = try {
        val response = api.getUpcomingAppointmentsByUser(userId)
        if (response.isSuccessful) {
            Result.success(response.body() ?: emptyList())
        } else {
            Result.failure(HttpException(response))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getAppointmentsByDoctorAndDate(doctorId: Long, date: String): Result<List<AppointmentResponseDto>> = try {
        val response = api.getAppointmentsByDoctorAndDate(doctorId, date)
        if (response.isSuccessful) {
            Result.success(response.body() ?: emptyList())
        } else {
            Result.failure(HttpException(response))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }


    suspend fun getAppointmentDetail(appointmentId: Long): Result<AppointmentResponseDto> = try {
        val response = api.getAppointmentById(appointmentId)
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            if (response.code() == 404) {
                Result.failure(IllegalArgumentException("Cita no encontrada"))
            } else {
                Result.failure(HttpException(response))
            }
        }
    } catch (e: Exception) {
        Result.failure(e)
    }


    suspend fun createAppointment(
        userId: Long?,
        petId: Long?,
        doctorId: Long,
        date: String,
        time: String,
        notes: String?
    ): Result<Long> = try {
        val request = AppointmentRequestDto(
            userId = userId,
            petId = petId,
            doctorId = doctorId,
            date = date,
            time = time,
            notes = notes
        )

        val response = api.createAppointment(request)

        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(HttpException(response))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteAppointmentById(appointmentId: Long): Result<Unit> = try {
        val response = api.deleteAppointment(appointmentId)
        if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            Result.failure(HttpException(response))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getAppointmentsForDoctor(doctorId: Long): Result<List<AppointmentResponseDto>> = try {
        val response = api.getAppointmentsByDoctor(doctorId)
        if (response.isSuccessful) {
            Result.success(response.body() ?: emptyList())
        } else {
            Result.failure(HttpException(response))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}