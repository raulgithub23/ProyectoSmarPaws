package com.example.smartpaws.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.smartpaws.data.repository.AppointmentRepository
import com.example.smartpaws.data.repository.DoctorRepository
import com.example.smartpaws.data.repository.PetsRepository

class HomeViewModelFactory(     // Dependencias que necesita el HomeViewModel para funcionar
    private val repository: AppointmentRepository, // Acceso a citas en la base de datos
//    private val petFactDao: PetFactDao, // Acceso a datos curiosos en la base de datos
    private val authViewModel: AuthViewModel,  // Se Agrega para traer la id del user despues de loguearse
    private val petsRepository: PetsRepository,
    private val doctorRepository: DoctorRepository,
) : ViewModelProvider.Factory { // Hereda de Factory para que Android pueda usarla
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {         // Verifica que la clase solicitada sea HomeViewModel o una subclase
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {  // Crea el HomeViewModel pasándole todas las dependencias que necesita
            return HomeViewModel(
                repository,
//                petFactDao,
                authViewModel,
                petsRepository = petsRepository,
                doctorRepository = doctorRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}") // Si se pidió un ViewModel diferente, lanza error con el nombre de la clase
                                                                                            // Esto ayuda a depurar si se usa la factory incorrecta
    }
}

