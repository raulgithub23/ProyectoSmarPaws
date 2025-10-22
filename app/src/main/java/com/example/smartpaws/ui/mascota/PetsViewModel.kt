package com.example.smartpaws.ui.mascota

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartpaws.data.local.pets.PetsEntity
import com.example.smartpaws.data.repository.PetsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PetsViewModel(
    private val repository: PetsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PetsUiState())
    val uiState: StateFlow<PetsUiState> = _uiState.asStateFlow()

    /*
    * INICIALIZACION DE DATOS DIRECTA DEL ROPOSITORY
    *  OCUPA LOS DATOS SEMILLA DEL DATABASE QUE SE DECLARARON
    *   CON EL ID DEL USUARIO SEMILLA DE PRUEBA
    * ============
    * EN CASO DE QUERER OCUPAR OTRO CAMBIAR EL ID O QUITAR EL INIT DE CASO CONTRARIO.
    * */

//    init {
//        loadPets(userId = 2L)
//    }

    fun onEvent(event: PetsEvent) {
        when(event) {
            is PetsEvent.AddNewPet -> addNewPet(event.pet)
            is PetsEvent.EditPetInformation -> editPetInformation(event.pet)
            is PetsEvent.RemovePetFromUser -> removePet(event.pet)
            is PetsEvent.LoadUserPets -> loadPets(event.userId)
        }
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

    private fun addNewPet(pet: PetsEntity) {
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

    private fun editPetInformation(pet: PetsEntity) {
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

    private fun removePet(pet: PetsEntity) {
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
//
//private fun getPetList() = listOf(
//    Mascota(
//        id = 1,
//        nombre = "Max",
//        especie = "Perro",
//        raza = "Labrador Retriever",
//        fechaNacimiento = "2021-05-10",
//        peso = 25.4f,
//        genero = "M",
//        color = "Dorado",
//        chip = "CHP-00123",
//        notas = "Muy amigable y activo",
//        estado = true,
//        idUsuario = 101
//    ),
//    Mascota(
//        id = 2,
//        nombre = "Luna",
//        especie = "Gato",
//        raza = "Persa",
//        fechaNacimiento = "2020-11-22",
//        peso = 4.8f,
//        genero = "F",
//        color = "Gris claro",
//        chip = "CHP-00456",
//        notas = "Le gusta dormir cerca de la ventana",
//        estado = true,
//        idUsuario = 101
//    ),
//    Mascota(
//        id = 3,
//        nombre = "Rocky",
//        especie = "Perro",
//        raza = "Bulldog Francés",
//        fechaNacimiento = "2022-08-03",
//        peso = 10.2f,
//        genero = "M",
//        color = "Negro con blanco",
//        chip = "CHP-00789",
//        notas = "Sensible al frío, requiere abrigo",
//        estado = true,
//        idUsuario = 101
//    )
//)
