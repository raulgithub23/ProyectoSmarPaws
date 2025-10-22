package com.example.smartpaws.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartpaws.data.local.appointment.AppointmentDao
import com.example.smartpaws.data.local.appointment.AppointmentWithDetails
import com.example.smartpaws.data.local.pets.PetFactEntity
import com.example.smartpaws.data.local.pets.PetFactDao
import kotlinx.coroutines.flow.*

class HomeViewModel(
    private val petFactDao: PetFactDao,
    private val appointmentDao: AppointmentDao,
    private val userId: Long? = null  // ID del usuario logueado (opcional)
) : ViewModel() {

    // Dato curioso aleatorio de gatos
    val randomCatFact: StateFlow<PetFactEntity?> = petFactDao
        .getRandomFactByType("cat")
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Dato curioso aleatorio de perros
    val randomDogFact: StateFlow<PetFactEntity?> = petFactDao
        .getRandomFactByType("dog")
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Próximas citas (filtradas por usuario si userId != null)
    val upcomingAppointments: StateFlow<List<AppointmentWithDetails>> =
        if (userId != null) {
            appointmentDao.getUpcomingAppointmentsByUser(userId)
        } else {
            appointmentDao.getUpcomingAppointments()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}