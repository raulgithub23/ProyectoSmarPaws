package com.example.smartpaws.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartpaws.data.local.appointment.AppointmentWithDetails
import com.example.smartpaws.data.local.pets.PetFactDao
import com.example.smartpaws.data.local.pets.PetFactEntity
import com.example.smartpaws.data.repository.AppointmentRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Estado de la pantalla Home
data class HomeUiState(
    val currentFact: PetFactEntity? = null,
    val upcomingAppointments: List<AppointmentWithDetails> = emptyList(),
    val isLoading: Boolean = false,
    val errorMsg: String? = null
)

class HomeViewModel(
    private val repository: AppointmentRepository,
    private val petFactDao: PetFactDao
) : ViewModel() {

    private val _homeState = MutableStateFlow(HomeUiState())
    val homeState: StateFlow<HomeUiState> = _homeState

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            _homeState.update { it.copy(isLoading = true, errorMsg = null) }

            try {
                // Cargar datos curiosos (gatos Y perros)
                launch {
                    petFactDao.getAllFacts().collect { allFacts ->
                        Log.d("HomeViewModel", "Total de datos curiosos: ${allFacts.size}")
                        val randomFact = allFacts.randomOrNull()
                        Log.d("HomeViewModel", "Dato aleatorio: ${randomFact?.title} - ${randomFact?.type}")
                        _homeState.update { it.copy(currentFact = randomFact) }
                    }
                }

                // Cargar citas en paralelo
                launch {
                    repository.getUpcomingAppointments().collect { appointments ->
                        Log.d("HomeViewModel", "Citas encontradas: ${appointments.size}")
                        appointments.forEach {
                            Log.d("HomeViewModel", "  - ${it.petName}: ${it.date} ${it.time}")
                        }
                        _homeState.update {
                            it.copy(
                                upcomingAppointments = appointments,
                                isLoading = false
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "ERROR: ${e.message}", e)
                _homeState.update {
                    it.copy(
                        isLoading = false,
                        errorMsg = "Error al cargar datos: ${e.message}"
                    )
                }
            }
        }
    }

    fun refreshFact() {
        viewModelScope.launch {
            try {
                petFactDao.getAllFacts().first().let { allFacts ->
                    val newFact = allFacts.randomOrNull()
                    Log.d("HomeViewModel", "Refrescando dato: ${newFact?.title}")
                    _homeState.update { it.copy(currentFact = newFact) }
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error al refrescar: ${e.message}", e)
                _homeState.update {
                    it.copy(errorMsg = "Error al refrescar: ${e.message}")
                }
            }
        }
    }

    fun clearError() {
        _homeState.update { it.copy(errorMsg = null) }
    }
}