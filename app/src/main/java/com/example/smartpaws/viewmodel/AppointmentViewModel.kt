package com.example.smartpaws.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartpaws.data.local.appointment.AppointmentWithDetails
import com.example.smartpaws.data.local.doctors.DoctorWithSchedules
import com.example.smartpaws.data.local.pets.PetsEntity
import com.example.smartpaws.data.repository.AppointmentRepository
import com.example.smartpaws.data.repository.DoctorRepository
import com.example.smartpaws.ui.mascota.PetsViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlinx.datetime.DayOfWeek.*

//ACÁ SE MANEJA TODA LA LOGICA DE LA VISTA DE CITAS DE MASCOTAS
@RequiresApi(Build.VERSION_CODES.O)
class AppointmentViewModel(
    private val repository: AppointmentRepository, // Repositorio para operaciones CRUD de citas nos sirve de mucho
    private val doctorRepository: DoctorRepository, // Repositorio para obtener doctores
    private val petsViewModel: PetsViewModel, // ViewModel de mascotas para obtener las mascotas del usuario
    private val userId: Long? // ID del usuario actual (nullable por seguridad)
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppointmentUiState())     // Estado privado mutable que solo el ViewModel puede modificar
    val uiState: StateFlow<AppointmentUiState> = _uiState // Estado publico inmutable que la UI (Pantalla de citas) puede observar

    // Bloque init: se ejecuta al crear el ViewModel
    init {
        loadDoctors()
        observeUserPets()
        observeUserAppointments()
    }

    private fun observeUserPets() {         // Observa el estado de mascotas del PetsViewModel
        viewModelScope.launch {
            petsViewModel.uiState.collect { petsState ->
                _uiState.update { it.copy(userPets = petsState.petsList) }
            }
        }
    }

    private fun observeUserAppointments() {  // Observa y actualiza las citas próximas del usuario en tiempo real
        viewModelScope.launch {
            // Observa las citas del usuario
            if (userId != null) {
                repository.getUpcomingAppointmentsByUser(userId).collect { appointments ->
                    _uiState.update {
                        it.copy(scheduledAppointments = appointments)
                    }
                }
            }
        }
    }

    private fun loadDoctors() { // Carga la lista de doctores con sus horarios disponibles desde la BD
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val doctors = doctorRepository.getAllDoctorsWithSchedules()
                _uiState.update {
                    it.copy(
                        doctors = doctors,
                        isLoading = false
                    )
                }
            } catch (e: Exception) { // Si hay error, desactiva carga y muestra mensaje de error
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMsg = "Error al cargar doctores: ${e.message}"
                    )
                }
            }
        }
    }

    fun selectPet(pet: PetsEntity) { // Actualiza la mascota seleccionada por el usuario
        _uiState.update { it.copy(selectedPet = pet) }
    }

    fun selectDate(date: LocalDate) { // Actualiza la fecha seleccionada y reinicia hora y horarios disponibles
        _uiState.update {
            it.copy(
                selectedDate = date,
                selectedTime = null,
                availableTimes = emptyList()
            )
        }
        _uiState.value.selectedDoctor?.let { loadAvailableTimesForDoctor(date, it) }  // Si ya hay un doctor seleccionado, carga los horarios para esa fecha
    }

    fun selectTime(time: String) { // Actualiza la hora seleccionada por el usuario
        _uiState.update { it.copy(selectedTime = time) }
    }

    fun selectDoctor(doctor: DoctorWithSchedules) { // Actualiza el doctor seleccionado y recarga horarios si ya hay fecha
        _uiState.update {
            it.copy(
                selectedDoctor = doctor,
                selectedTime = null,
                availableTimes = emptyList()
            )
        }
        _uiState.value.selectedDate?.let { loadAvailableTimesForDoctor(it, doctor) }
    }

    fun nextMonth() { // Avanza al siguiente mes en el calendario
        _uiState.update {
            it.copy(currentMonth = it.currentMonth.plusMonths(1))
        }
    }

    fun previousMonth() {     // Retrocede al mes anterior, pero no permite ir a meses pasados
        val newMonth = _uiState.value.currentMonth.minusMonths(1)
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val currentYearMonth = today.toYearMonth()

        if (newMonth.year > currentYearMonth.year ||
            (newMonth.year == currentYearMonth.year && newMonth.month >= currentYearMonth.month)) {
            _uiState.update { it.copy(currentMonth = newMonth) }
        }
    }

    private fun loadAvailableTimesForDoctor(date: LocalDate, doctor: DoctorWithSchedules) {     // Carga los horarios disponibles de un doctor específico para una fecha dada
        val dayOfWeek = getDayOfWeekInSpanish(date)
        val schedule = doctor.schedules.find { it.dayOfWeek == dayOfWeek }

        if (schedule == null) {
            _uiState.update { it.copy(availableTimes = emptyList()) }
            return
        }

        val times = generateTimeSlots(schedule.startTime, schedule.endTime, date)
        _uiState.update { it.copy(availableTimes = times) }
    }

    // Genera slots de tiempo cada 30 minutos entre hora de inicio y fin
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

    // Convierte el día de la semana de LocalDate a String en español
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

    // Función principal para agendar una cita con todas las validaciones

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

            val result = repository.createAppointment(
                userId = state.selectedPet.userId,
                petId = state.selectedPet.id,
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
            try {
                repository.deleteAppointmentById(appointmentId)
                _uiState.update {
                    it.copy(
                        scheduledAppointments = it.scheduledAppointments.filterNot { a ->
                            a.id == appointmentId
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMsg = "Error al eliminar cita: ${e.message}")
                }
            }
        }
    }

}
/* Data class que representa estado de la ui appointments*/

data class AppointmentUiState(
    val scheduledAppointments : List<AppointmentWithDetails> = emptyList(),
    val userPets: List<PetsEntity> = emptyList(),
    val selectedPet: PetsEntity? = null,
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

// Función de extensión para convertir LocalDate a YearMonth personalizado

fun LocalDate.toYearMonth(): YearMonth = YearMonth(year, monthNumber)


// Data class personalizada para manejar año y mes (similar a java.time.YearMonth)

data class YearMonth(val year: Int, val month: Int) {
    fun lengthOfMonth(): Int = LocalDate(year, month, 1)
        .plus(1, DateTimeUnit.MONTH)
        .minus(1, DateTimeUnit.DAY).dayOfMonth

    fun atDay(day: Int) = LocalDate(year, month, day)

    fun plusMonths(n: Int): YearMonth {
        val totalMonths = year * 12 + month - 1 + n
        return YearMonth(totalMonths / 12, totalMonths % 12 + 1)
    }

    // Resta n meses (reutiliza plusMonths con valor negativo)

    fun minusMonths(n: Int) = plusMonths(-n)
}