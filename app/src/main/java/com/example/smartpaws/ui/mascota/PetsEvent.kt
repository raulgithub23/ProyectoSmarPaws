package com.example.smartpaws.ui.mascota

import com.example.smartpaws.data.remote.pets.PetsDto

sealed class PetsEvent {
    data class AddNewPet(val pet: PetsDto) : PetsEvent()
    data class EditPetInformation(val pet: PetsDto) : PetsEvent()
    data class RemovePetFromUser(val pet: PetsDto) : PetsEvent()
    data class LoadUserPets(val userId: Long) : PetsEvent()
}