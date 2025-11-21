package com.example.smartpaws.data.local.doctors

import androidx.room.Embedded
import androidx.room.Relation
import com.example.smartpaws.data.remote.dto.ScheduleDto

data class DoctorWithSchedules(
    @Embedded val doctor: DoctorEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "doctorId"
    )
    val schedules: List<ScheduleDto>
)