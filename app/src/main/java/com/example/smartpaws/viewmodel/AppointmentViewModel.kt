package com.example.smartpaws.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartpaws.data.local.doctors.DoctorWithSchedules
import com.example.smartpaws.data.repository.AppointmentRepository
import com.example.smartpaws.data.repository.DoctorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.*
@RequiresApi(Build.VERSION_CODES.O)
class AppointmentViewModel(
    private val repository: AppointmentRepository,
    private val doctorRepository: DoctorRepository,
    private val userId: Long?  // Pasado desde AuthViewModel
) : ViewModel() {

    // Estados de UI
    private val _uiState = MutableStateFlow(AppointmentUiState())
    val uiState: StateFlow<AppointmentUiState> = _uiState

    // Cargar doctores disponibles al iniciar
    init {
        loadDoctors()
    }

    private fun loadDoctors() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val doctors = doctorRepository.getAllDoctorsWithSchedules()
                android.util.Log.d("AppointmentVM", "Doctores cargados: ${doctors.size}")
                doctors.forEach { doctor ->
                    android.util.Log.d("AppointmentVM", "Doctor: ${doctor.doctor.name}, Horarios: ${doctor.schedules.size}")
                }
                _uiState.update {
                    it.copy(
                        doctors = doctors,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("AppointmentVM", "Error cargando doctores", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMsg = "Error al cargar doctores: ${e.message}"
                    )
                }
            }
        }
    }

    fun selectDate(date: LocalDate) {
        _uiState.update {
            it.copy(
                selectedDate = date,
                selectedTime = null, // Reset hora al cambiar fecha
                availableTimes = emptyList()
            )
        }
        // Si ya hay un doctor seleccionado, cargar horarios disponibles
        _uiState.value.selectedDoctor?.let { loadAvailableTimesForDoctor(date, it) }
    }

    fun selectTime(time: String) {
        _uiState.update { it.copy(selectedTime = time) }
    }

    fun selectDoctor(doctor: DoctorWithSchedules) {
        _uiState.update {
            it.copy(
                selectedDoctor = doctor,
                selectedTime = null,
                availableTimes = emptyList()
            )
        }
        // Si ya hay fecha seleccionada, cargar horarios
        _uiState.value.selectedDate?.let { loadAvailableTimesForDoctor(it, doctor) }
    }

    fun nextMonth() {
        _uiState.update {
            it.copy(currentMonth = it.currentMonth.plusMonths(1))
        }
    }

    fun previousMonth() {
        val newMonth = _uiState.value.currentMonth.minusMonths(1)
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val currentYearMonth = today.toYearMonth()

        // No permitir ir a meses anteriores al actual
        if (newMonth.year > currentYearMonth.year ||
            (newMonth.year == currentYearMonth.year && newMonth.month >= currentYearMonth.month)) {
            _uiState.update { it.copy(currentMonth = newMonth) }
        }
    }

    private fun loadAvailableTimesForDoctor(date: LocalDate, doctor: DoctorWithSchedules) {
        val dayOfWeek = getDayOfWeekInSpanish(date)
        val schedule = doctor.schedules.find { it.dayOfWeek == dayOfWeek }

        if (schedule == null) {
            _uiState.update { it.copy(availableTimes = emptyList()) }
            return
        }

        val times = generateTimeSlots(schedule.startTime, schedule.endTime, date)
        _uiState.update { it.copy(availableTimes = times) }
    }

    private fun generateTimeSlots(startTime: String, endTime: String, date: LocalDate): List<String> {
        val slots = mutableListOf<String>()
        val start = startTime.split(":").map { it.toInt() }
        val end = endTime.split(":").map { it.toInt() }

        var currentHour = start[0]
        var currentMinute = start[1]
        val endHour = end[0]
        val endMinute = end[1]

        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

        while (currentHour < endHour || (currentHour == endHour && currentMinute < endMinute)) {
            // Si es hoy, filtrar horarios pasados
            if (date == today) {
                if (currentHour > now.hour || (currentHour == now.hour && currentMinute > now.minute)) {
                    slots.add(String.format("%02d:%02d", currentHour, currentMinute))
                }
            } else {
                slots.add(String.format("%02d:%02d", currentHour, currentMinute))
            }

            // Incrementar 30 minutos
            currentMinute += 30
            if (currentMinute >= 60) {
                currentMinute -= 60
                currentHour++
            }
        }

        return slots
    }

    private fun getDayOfWeekInSpanish(date: LocalDate): String {
        return when (date.dayOfWeek) {
            kotlinx.datetime.DayOfWeek.MONDAY -> "Lunes"
            kotlinx.datetime.DayOfWeek.TUESDAY -> "Martes"
            kotlinx.datetime.DayOfWeek.WEDNESDAY -> "Miércoles"
            kotlinx.datetime.DayOfWeek.THURSDAY -> "Jueves"
            kotlinx.datetime.DayOfWeek.FRIDAY -> "Viernes"
            kotlinx.datetime.DayOfWeek.SATURDAY -> "Sábado"
            kotlinx.datetime.DayOfWeek.SUNDAY -> "Domingo"
        }
    }

    fun scheduleAppointment(petId: Long, notes: String? = null) {
        val state = _uiState.value

        if (state.selectedDate == null || state.selectedTime == null || state.selectedDoctor == null) {
            _uiState.update { it.copy(errorMsg = "Completa todos los campos") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMsg = null) }

            val result = repository.createAppointment(
                userId = userId,
                petId = petId,
                doctorId = state.selectedDoctor.doctor.id,
                date = state.selectedDate.toString(),
                time = state.selectedTime,
                notes = notes
            )

            result.fold(
                onSuccess = { appointmentId ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            success = true,
                            createdAppointmentId = appointmentId
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMsg = "Error al agendar: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMsg = null) }
    }

    fun resetState() {
        _uiState.update { AppointmentUiState() }
        loadDoctors()
    }
}

// Estado de UI consolidado
data class AppointmentUiState(
    val selectedDate: LocalDate? = null,
    val selectedTime: String? = null,
    val selectedDoctor: DoctorWithSchedules? = null,
    val currentMonth: YearMonth = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()).date.toYearMonth(),
    val doctors: List<DoctorWithSchedules> = emptyList(),
    val availableTimes: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val success: Boolean = false,
    val errorMsg: String? = null,
    val createdAppointmentId: Long? = null,
    val canSubmit: Boolean = false
) {
    // Computed property
    val isReadyToSchedule: Boolean
        get() = selectedDate != null && selectedTime != null && selectedDoctor != null && !isSubmitting
}

// Extension para YearMonth
fun LocalDate.toYearMonth(): YearMonth = YearMonth(year, monthNumber)

data class YearMonth(val year: Int, val month: Int) {
    fun lengthOfMonth(): Int = LocalDate(year, month, 1)
        .plus(1, DateTimeUnit.MONTH)
        .minus(1, DateTimeUnit.DAY).dayOfMonth

    fun atDay(day: Int) = LocalDate(year, month, day)

    fun plusMonths(n: Int): YearMonth {
        val totalMonths = year * 12 + month - 1 + n
        return YearMonth(totalMonths / 12, totalMonths % 12 + 1)
    }

    fun minusMonths(n: Int) = plusMonths(-n)
}
