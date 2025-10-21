package com.example.smartpaws.data.local.doctors

import androidx.room.Embedded
import androidx.room.Relation

data class DoctorWithSchedules(
    @Embedded val doctor: DoctorEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "doctorId"
    )
    val schedules: List<DoctorScheduleEntity>
)