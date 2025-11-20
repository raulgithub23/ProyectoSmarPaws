package com.example.smartpaws.data.remote.dto

data class DoctorDto(
    val id: Long,
    val name: String,
    val specialty: String,
    val email: String,
    val phone: String?,
    val schedules: List<ScheduleDto>
)