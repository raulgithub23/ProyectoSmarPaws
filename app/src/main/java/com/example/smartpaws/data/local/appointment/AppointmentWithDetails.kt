package com.example.smartpaws.data.local.appointment

data class AppointmentWithDetails(
    val id: Long,
    val userId: Long,
    val petId: Long,
    val doctorId: Long,
    val date: String,
    val time: String,
    val notes: String?,
    val petName: String,
    val doctorName: String,
    val doctorSpecialty: String? = null
)

