package com.example.smartpaws.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import androidx.lifecycle.viewModelScope
import com.example.smartpaws.data.remote.appointments.AppointmentResponseDto
import com.example.smartpaws.data.repository.AppointmentRepository
import com.example.smartpaws.data.repository.DoctorRepository
import com.example.smartpaws.data.repository.PetsRepository
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// Clase UI para reemplazar a AppointmentWithDetails en la vista
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
    val appointments: List<HistoryAppointmentUiItem> = emptyList(), // Lista de citas pasad
    val isLoading: Boolean = false,  // Indica si está cargando datos
    val errorMsg: String? = null // Mensaje de error (null si no hay error)
)


data class DetailUiState( // Data class que representa el estado del detalle de una cita
    val appointment: HistoryAppointmentUiItem? = null, // Cita seleccionada (null si no hay ninguna)
    val isLoading: Boolean = false,
    val errorMsg: String? = null
)

class HistoryViewModel(
    private val repository: AppointmentRepository,
    private val petsRepository: PetsRepository,
    private val doctorRepository: DoctorRepository,
    private val authViewModel: AuthViewModel
) : ViewModel() {

    private val _historyState = MutableStateFlow(HistoryUiState())     // Estado privado mutable para el historial (solo este ViewModel puede modificarlo)
    val historyState: StateFlow<HistoryUiState> = _historyState     // Estado público inmutable que la UI puede observar

    private val _detailState = MutableStateFlow(DetailUiState())     // Estado privado mutable para el detalle de una cita
    val detailState: StateFlow<DetailUiState> = _detailState     // Estado público inmutable para el detalle


    init {     // Bloque init: se ejecuta al crear el ViewModel
        viewModelScope.launch {        // Observa cambios en el estado de login del AuthViewModel
            authViewModel.login.collect { loginState -> // collect: escucha continuamente los cambios en el estado de login
                Log.d("HistoryViewModel", "Login state cambió - userId: ${loginState.userId}") // Log para debugging: registra cuando cambia el userId
                if (loginState.userId != null) { // Si hay un usuario logueado, carga su historial
                    loadPastAppointments(loginState.userId)
                } else { // Si no hay usuario (logout), limpia el estado
                    _historyState.update {
                        it.copy(
                            appointments = emptyList(),
                            isLoading = false,
                            errorMsg = null
                        )
                    }
                }
            }
        }
    }

    private fun loadPastAppointments(userId: Long) {
        // Activa el estado de carga y limpia errores previos
        viewModelScope.launch {
            _historyState.update { it.copy(isLoading = true, errorMsg = null) }
            Log.d("HistoryViewModel", "Cargando historial para usuario: $userId")

            try {
                // CAMBIO: Usa getAppointmentsByUser en lugar de getUpcomingAppointmentsByUser
                val result = repository.getAppointmentsByUser(userId)

                result.fold(
                    onSuccess = { allAppointments ->
                        Log.d("HistoryViewModel", "Total citas recibidas: ${allAppointments.size}") // Log de cada cita para debugging

                        // Mapeamos DTO a UI Item buscando nombres (Crucial para microservicios)
                        val mappedAppointments = allAppointments.map { dto ->
                            mapDtoToUiItem(dto)
                        }

                        mappedAppointments.forEach { apt -> // Log de cada cita para debugging
                            Log.d("HistoryViewModel", "Cita: ${apt.date} - ${apt.petName} - ${apt.notes}")
                        }

                        val today = Clock.System.now()
                            .toLocalDateTime(TimeZone.currentSystemDefault()).date

                        Log.d("HistoryViewModel", "Fecha de hoy: $today")

                        val pastAppointments = mappedAppointments.filter { appointment -> // FILTRADO: Solo citas con fecha anterior a hoy
                            try {
                                val appointmentDate = LocalDate.parse(appointment.date) // Convierte el String de fecha a LocalDate para comparar
                                val isPast = appointmentDate < today // Verifica si la fecha de la cita es menor (anterior) que hoy
                                Log.d("HistoryViewModel", "${appointment.date} < $today = $isPast")
                                isPast
                            } catch (e: Exception) { // Si hay error al parsear la fecha, registra el error y excluye esa cita
                                Log.e("HistoryViewModel", "Error parseando fecha: ${appointment.date}", e)
                                false
                            }
                        }.sortedByDescending { it.date }

                        Log.d("HistoryViewModel", "Citas pasadas filtradas: ${pastAppointments.size}")

                        // Actualiza el estado con las citas filtradas
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
                // Si hay cualquier error lo registra y muestra mensaje al usuario
                Log.e("HistoryViewModel", "Error cargando historial", e)
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
            _detailState.update { it.copy(isLoading = true, errorMsg = null) } // Activa el estado de carga del detalle

            val result = repository.getAppointmentDetail(appointmentId) // Obtiene el detalle de la cita desde el repositorio

            result.fold(
                onSuccess = { dto ->
                    val uiItem = mapDtoToUiItem(dto)
                    _detailState.update {  // Actualiza el estado según el resultado
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

    /**
     * Limpia el estado del detalle
     * Se usa cuando el usuario cierra la vista de detalle para liberar memoria
     */
    fun clearDetail() {
        _detailState.update { DetailUiState() }
    }

    // Función auxiliar para mapear IDs a Nombres
    private suspend fun mapDtoToUiItem(dto: AppointmentResponseDto): HistoryAppointmentUiItem {
        val petName = if (dto.petId != null) {
            petsRepository.getPetById(dto.petId).getOrNull()?.name ?: "Mascota desconocida"
        } else "Sin mascota"

        val doctorObj = doctorRepository.getDoctorWithSchedules(dto.doctorId).getOrNull()

        return HistoryAppointmentUiItem(
            id = dto.id,
            date = dto.date,
            time = dto.time,
            notes = dto.notes,
            petName = petName,
            doctorName = doctorObj?.doctor?.name ?: "Doctor desconocido",
            doctorSpecialty = doctorObj?.doctor?.specialty
        )
    }
}