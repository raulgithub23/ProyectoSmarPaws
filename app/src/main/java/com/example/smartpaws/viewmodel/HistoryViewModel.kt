package com.example.smartpaws.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import androidx.lifecycle.viewModelScope
import com.example.smartpaws.data.local.appointment.AppointmentWithDetails
import com.example.smartpaws.data.repository.AppointmentRepository
import kotlinx.coroutines.launch


data class HistoryUiState(
    val appointments: List<AppointmentWithDetails> = emptyList(),
    val isLoading: Boolean = false,
    val errorMsg: String? = null
)

data class DetailUiState(
    val appointment: AppointmentWithDetails? = null,
    val isLoading: Boolean = false,
    val errorMsg: String? = null
)

class HistoryViewModel(
    private val repository: AppointmentRepository
) : ViewModel() {

    private val _historyState = MutableStateFlow(HistoryUiState())
    val historyState: StateFlow<HistoryUiState> = _historyState

    private val _detailState = MutableStateFlow(DetailUiState())
    val detailState: StateFlow<DetailUiState> = _detailState

    init {
        loadAllAppointments()
    }

    // Cargar todas las citas
    private fun loadAllAppointments() {
        viewModelScope.launch {
            _historyState.update { it.copy(isLoading = true, errorMsg = null) }

            repository.getAllAppointments().collect { appointments ->
                _historyState.update {
                    it.copy(
                        appointments = appointments,
                        isLoading = false,
                        errorMsg = null
                    )
                }
            }
        }
    }

    // Cargar detalle de una cita (para el "Ver más")
    fun loadAppointmentDetail(appointmentId: Long) {
        viewModelScope.launch {
            _detailState.update { it.copy(isLoading = true, errorMsg = null) }

            val result = repository.getAppointmentDetail(appointmentId)

            _detailState.update {
                if (result.isSuccess) {
                    it.copy(
                        appointment = result.getOrNull(),
                        isLoading = false,
                        errorMsg = null
                    )
                } else {
                    it.copy(
                        isLoading = false,
                        errorMsg = result.exceptionOrNull()?.message ?: "Error al cargar detalle"
                    )
                }
            }
        }
    }

    fun clearDetail() {
        _detailState.update { DetailUiState() }
    }
}