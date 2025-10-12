package com.example.smartpaws.ui.mascota

import androidx.lifecycle.ViewModel
import com.example.smartpaws.data.model.Mascota
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PetsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PetsUiState())
    val uiState: StateFlow<PetsUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(petsList = getPetList()) }
    }

    fun onEvent(event: PetsEvent) {
        when(event) {
            is PetsEvent.EditPetInformation -> editPetInformation(event.pet)
            is PetsEvent.RemovePetFromUser -> removePet(event.petId)
            is PetsEvent.AddNewPet -> addNewPet(event.newPet)
        }
    }

    private fun addNewPet(newPet: Mascota) {
        _uiState.update {
            it.copy(
                petsList = (it.petsList + newPet)
            )
        }
    }

    private fun editPetInformation(pet: Mascota) {
        _uiState.update {
            it.copy(
                petsList = it.petsList.map { element ->
                    if (element.id == pet.id) element.copy(nombre = pet.nombre)
                    else element
                }
            )
        }
    }

    private fun removePet(petId: Int) {
        _uiState.update {
            it.copy(
                petsList = it.petsList.filter { pet ->
                    pet.id != petId
                }
            )
        }
    }

}

private fun getPetList() = listOf(
    Mascota(
        id = 1,
        nombre = "Max",
        especie = "Perro",
        raza = "Labrador Retriever",
        fechaNacimiento = "2021-05-10",
        peso = 25.4f,
        genero = "M",
        color = "Dorado",
        chip = "CHP-00123",
        notas = "Muy amigable y activo",
        estado = true,
        idUsuario = 101
    ),
    Mascota(
        id = 2,
        nombre = "Luna",
        especie = "Gato",
        raza = "Persa",
        fechaNacimiento = "2020-11-22",
        peso = 4.8f,
        genero = "F",
        color = "Gris claro",
        chip = "CHP-00456",
        notas = "Le gusta dormir cerca de la ventana",
        estado = true,
        idUsuario = 101
    ),
    Mascota(
        id = 3,
        nombre = "Rocky",
        especie = "Perro",
        raza = "Bulldog Francés",
        fechaNacimiento = "2022-08-03",
        peso = 10.2f,
        genero = "M",
        color = "Negro con blanco",
        chip = "CHP-00789",
        notas = "Sensible al frío, requiere abrigo",
        estado = true,
        idUsuario = 101
    )
)
