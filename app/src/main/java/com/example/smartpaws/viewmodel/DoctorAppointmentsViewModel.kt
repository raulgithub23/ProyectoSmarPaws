package com.example.smartpaws.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartpaws.data.remote.appointments.AppointmentResponseDto
import com.example.smartpaws.data.repository.AppointmentRepository
import com.example.smartpaws.data.repository.DoctorRepository
import com.example.smartpaws.data.repository.PetsRepository
import com.example.smartpaws.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DoctorAppointmentUiItem(
    val id: Long,
    val date: String,
    val time: String,
    val notes: String?,
    val petId: Long?,
    val petName: String,
    val petEspecie: String,
    val ownerId: Long?,
    val ownerName: String,
    val ownerPhone: String?
)

data class DoctorAppointmentsUiState(
    val appointments: List<DoctorAppointmentUiItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val doctorName: String = ""
)

class DoctorAppointmentsViewModel(
    private val appointmentRepository: AppointmentRepository,
    private val doctorRepository: DoctorRepository,
    private val petRepository: PetsRepository,
    private val userRepository: UserRepository,
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
                val doctorResult = doctorRepository.getDoctorByEmail(userEmail)

                if (doctorResult.isSuccess) {
                    val doctorDto = doctorResult.getOrNull()!!

                    _uiState.update { it.copy(doctorName = doctorDto.name) }

                    val appointmentsResult = appointmentRepository.getAppointmentsForDoctor(doctorDto.id)

                    appointmentsResult.fold(
                        onSuccess = { dtoList ->
                            val uiItems = dtoList.map { dto ->
                                mapDtoToUiItem(dto)
                            }

                            _uiState.update {
                                it.copy(
                                    appointments = uiItems,
                                    isLoading = false,
                                    error = null
                                )
                            }
                        },
                        onFailure = { e ->
                            _uiState.update {
                                it.copy(isLoading = false, error = "Error obteniendo citas: ${e.message}")
                            }
                        }
                    )

                } else {
                    _uiState.update { it.copy(isLoading = false, error = "No se encontró perfil de doctor.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Error general: ${e.message}") }
            }
        }
    }

    private suspend fun mapDtoToUiItem(dto: AppointmentResponseDto): DoctorAppointmentUiItem {
        val petDto = if (dto.petId != null) {
            petRepository.getPetById(dto.petId).getOrNull()
        } else null

        val userDto = if (dto.userId != null) {
            try {
                userRepository.getUserById(dto.userId)
            } catch (e: Exception) {
                null
            }
        } else null

        return DoctorAppointmentUiItem(
            id = dto.id,
            date = dto.date,
            time = dto.time,
            notes = dto.notes,
            petId = dto.petId,
            petName = petDto?.name ?: "Mascota #${dto.petId ?: "?"}",
            petEspecie = petDto?.especie ?: "Desconocido",
            ownerId = dto.userId,
            ownerName = userDto?.name ?: "Dueño #${dto.userId ?: "?"}",
            ownerPhone = userDto?.phone ?: "Sin teléfono"
        )
    }
}