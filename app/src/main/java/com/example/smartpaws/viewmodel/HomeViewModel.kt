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
    private val petFactDao: PetFactDao,
    private val authViewModel: AuthViewModel // se recibe el authViewModel opara sacar la id del ususraio

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
                // Cargar datos curiosos
                launch {
                    petFactDao.getAllFacts().collect { allFacts ->
                        val randomFact = allFacts.randomOrNull()
                        _homeState.update { it.copy(currentFact = randomFact) }
                    }
                }

                // Cargar citas OBSERVANDO el userId
                launch {
                    authViewModel.login.collect { loginState ->
                        if (loginState.userId != null) {
                            repository.getUpcomingAppointmentsByUser(loginState.userId).collect { appointments ->
                                _homeState.update {
                                    it.copy(
                                        upcomingAppointments = appointments,
                                        isLoading = false
                                    )
                                }
                            }
                        } else {
                            _homeState.update {
                                it.copy(
                                    upcomingAppointments = emptyList(),
                                    isLoading = false,
                                    errorMsg = null // Quitamos el error cuando no hay usuario
                                )
                            }
                        }
                    }
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