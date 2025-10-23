
package com.example.smartpaws.data.repository

import com.example.smartpaws.data.local.appointment.AppointmentDao
import com.example.smartpaws.data.local.appointment.AppointmentEntity
import com.example.smartpaws.data.local.appointment.AppointmentWithDetails
import kotlinx.coroutines.flow.Flow

class AppointmentRepository(
    private val appointmentDao: AppointmentDao
) {

    // Flow para obtener todas las citas (reactivo)
    fun getAllAppointments(): Flow<List<AppointmentWithDetails>> {
        return appointmentDao.getAllAppointments()
    }

    // Flow para citas de un usuario específico
    fun getAppointmentsByUser(userId: Long): Flow<List<AppointmentWithDetails>> {
        return appointmentDao.getAppointmentsByUser(userId)
    }

    // Flow para próximas 3 citas (todas)
    fun getUpcomingAppointments(): Flow<List<AppointmentWithDetails>> {
        return appointmentDao.getUpcomingAppointments()
    }

    //Flow para próximas 3 citas de un usuario
    fun getUpcomingAppointmentsByUser(userId: Long): Flow<List<AppointmentWithDetails>> {
        return appointmentDao.getUpcomingAppointmentsByUser(userId)
    }

    // Obtener detalle con Result<T>
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

    // Crear cita (como el register del profe)
    suspend fun createAppointment(
        userId: Long?,
        petId: Long,
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
}