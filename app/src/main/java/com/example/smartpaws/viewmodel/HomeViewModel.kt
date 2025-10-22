package com.example.smartpaws.viewmodel

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

   // init {
     //   loadHomeData()
    //}

   /* private fun loadHomeData() {
        viewModelScope.launch {
            _homeState.update { it.copy(isLoading = true, errorMsg = null) }

            try {
                // Combinar datos curiosos + citas
                combine(
                    petFactDao.getRandomFactByType("cat"),
                    repository.getUpcomingAppointments()
                ) { fact, appointments ->
                    HomeUiState(
                        currentFact = fact,
                        upcomingAppointments = appointments,
                        isLoading = false,
                        errorMsg = null
                    )
                }.collect { newState ->
                    _homeState.value = newState
                }
            } catch (e: Exception) {
                _homeState.update {
                    it.copy(
                        isLoading = false,
                        errorMsg = "Error al cargar datos: ${e.message}"
                    )
                }
            }
        }
    }

    */

    fun refreshFact() {
        viewModelScope.launch {
            try {
                petFactDao.getRandomFactByType("cat").first()?.let { newFact ->
                    _homeState.update { it.copy(currentFact = newFact) }
                }
            } catch (e: Exception) {
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