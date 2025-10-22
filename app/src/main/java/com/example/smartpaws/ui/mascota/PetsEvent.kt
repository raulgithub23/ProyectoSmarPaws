package com.example.smartpaws.ui.mascota

import com.example.smartpaws.data.local.pets.PetsEntity

sealed class PetsEvent {
    data class AddNewPet(val pet: PetsEntity) : PetsEvent()
    data class EditPetInformation(val pet: PetsEntity) : PetsEvent()
    data class RemovePetFromUser(val pet: PetsEntity) : PetsEvent()
    data class LoadUserPets(val userId: Long) : PetsEvent()
}