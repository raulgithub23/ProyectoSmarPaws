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
    private val repository: AppointmentRepository,
    private val authViewModel: AuthViewModel
) : ViewModel() {

    private val _historyState = MutableStateFlow(HistoryUiState())
    val historyState: StateFlow<HistoryUiState> = _historyState

    private val _detailState = MutableStateFlow(DetailUiState())
    val detailState: StateFlow<DetailUiState> = _detailState

    init {
        // Observa cambios en el userId
        viewModelScope.launch {
            authViewModel.login.collect { loginState ->
                Log.d("HistoryViewModel", "Login state cambió - userId: ${loginState.userId}")
                if (loginState.userId != null) {
                    loadPastAppointments(loginState.userId)
                } else {
                    // Si no hay usuario, limpiamos el estado
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
        viewModelScope.launch {
            _historyState.update { it.copy(isLoading = true, errorMsg = null) }
            Log.d("HistoryViewModel", "Cargando historial para usuario: $userId")

            try {
                // CAMBIO: Usa getAppointmentsByUser en lugar de getUpcomingAppointmentsByUser
                repository.getAppointmentsByUser(userId).collect { allAppointments ->
                    Log.d("HistoryViewModel", "Total citas recibidas: ${allAppointments.size}")

                    allAppointments.forEach { apt ->
                        Log.d("HistoryViewModel", "Cita: ${apt.date} - ${apt.petName} - ${apt.notes}")
                    }

                    val today = Clock.System.now()
                        .toLocalDateTime(TimeZone.currentSystemDefault()).date

                    Log.d("HistoryViewModel", "Fecha de hoy: $today")

                    val pastAppointments = allAppointments.filter { appointment ->
                        try {
                            val appointmentDate = LocalDate.parse(appointment.date)
                            val isPast = appointmentDate < today
                            Log.d("HistoryViewModel", "${appointment.date} < $today = $isPast")
                            isPast
                        } catch (e: Exception) {
                            Log.e("HistoryViewModel", "Error parseando fecha: ${appointment.date}", e)
                            false
                        }
                    }.sortedByDescending { it.date }

                    Log.d("HistoryViewModel", "Citas pasadas filtradas: ${pastAppointments.size}")

                    _historyState.update {
                        it.copy(
                            appointments = pastAppointments,
                            isLoading = false,
                            errorMsg = null
                        )
                    }
                }
            } catch (e: Exception) {
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