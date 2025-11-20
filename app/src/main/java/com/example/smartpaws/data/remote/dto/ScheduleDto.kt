package com.example.smartpaws.data.remote.dto

data class ScheduleDto(
    val id: Long?,
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String
)