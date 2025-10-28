import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import androidx.lifecycle.viewModelScope
import com.example.smartpaws.data.local.appointment.AppointmentWithDetails
import com.example.smartpaws.data.repository.AppointmentRepository
import com.example.smartpaws.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime


data class HistoryUiState(
    val appointments: List<AppointmentWithDetails> = emptyList(), // Lista de citas pasad
    val isLoading: Boolean = false,  // Indica si está cargando datos
    val errorMsg: String? = null // Mensaje de error (null si no hay error)
)


data class DetailUiState( // Data class que representa el estado del detalle de una cita
    val appointment: AppointmentWithDetails? = null, // Cita seleccionada (null si no hay ninguna)
    val isLoading: Boolean = false,
    val errorMsg: String? = null
)

class HistoryViewModel(
    private val repository: AppointmentRepository,
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
                repository.getAppointmentsByUser(userId).collect { allAppointments ->
                    Log.d("HistoryViewModel", "Total citas recibidas: ${allAppointments.size}") // Log de cada cita para debugging

                    allAppointments.forEach { apt -> // Log de cada cita para debugging
                        Log.d("HistoryViewModel", "Cita: ${apt.date} - ${apt.petName} - ${apt.notes}")
                    }

                    val today = Clock.System.now()
                        .toLocalDateTime(TimeZone.currentSystemDefault()).date

                    Log.d("HistoryViewModel", "Fecha de hoy: $today")

                    val pastAppointments = allAppointments.filter { appointment -> // FILTRADO: Solo citas con fecha anterior a hoy
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
                }
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

            _detailState.update {  // Actualiza el estado según el resultado
                if (result.isSuccess) {
                    it.copy(
                        appointment = result.getOrNull(),
                        isLoading = false,
                        errorMsg = null
                    )
                } else { // Si fallo muestra el error
                    it.copy(
                        isLoading = false,
                        errorMsg = result.exceptionOrNull()?.message ?: "Error al cargar detalle"
                    )
                }
            }
        }
    }

    /**
     * Limpia el estado del detalle
     * Se usa cuando el usuario cierra la vista de detalle para liberar memoria
     */
    fun clearDetail() {
        _detailState.update { DetailUiState() }
    }
}