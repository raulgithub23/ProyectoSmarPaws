package com.example.smartpaws.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartpaws.data.remote.appointments.AppointmentResponseDto
import com.example.smartpaws.data.remote.dto.DoctorDto
import com.example.smartpaws.data.remote.pets.PetsDto
import com.example.smartpaws.data.repository.AppointmentRepository
import com.example.smartpaws.data.repository.DoctorRepository
import com.example.smartpaws.ui.mascota.PetsViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.*

//ACÁ SE MANEJA TODA LA LOGICA DE LA VISTA DE CITAS DE MASCOTAS
@RequiresApi(Build.VERSION_CODES.O)
class AppointmentViewModel(
    private val repository: AppointmentRepository,
    private val doctorRepository: DoctorRepository,
    private val petsViewModel: PetsViewModel,
    private val userId: Long?
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppointmentUiState())
    val uiState: StateFlow<AppointmentUiState> = _uiState

    init {
        loadDoctors()
        observeUserPets()
        observeUserAppointments()
    }

    private fun observeUserPets() {
        viewModelScope.launch {
            petsViewModel.uiState.collect { petsState ->
                _uiState.update { it.copy(userPets = petsState.petsList) }
            }
        }
    }

    private fun observeUserAppointments() {
        viewModelScope.launch {
            if (userId != null) {
                val result = repository.getUpcomingAppointmentsByUser(userId)
                result.onSuccess { appointments ->
                    _uiState.update {
                        it.copy(scheduledAppointments = appointments)
                    }
                }
            }
        }
    }

    private fun loadDoctors() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Ahora devuelve List<DoctorDto>, compatible con el State actualizado
                val doctors = doctorRepository.getAllDoctorsWithSchedules()
                _uiState.update {
                    it.copy(
                        doctors = doctors,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMsg = "Error al cargar doctores: ${e.message}"
                    )
                }
            }
        }
    }

    fun selectPet(pet: PetsDto) {
        _uiState.update { it.copy(selectedPet = pet) }
    }

    fun selectDate(date: LocalDate) {
        _uiState.update {
            it.copy(
                selectedDate = date,
                selectedTime = null,
                availableTimes = emptyList()
            )
        }
        _uiState.value.selectedDoctor?.let { loadAvailableTimesForDoctor(date, it) }
    }

    fun selectTime(time: String) {
        _uiState.update { it.copy(selectedTime = time) }
    }

    // CAMBIO: Recibe DoctorDto en lugar de DoctorWithSchedules
    fun selectDoctor(doctor: DoctorDto) {
        _uiState.update {
            it.copy(
                selectedDoctor = doctor,
                selectedTime = null,
                availableTimes = emptyList()
            )
        }
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

        if (newMonth.year > currentYearMonth.year ||
            (newMonth.year == currentYearMonth.year && newMonth.month >= currentYearMonth.month)) {
            _uiState.update { it.copy(currentMonth = newMonth) }
        }
    }

    // CAMBIO: Recibe DoctorDto
    private fun loadAvailableTimesForDoctor(date: LocalDate, doctor: DoctorDto) {
        val dayOfWeek = getDayOfWeekInSpanish(date)
        // DoctorDto tiene 'schedules' que es List<ScheduleDto>
        val schedule = doctor.schedules.find { it.dayOfWeek == dayOfWeek }

        if (schedule == null) {
            _uiState.update { it.copy(availableTimes = emptyList()) }
            return
        }

        viewModelScope.launch {
            val allSlots = generateTimeSlots(schedule.startTime, schedule.endTime, date)

            // CAMBIO: Usamos doctor.id directamente (ya no doctor.doctor.id)
            val result = repository.getAppointmentsByDoctorAndDate(doctor.id, date.toString())

            val occupiedTimes = try {
                result.getOrDefault(emptyList()).map { it.time }
            } catch (e: Exception) {
                emptyList<String>()
            }

            val availableSlots = allSlots.filter { it !in occupiedTimes }

            _uiState.update { it.copy(availableTimes = availableSlots) }
        }
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
            if (date == today) {
                if (currentHour > now.hour || (currentHour == now.hour && currentMinute > now.minute)) {
                    slots.add(String.format("%02d:%02d", currentHour, currentMinute))
                }
            } else {
                slots.add(String.format("%02d:%02d", currentHour, currentMinute))
            }

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
            DayOfWeek.MONDAY -> "Lunes"
            DayOfWeek.TUESDAY -> "Martes"
            DayOfWeek.WEDNESDAY -> "Miércoles"
            DayOfWeek.THURSDAY -> "Jueves"
            DayOfWeek.FRIDAY -> "Viernes"
            DayOfWeek.SATURDAY -> "Sábado"
            DayOfWeek.SUNDAY -> "Domingo"
            else -> ""
        }
    }

    fun scheduleAppointment(notes: String? = null) {
        val state = _uiState.value

        if (state.selectedPet == null) {
            _uiState.update { it.copy(errorMsg = "Selecciona una mascota") }
            return
        }

        if (state.selectedDate == null || state.selectedTime == null || state.selectedDoctor == null) {
            _uiState.update { it.copy(errorMsg = "Completa todos los campos") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMsg = null) }

            // CAMBIO: doctor.id
            val checkResult = repository.getAppointmentsByDoctorAndDate(
                state.selectedDoctor.id,
                state.selectedDate.toString()
            )

            val isTimeTaken = checkResult.fold(
                onSuccess = { existing -> existing.any { it.time == state.selectedTime } },
                onFailure = { false }
            )

            if (isTimeTaken) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMsg = "Lo sentimos, este horario acaba de ser ocupado."
                    )
                }
                loadAvailableTimesForDoctor(state.selectedDate, state.selectedDoctor)
                return@launch
            }

            val result = repository.createAppointment(
                userId = state.selectedPet.userId,
                petId = state.selectedPet.id,
                doctorId = state.selectedDoctor.id, // CAMBIO: doctor.id
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
        _uiState.update {
            AppointmentUiState(
                userPets = it.userPets,
                doctors = it.doctors
            )
        }
    }

    fun acknowledgeSuccess() {
        _uiState.update { it.copy(success = false) }
    }

    fun deleteAppointment(appointmentId: Long) {
        viewModelScope.launch {
            val result = repository.deleteAppointmentById(appointmentId)
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        scheduledAppointments = it.scheduledAppointments.filterNot { a ->
                            a.id == appointmentId
                        }
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(errorMsg = "Error al eliminar cita: ${e.message}")
                }
            }
        }
    }
}

// --- CLASES DE ESTADO ACTUALIZADAS ---

data class AppointmentUiState(
    val scheduledAppointments: List<AppointmentResponseDto> = emptyList(),
    val userPets: List<PetsDto> = emptyList(),
    val selectedPet: PetsDto? = null,
    val selectedDate: LocalDate? = null,
    val selectedTime: String? = null,
    val selectedDoctor: DoctorDto? = null, // CAMBIO: DoctorDto
    val currentMonth: YearMonth = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()).date.toYearMonth(),
    val doctors: List<DoctorDto> = emptyList(), // CAMBIO: List<DoctorDto>
    val availableTimes: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val success: Boolean = false,
    val errorMsg: String? = null,
    val createdAppointmentId: Long? = null
) {
    val isReadyToSchedule: Boolean
        get() = selectedPet != null &&
                selectedDate != null &&
                selectedTime != null &&
                selectedDoctor != null &&
                !isSubmitting

    val hasNoPets: Boolean
        get() = userPets.isEmpty()
}

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