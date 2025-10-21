package com.example.smartpaws.data.local.doctors

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "doctor_schedules",
    foreignKeys = [
        ForeignKey(
            entity = DoctorEntity::class,
            parentColumns = ["id"],
            childColumns = ["doctorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["doctorId"])]
)
data class DoctorScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val doctorId: Long,                 // Foreign Key al doctor
    val dayOfWeek: String,              // "Lunes", "Martes", etc.
    val startTime: String,              // "09:00"
    val endTime: String                 // "18:00"
)
