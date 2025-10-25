package com.example.smartpaws.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.smartpaws.data.local.pets.PetFactDao
import com.example.smartpaws.data.repository.AppointmentRepository

class HomeViewModelFactory(
    private val repository: AppointmentRepository,
    private val petFactDao: PetFactDao,
    private val authViewModel: AuthViewModel  // Se Agrega para traer la id del user despues de loguearse
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(repository, petFactDao, authViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

