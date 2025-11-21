package com.example.smartpaws.data.remote.appointments

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AppointmentApiService {

    // Endpoint: @GetMapping("/api/appointments")
    // Obtener historial general de citas
    @GET("api/appointments")
    suspend fun getAllAppointments(): Response<List<AppointmentResponseDto>>

    // Endpoint: @GetMapping("/api/appointments/upcoming")
    // Obtener próximas 3 citas generales
    @GET("api/appointments/upcoming")
    suspend fun getUpcomingAppointments(): Response<List<AppointmentResponseDto>>

    // Endpoint: @GetMapping("/api/appointments/user/{userId}")
    // Obtener historial de citas por usuario
    @GET("api/appointments/user/{userId}")
    suspend fun getAppointmentsByUser(@Path("userId") userId: Long): Response<List<AppointmentResponseDto>>

    // Endpoint: @GetMapping("/api/appointments/user/{userId}/upcoming")
    // Obtener próximas 3 citas de un usuario específico
    @GET("api/appointments/user/{userId}/upcoming")
    suspend fun getUpcomingAppointmentsByUser(@Path("userId") userId: Long): Response<List<AppointmentResponseDto>>

    // Endpoint: @GetMapping("/api/appointments/doctor/{doctorId}")
    // Obtener citas asignadas a un doctor
    @GET("api/appointments/doctor/{doctorId}")
    suspend fun getAppointmentsByDoctor(@Path("doctorId") doctorId: Long): Response<List<AppointmentResponseDto>>

    // Endpoint: @GetMapping("/api/appointments/doctor/{doctorId}/date")
    // Obtener citas por doctor y fecha específica (formato fecha: yyyy-MM-dd)
    @GET("api/appointments/doctor/{doctorId}/date")
    suspend fun getAppointmentsByDoctorAndDate(
        @Path("doctorId") doctorId: Long,
        @Query("date") date: String
    ): Response<List<AppointmentResponseDto>>

    // Endpoint: @GetMapping("/api/appointments/{id}")
    // Obtener detalle de una cita por ID
    @GET("api/appointments/{id}")
    suspend fun getAppointmentById(@Path("id") id: Long): Response<AppointmentResponseDto>

    // Endpoint: @PostMapping("/api/appointments")
    // Agendar una nueva cita. Retorna el ID de la cita creada (Long).
    @POST("api/appointments")
    suspend fun createAppointment(@Body request: AppointmentRequestDto): Response<Long>

    // Endpoint: @DeleteMapping("/api/appointments/{id}")
    // Cancelar/Eliminar una cita
    @DELETE("api/appointments/{id}")
    suspend fun deleteAppointment(@Path("id") id: Long): Response<Unit>

    // Endpoint: @GetMapping("/api/appointments/count")
    // Total de citas (opcional, útil para stats)
    @GET("api/appointments/count")
    suspend fun countAppointments(): Response<Long>
}