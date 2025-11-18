package com.example.smartpaws.data.local.doctors

data class DoctorAppointmentSummary(
    val id: Long,
    val date: String,
    val time: String,
    val notes: String?,
    val petName: String,
    val petEspecie: String,
    val ownerName: String,
    val ownerPhone: String?
)
