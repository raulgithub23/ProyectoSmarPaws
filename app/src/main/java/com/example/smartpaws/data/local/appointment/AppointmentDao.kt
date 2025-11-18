package com.example.smartpaws.data.local.appointment

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.smartpaws.data.local.appointment.AppointmentWithDetails
import kotlinx.coroutines.flow.Flow
import com.example.smartpaws.data.local.doctors.DoctorAppointmentSummary


/*
* abstraer y definir la interfaz para interactuar con la base de datos (en este caso, la base de datos SQLite que Room maneja)
* como el "traductor" entre tu código Kotlin/Java y la base de datos SQL
* */
@Dao
interface AppointmentDao {

    // Contar registros (para seeds)
    @Query("SELECT COUNT(*) FROM appointments")
    suspend fun count(): Int

    // Obtener TODAS las citas (para el historial general)
    @Query("""
        SELECT a.*, p.name as petName, d.name as doctorName
        FROM appointments a
        INNER JOIN pets p ON a.petId = p.id
        INNER JOIN doctors d ON a.doctorId = d.id
        ORDER BY a.date DESC, a.time DESC
    """)
    fun getAllAppointments(): Flow<List<AppointmentWithDetails>>

    //Obtener próximas 3 citas (todas)
    @Query("""
        SELECT a.*, p.name as petName, d.name as doctorName, d.specialty as doctorSpecialty
        FROM appointments a
        INNER JOIN pets p ON a.petId = p.id
        INNER JOIN doctors d ON a.doctorId = d.id
        WHERE a.date >= date('now')
        ORDER BY a.date ASC, a.time ASC
        LIMIT 3
    """)
    fun getUpcomingAppointments(): Flow<List<AppointmentWithDetails>>

    // Obtener citas de un usuario específico
    @Query("""
        SELECT a.*, p.name as petName, d.name as doctorName
        FROM appointments a
        INNER JOIN pets p ON a.petId = p.id
        INNER JOIN doctors d ON a.doctorId = d.id
        WHERE a.userId = :userId
        ORDER BY a.date DESC, a.time DESC
    """)
    fun getAppointmentsByUser(userId: Long): Flow<List<AppointmentWithDetails>>

    // Obtener próximas 3 citas de un usuario
    @Query("""
        SELECT a.*, p.name as petName, d.name as doctorName, d.specialty as doctorSpecialty
        FROM appointments a
        INNER JOIN pets p ON a.petId = p.id
        INNER JOIN doctors d ON a.doctorId = d.id
        WHERE a.userId = :userId AND a.date >= date('now')
        ORDER BY a.date ASC, a.time ASC
        LIMIT 3
    """)
    fun getUpcomingAppointmentsByUser(userId: Long): Flow<List<AppointmentWithDetails>>

    @Query("SELECT * FROM appointments WHERE doctorId = :doctorId AND date = :date")
    suspend fun getAppointmentsByDoctorAndDate(doctorId: Long, date: String): List<AppointmentEntity>

    // Obtener detalle de una cita específica
    @Query("""
        SELECT a.*, p.name as petName, d.name as doctorName, d.specialty as doctorSpecialty
        FROM appointments a
        INNER JOIN pets p ON a.petId = p.id
        INNER JOIN doctors d ON a.doctorId = d.id
        WHERE a.id = :appointmentId
    """)
    suspend fun getAppointmentById(appointmentId: Long): AppointmentWithDetails?

    @Insert
    suspend fun insert(appointment: AppointmentEntity): Long

    @Query("DELETE FROM appointments WHERE id = :appointmentId")
    suspend fun deleteById(appointmentId: Long)

    @Query("""
        SELECT 
            a.id, a.date, a.time, a.notes,
            p.name as petName, p.especie as petEspecie,
            u.name as ownerName, u.phone as ownerPhone
        FROM appointments a
        INNER JOIN pets p ON a.petId = p.id
        INNER JOIN users u ON a.userId = u.id
        WHERE a.doctorId = :doctorId
        ORDER BY a.date ASC, a.time ASC
    """)
    fun getAppointmentsByDoctor(doctorId: Long): Flow<List<DoctorAppointmentSummary>>
}