package com.example.smartpaws.ui.mascota

import com.example.smartpaws.data.model.Mascota

sealed class PetsEvent {
    data class EditPetInformation(val pet: Mascota) : PetsEvent()
    data class RemovePetFromUser(val petId: Int) : PetsEvent()

    data class AddNewPet(val newPet: Mascota) : PetsEvent()
}