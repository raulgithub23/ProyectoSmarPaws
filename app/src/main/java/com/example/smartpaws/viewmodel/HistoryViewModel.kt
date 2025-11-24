package com.example.smartpaws.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartpaws.data.remote.appointments.AppointmentResponseDto
import com.example.smartpaws.data.repository.AppointmentRepository
import com.example.smartpaws.data.repository.DoctorRepository
import com.example.smartpaws.data.repository.PetsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// Clase UI para mostrar los datos en la vista
data class HistoryAppointmentUiItem(
    val id: Long,
    val date: String,
    val time: String,
    val notes: String?,
    val petName: String,
    val doctorName: String,
    val doctorSpecialty: String?
)

data class HistoryUiState(
    val appointments: List<HistoryAppointmentUiItem> = emptyList(), // Lista de citas pasadas
    val isLoading: Boolean = false,  // Indica si está cargando datos
    val errorMsg: String? = null // Mensaje de error (null si no hay error)
)

data class DetailUiState(
    val appointment: HistoryAppointmentUiItem? = null,
    val isLoading: Boolean = false,
    val errorMsg: String? = null
)

class HistoryViewModel(
    private val repository: AppointmentRepository,
    private val petsRepository: PetsRepository,
    private val doctorRepository: DoctorRepository,
    private val authViewModel: AuthViewModel
) : ViewModel() {

    private val _historyState = MutableStateFlow(HistoryUiState())
    val historyState: StateFlow<HistoryUiState> = _historyState

    private val _detailState = MutableStateFlow(DetailUiState())
    val detailState: StateFlow<DetailUiState> = _detailState

    init {
        viewModelScope.launch {
            authViewModel.login.collect { loginState ->
                Log.d("HistoryViewModel", "Login state cambió - userId: ${loginState.userId}")
                if (loginState.userId != null) {
                    loadPastAppointments(loginState.userId)
                    _historyState.update {
                        it.copy(
                            errorMsg = null
                        )
                    }
                }
            }
        }
    }

    private fun loadPastAppointments(userId: Long) {
        viewModelScope.launch {
            _historyState.update { it.copy(isLoading = true, errorMsg = null) }
            Log.d("HistoryViewModel", "Cargando historial para usuario: $userId")

            try {
                val result = repository.getAppointmentsByUser(userId)

                result.fold(
                    onSuccess = { allAppointments ->
                        Log.d("HistoryViewModel", "Total citas recibidas: ${allAppointments.size}")

                        val mappedAppointments = allAppointments.map { dto ->
                            mapDtoToUiItem(dto)
                        }

                        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

                        val pastAppointments = mappedAppointments.filter { appointment ->
                            try {
                                val appDate = LocalDate.parse(appointment.date)

                                val appTime = try {
                                    LocalTime.parse(appointment.time)
                                } catch (e: Exception) {
                                    Log.e("HistoryViewModel", "Error parseando hora: ${appointment.time}", e)
                                    LocalTime(0, 0)
                                }

                                val appDateTime = LocalDateTime(appDate, appTime)

                                val isPast = appDateTime < now

                                Log.d("HistoryViewModel", "Cita ${appointment.date} ${appointment.time} es pasado? $isPast")
                                isPast

                            } catch (e: Exception) {
                                Log.e("HistoryViewModel", "Error procesando fecha completa: ${appointment.date}", e)
                                try {
                                    val appDate = LocalDate.parse(appointment.date)
                                    appDate < now.date
                                } catch (e2: Exception) {
                                    false
                                }
                            }
                        }.sortedWith(compareByDescending<HistoryAppointmentUiItem> { it.date }.thenByDescending { it.time })

                        Log.d("HistoryViewModel", "Citas visibles filtradas (Historial): ${pastAppointments.size}")

                        _historyState.update {
                            it.copy(
                                appointments = pastAppointments,
                                isLoading = false,
                                errorMsg = null
                            )
                        }
                    },
                    onFailure = { e ->
                        Log.e("HistoryViewModel", "Error cargando historial", e)
                        _historyState.update {
                            it.copy(
                                isLoading = false,
                                errorMsg = "Error al cargar historial: ${e.message}"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e("HistoryViewModel", "Error cargando historial (Excepción)", e)
                _historyState.update {
                    it.copy(
                        isLoading = false,
                        errorMsg = "Error al cargar historial: ${e.message}"
                    )
                }
            }
        }
    }

    fun loadAppointmentDetail(appointmentId: Long) {
        viewModelScope.launch {
            _detailState.update { it.copy(isLoading = true, errorMsg = null) }

            val result = repository.getAppointmentDetail(appointmentId)

            result.fold(
                onSuccess = { dto ->
                    val uiItem = mapDtoToUiItem(dto)
                    _detailState.update {
                        it.copy(
                            appointment = uiItem,
                            isLoading = false,
                            errorMsg = null
                        )
                    }
                },
                onFailure = { e ->
                    _detailState.update {
                        it.copy(
                            isLoading = false,
                            errorMsg = e.message ?: "Error al cargar detalle"
                        )
                    }
                }
            )
        }
    }

    private suspend fun mapDtoToUiItem(dto: AppointmentResponseDto): HistoryAppointmentUiItem {
        val petName = if (dto.petId != null) {
            petsRepository.getPetById(dto.petId).getOrNull()?.name ?: "Mascota desconocida"
        } else "Sin mascota"

        val doctorDto = doctorRepository.getDoctorWithSchedules(dto.doctorId).getOrNull()

        return HistoryAppointmentUiItem(
            id = dto.id,
            date = dto.date,
            time = dto.time,
            notes = dto.notes,
            petName = petName,
            doctorName = doctorDto?.name ?: "Doctor desconocido",
            doctorSpecialty = doctorDto?.specialty
        )
    }
}