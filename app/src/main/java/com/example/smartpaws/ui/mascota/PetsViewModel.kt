package com.example.smartpaws.ui.mascota

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartpaws.data.local.pets.PetsEntity
import com.example.smartpaws.data.remote.pets.PetsDto
import com.example.smartpaws.data.repository.PetsRepository
import com.example.smartpaws.viewmodel.AuthViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PetsViewModel(
    private val repository: PetsRepository,
    private val authViewModel: AuthViewModel
) : ViewModel() {

    private val _uiState = MutableStateFlow(PetsUiState())
    val uiState: StateFlow<PetsUiState> = _uiState.asStateFlow()

    /*
    * INICIALIZACION DE DATOS DIRECTA DEL ROPOSITORY
    *  OCUPA LOS DATOS SEMILLA DEL DATABASE QUE SE DECLARARON
    *   CON EL ID DEL USUARIO SEMILLA DE PRUEBA
    * ============
    */

    private var observePetsJob: Job? = null

    init {
        viewModelScope.launch {
            authViewModel.login.collect { loginState ->
                if (loginState.success && loginState.userId != null) {
                    // Reinicia la observación para el nuevo usuario
                    startObservingPets(loginState.userId)
                } else if (!loginState.success && loginState.userId == null) {
                    // Limpia datos al desloguear
                    clearPets()
                }
            }
        }
    }

    fun onEvent(event: PetsEvent) {
        when(event) {
            is PetsEvent.AddNewPet -> addNewPet(event.pet)
            is PetsEvent.EditPetInformation -> editPetInformation(event.pet)
            is PetsEvent.RemovePetFromUser -> removePet(event.pet)
            is PetsEvent.LoadUserPets -> loadPets(event.userId)
        }
    }

    private fun startObservingPets(userId: Long) {
        // Cancela observaciones anteriores (si había otra sesión)
        observePetsJob?.cancel()

        // Limpia el estado anterior antes de cargar el nuevo usuario
        _uiState.update { it.copy(petsList = emptyList(), isLoading = true, error = null) }

        // suscripción al Flow del usuario actual
        observePetsJob = viewModelScope.launch {
            repository.observePetsByUser(userId).collect { result ->
                result.onSuccess { pets ->
                    _uiState.update {
                        it.copy(petsList = pets, isLoading = false, error = null)
                    }
                }.onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message ?: "Error al cargar mascotas")
                    }
                }
            }
        }
    }

    private fun clearPets() {
        observePetsJob?.cancel()
        _uiState.value = PetsUiState() // Limpia completamente
    }

    /*
    *   FUNCION PARA CARGAR LA LISTA DE MASCOTAS ASOCIADAS
    *   AL ID DEL USUARIO LOGEADO
    * */
    private fun loadPets(userId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = repository.getPetsByUser(userId)

            result.onSuccess { pets ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        petsList = pets,
                        error = null
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error al cargar mascotas"
                    )
                }
            }
        }
    }

    private fun addNewPet(pet: PetsDto) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = repository.insertPet(pet)

            result.onSuccess {
                loadPets(pet.userId)
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error al agregar mascota"
                    )
                }
            }
        }
    }

    private fun editPetInformation(pet: PetsDto) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = repository.updatePet(pet)

            result.onSuccess {
                loadPets(pet.userId)
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error al editar mascota"
                    )
                }
            }
        }
    }

    private fun removePet(pet: PetsDto) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = repository.deletePet(pet)

            result.onSuccess {
                loadPets(pet.userId)
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error al eliminar mascota"
                    )
                }
            }
        }
    }

}