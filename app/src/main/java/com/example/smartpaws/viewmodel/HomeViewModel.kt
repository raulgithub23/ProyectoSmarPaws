package com.example.smartpaws.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartpaws.data.remote.pets.PetFact
import com.example.smartpaws.data.repository.AppointmentRepository
import com.example.smartpaws.data.repository.DoctorRepository
import com.example.smartpaws.data.repository.PetsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeAppointmentUiItem(
    val id: Long,
    val date: String,
    val time: String,
    val notes: String?,
    val petName: String,
    val doctorName: String,
    val doctorSpecialty: String?
)

// Estado de la pantalla Home
data class HomeUiState(
    val currentFact: PetFact? = null, // Usamos la nueva clase simple PetFact
    val upcomingAppointments: List<HomeAppointmentUiItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMsg: String? = null
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val repository: AppointmentRepository,
    // private val petFactDao: PetFactDao, // ELIMINADO: Ya no existe DAO
    private val authViewModel: AuthViewModel,
    private val petsRepository: PetsRepository,
    private val doctorRepository: DoctorRepository
) : ViewModel() {

    private val _homeState = MutableStateFlow(HomeUiState())
    val homeState: StateFlow<HomeUiState> = _homeState

    // Lista "quemada" (Hardcoded) de datos curiosos para reemplazar la BD local temporalmente
    private val localFacts = listOf(
        PetFact("Datos sobre gatos", "Los gatos duermen entre 13 y 16 horas al día.", "cat"),
        PetFact("Datos sobre gatos", "Los gatos tienen 32 músculos en cada oreja.", "cat"),
        PetFact("Datos sobre gatos", "El ronroneo de un gato puede reducir el estrés.", "cat"),
        PetFact("Datos sobre perros", "El olfato de un perro es 10,000 veces más fuerte que el humano.", "dog"),
        PetFact("Datos sobre perros", "La nariz de cada perro es única, como una huella dactilar.", "dog"),
        PetFact("Datos sobre perros", "Los perros sudan a través de sus patas.", "dog")
    )

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            _homeState.update { it.copy(isLoading = true, errorMsg = null) }

            // 1. Cargar Dato Curioso (Simulado localmente)
            refreshFact()

            // 2. Cargar citas OBSERVANDO el userId del AuthViewModel
            authViewModel.login
                .distinctUntilChangedBy { it.userId }
                .collectLatest { loginState ->
                    if (loginState.userId != null) {
                        loadAppointmentsForUser(loginState.userId)
                    } else {
                        _homeState.update { it.copy(upcomingAppointments = emptyList(), isLoading = false) }
                    }
                }
        }
    }

    private suspend fun loadAppointmentsForUser(userId: Long) {
        try {
            val result = repository.getUpcomingAppointmentsByUser(userId)

            result.fold(
                onSuccess = { dtos ->
                    // Mapeo de IDs a Nombres
                    // Nota: Esto hace muchas llamadas de red en bucle (N+1 problem).
                    // Idealmente el backend debería devolver el nombre del doctor y la mascota en la cita.
                    // Pero por ahora lo mantenemos así para que funcione con tu lógica actual.
                    val uiItems = dtos.map { dto ->
                        // Obtener nombre de mascota
                        val petName = if (dto.petId != null) {
                            petsRepository.getPetById(dto.petId).getOrNull()?.name ?: "Desconocido"
                        } else "Sin mascota"

                        // Obtener datos del doctor
                        // CORRECCIÓN: getDoctorWithSchedules devuelve un Result<DoctorDto>
                        val doctorDto = doctorRepository.getDoctorWithSchedules(dto.doctorId).getOrNull()

                        HomeAppointmentUiItem(
                            id = dto.id,
                            date = dto.date,
                            time = dto.time,
                            notes = dto.notes,
                            petName = petName,
                            // CORRECCIÓN: Accedemos directo a las propiedades del DTO, sin .doctor intermedio
                            doctorName = doctorDto?.name ?: "Dr. Desconocido",
                            doctorSpecialty = doctorDto?.specialty
                        )
                    }

                    _homeState.update {
                        it.copy(
                            upcomingAppointments = uiItems,
                            isLoading = false,
                            errorMsg = null
                        )
                    }
                },
                onFailure = { e ->
                    _homeState.update {
                        it.copy(isLoading = false, errorMsg = "Error cargando citas: ${e.message}")
                    }
                }
            )
        } catch (e: Exception) {
            _homeState.update {
                it.copy(isLoading = false, errorMsg = "Error inesperado: ${e.message}")
            }
        }
    }

    fun refreshFact() {
        // Lógica simple: sacar uno al azar de la lista local
        val randomFact = localFacts.random()
        _homeState.update { it.copy(currentFact = randomFact) }
        Log.d("HomeViewModel", "Dato refrescado: ${randomFact.fact}")
    }

    fun clearError() {
        _homeState.update { it.copy(errorMsg = null) }
    }
}