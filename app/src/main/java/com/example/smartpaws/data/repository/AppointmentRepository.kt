package com.example.smartpaws.data.repository

import com.example.smartpaws.data.local.appointment.AppointmentDao
import com.example.smartpaws.data.local.appointment.AppointmentEntity
import com.example.smartpaws.data.local.appointment.AppointmentWithDetails
import com.example.smartpaws.data.local.doctors.DoctorAppointmentSummary
import kotlinx.coroutines.flow.Flow

class AppointmentRepository(
    private val appointmentDao: AppointmentDao
) {

    // Flow para citas de un usuario específico
    fun getAppointmentsByUser(userId: Long): Flow<List<AppointmentWithDetails>> {
        return appointmentDao.getAppointmentsByUser(userId)
    }

    fun getUpcomingAppointmentsByUser(userId: Long): Flow<List<AppointmentWithDetails>> {
        return appointmentDao.getUpcomingAppointmentsByUser(userId)
    }

    suspend fun getAppointmentsByDoctorAndDate(doctorId: Long, date: String): List<AppointmentEntity> {
        return appointmentDao.getAppointmentsByDoctorAndDate(doctorId, date)
    }

    suspend fun getAppointmentDetail(appointmentId: Long): Result<AppointmentWithDetails> {
        return try {
            val appointment = appointmentDao.getAppointmentById(appointmentId)
            if (appointment != null) {
                Result.success(appointment)
            } else {
                Result.failure(IllegalArgumentException("Cita no encontrada"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createAppointment(
        userId: Long?,
        petId: Long?,
        doctorId: Long,
        date: String,
        time: String,
        notes: String?
    ): Result<Long> {
        return try {
            val id = appointmentDao.insert(
                AppointmentEntity(
                    userId = userId,
                    petId = petId,
                    doctorId = doctorId,
                    date = date,
                    time = time,
                    notes = notes
                )
            )
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAppointmentById(appointmentId: Long): Result<Unit> {
        return try {
            appointmentDao.deleteById(appointmentId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAppointmentsForDoctor(doctorId: Long): Flow<List<DoctorAppointmentSummary>> {
        return appointmentDao.getAppointmentsByDoctor(doctorId)
    }
}