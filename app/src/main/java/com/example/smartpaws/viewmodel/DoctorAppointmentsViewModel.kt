package com.example.smartpaws.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartpaws.data.local.doctors.DoctorAppointmentSummary
import com.example.smartpaws.data.repository.AppointmentRepository
import com.example.smartpaws.data.repository.DoctorRepository
import com.example.smartpaws.data.repository.UserRepository
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
                // 1. Buscar al doctor usando el email del usuario logueado
                // Esto asume que el email del usuario coincide con el email registrado en la tabla Doctors
                val doctors = doctorRepository.getAllDoctorsWithSchedules()
                val currentDoctor = doctors.find { it.doctor.email == userEmail }

                if (currentDoctor != null) {
                    _uiState.update { it.copy(doctorName = currentDoctor.doctor.name) }

                    // 2. Obtener las citas de ese doctor
                    appointmentRepository.getAppointmentsForDoctor(currentDoctor.doctor.id)
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
                            error = "No se encontró un perfil de doctor asociado a este usuario ($userEmail)."
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

class DoctorAppointmentsViewModelFactory(
    private val appointmentRepository: AppointmentRepository,
    private val doctorRepository: DoctorRepository,
    private val userId: Long,
    private val userEmail: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DoctorAppointmentsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DoctorAppointmentsViewModel(
                appointmentRepository,
                doctorRepository,
                userId,
                userEmail
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}