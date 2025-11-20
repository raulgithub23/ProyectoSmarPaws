package com.example.smartpaws.data.local.appointment


import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.smartpaws.data.local.doctors.DoctorEntity
import com.example.smartpaws.data.local.pets.PetsEntity
import com.example.smartpaws.data.local.user.UserEntity

@Entity(
    tableName = "appointments",
    foreignKeys = [
        ForeignKey(
            entity = DoctorEntity::class,
            parentColumns = ["id"],
            childColumns = ["doctorId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PetsEntity::class,
            parentColumns = ["id"],
            childColumns = ["petId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long?,
    val petId: Long?,
    val doctorId: Long,
    val date: String,         // Ej: "2025-10-22"
    val time: String,         // Ej: "10:30"
    val notes: String? = null
)

