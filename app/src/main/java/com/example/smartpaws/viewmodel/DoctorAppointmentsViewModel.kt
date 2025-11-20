package com.example.smartpaws.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartpaws.data.local.doctors.DoctorAppointmentSummary
import com.example.smartpaws.data.repository.AppointmentRepository
import com.example.smartpaws.data.repository.DoctorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DoctorAppointmentsUiState(
    val appointments: List<DoctorAppointmentSummary> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val doctorName: String = ""
)

class DoctorAppointmentsViewModel(
    private val appointmentRepository: AppointmentRepository,
    private val doctorRepository: DoctorRepository,
    private val userId: Long,
    private val userEmail: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(DoctorAppointmentsUiState())
    val uiState: StateFlow<DoctorAppointmentsUiState> = _uiState.asStateFlow()

    init {
        loadDoctorAppointments()
    }

    private fun loadDoctorAppointments() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val result = doctorRepository.getDoctorByEmail(userEmail)

                if (result.isSuccess) {
                    val doctorWithSchedules = result.getOrNull()!!
                    _uiState.update { it.copy(doctorName = doctorWithSchedules.doctor.name) }

                    appointmentRepository.getAppointmentsForDoctor(doctorWithSchedules.doctor.id)
                        .collect { appointments ->
                            _uiState.update {
                                it.copy(
                                    appointments = appointments,
                                    isLoading = false
                                )
                            }
                        }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "No se encontró un perfil de doctor asociado."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Error al cargar citas: ${e.message}"
                    )
                }
            }
        }
    }
}