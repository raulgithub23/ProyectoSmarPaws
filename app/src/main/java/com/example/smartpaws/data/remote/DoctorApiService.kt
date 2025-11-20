package com.example.smartpaws.data.remote

import com.example.smartpaws.data.remote.dto.CreateDoctorRequest
import com.example.smartpaws.data.remote.dto.DoctorDto
import com.example.smartpaws.data.remote.dto.UpdateSchedulesRequest
import retrofit2.http.*

interface DoctorApiService {

    @GET("doctors")
    suspend fun getAllDoctors(): List<DoctorDto>

    @GET("doctors/{id}")
    suspend fun getDoctorById(@Path("id") id: Long): DoctorDto

    @GET("doctors/by-email")
    suspend fun getDoctorByEmail(@Query("email") email: String): DoctorDto

    @POST("doctors")
    suspend fun createDoctor(@Body request: CreateDoctorRequest): DoctorDto

    @PUT("doctors/{id}/schedules")
    suspend fun updateSchedules(
        @Path("id") id: Long,
        @Body request: UpdateSchedulesRequest
    ): DoctorDto

    @DELETE("doctors/{id}")
    suspend fun deleteDoctor(@Path("id") id: Long)

    @GET("doctors/count")
    suspend fun countDoctors(): Long
}