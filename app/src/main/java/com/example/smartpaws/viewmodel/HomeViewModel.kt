package com.example.smartpaws.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartpaws.data.local.appointment.AppointmentWithDetails
import com.example.smartpaws.data.local.pets.PetFactDao
import com.example.smartpaws.data.local.pets.PetFactEntity
import com.example.smartpaws.data.repository.AppointmentRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


// Estado de la pantalla Home
data class HomeUiState(
    val currentFact: PetFactEntity? = null, // Dato curioso actual (null si aun no se carga)
    val upcomingAppointments: List<AppointmentWithDetails> = emptyList(), // Próximas citas del usuario
    val isLoading: Boolean = false, // Indica si está cargando datos
    val errorMsg: String? = null // Mensaje de error (null si no hay error)
)
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class) // Necesario para usar flatMapLatest
class HomeViewModel(
    private val repository: AppointmentRepository, // Repositorio para operaciones de citas
    private val petFactDao: PetFactDao, // DAO para acceder a datos curiosos en la BD
    private val authViewModel: AuthViewModel // se recibe el authViewModel opara sacar la id del ususraio

) : ViewModel() {

    private val _homeState = MutableStateFlow(HomeUiState()) // Estado privado mutable (solo este ViewModel puede modificarlo)
    val homeState: StateFlow<HomeUiState> = _homeState // Estado público inmutable (la UI solo puede leerlo)

    init { // Bloque init: se ejecuta automáticamente al crear el ViewModel
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch { // Activa el estado de carga
            _homeState.update { it.copy(isLoading = true, errorMsg = null) }

            try {
                // Cargar datos curiosos
                launch {
                    petFactDao.getAllFacts().collect { allFacts -> // collect: escucha continuamente cambios en la tabla de datos curiosos
                        val randomFact = allFacts.randomOrNull() // Selecciona un dato aleatorio de la lista
                        _homeState.update { it.copy(currentFact = randomFact) }
                    }
                }

                // Cargar citas OBSERVANDO el userId
                launch {
                    authViewModel.login
                        .distinctUntilChangedBy { it.userId } // Solo reacciona cuando cambia el userId
                        .flatMapLatest { loginState ->
                            if (loginState.userId != null) {
                                // Observa las citas en tiempo real para este usuario
                                repository.getUpcomingAppointmentsByUser(loginState.userId)
                            } else {
                                // Si no hay usuario logueado, emite lista vacía
                                flowOf(emptyList())
                            }
                        }
                        .collect { appointments -> // collect: recibe cada emisión del Flow y actualiza el estado
                            _homeState.update {
                                it.copy(
                                    upcomingAppointments = appointments,
                                    isLoading = false,
                                    errorMsg = null
                                )
                            }
                        }
                }
            } catch (e: Exception) {  // Si cualquiera de las corrutinas falla, captura el error
                _homeState.update {
                    it.copy(
                        isLoading = false,
                        errorMsg = "Error al cargar datos: ${e.message}"
                    )
                }
            }
        }
    }

    fun refreshFact() {
        viewModelScope.launch {
            try {
                petFactDao.getAllFacts().first().let { allFacts -> // first(): Obtiene SOLO la primera emisión del Flow y luego se desconecta
                                                                     // Es diferente a collect() que se queda escuchando continuamente
                    val newFact = allFacts.randomOrNull()  // Selecciona un nuevo dato aleatorio
                    Log.d("HomeViewModel", "Refrescando dato: ${newFact?.title}")
                    _homeState.update { it.copy(currentFact = newFact) }
                }
            } catch (e: Exception) {
                // Si falla al refrescar, registra el error y lo muestra al usuario
                Log.e("HomeViewModel", "Error al refrescar: ${e.message}", e)
                _homeState.update {
                    it.copy(errorMsg = "Error al refrescar: ${e.message}")
                }
            }
        }
    }

    fun clearError() {
        _homeState.update { it.copy(errorMsg = null) }
    }
}