package com.example.smartpaws.data.remote.appointments

data class AppointmentRequestDto(
   val userId: Long?,
   val petId: Long?,
   val doctorId: Long,
   val date: String,
   val time: String,
   val notes: String? = null
)

data class AppointmentResponseDto(
    val id: Long,
    val userId: Long?,
    val petId: Long?,
    val doctorId: Long,
    val date: String,
    val time: String,
    val notes: String?
)
