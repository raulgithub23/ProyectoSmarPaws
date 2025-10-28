package com.example.smartpaws.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.smartpaws.data.repository.AppointmentRepository
import com.example.smartpaws.data.repository.DoctorRepository
import com.example.smartpaws.data.repository.PetsRepository
import com.example.smartpaws.ui.mascota.PetsViewModel

//Los ViewModels normalmente se crean automáticamente por Android sin parámetros entonces esta
// el factory le dice a Android CoMO crear el ViewModel con esos parametros
class AppointmentViewModelFactory(
    // Dependencias que necesita el AppointmentViewModel para funcionar
    private val appointmentRepository: AppointmentRepository, // Para operaciones de citas en BD
    private val doctorRepository: DoctorRepository, // Para obtener doctores
    private val petsViewModel: PetsViewModel, // Para observar las mascotas del usuario
    private val userId: Long? // ID del usuario actual (nullable por si no hay sesión)
) : ViewModelProvider.Factory { // Hereda de Factory, la interfaz que Android usa para crear ViewModels

    @RequiresApi(Build.VERSION_CODES.O)
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppointmentViewModel::class.java)) { // Verifica si la clase solicitada es AppointmentViewModel
            return AppointmentViewModel(             // Si es el ViewModel correcto, lo crea con todas sus dependencias
                repository = appointmentRepository,
                doctorRepository = doctorRepository,
                petsViewModel =  petsViewModel,
                userId = userId
            ) as T // Cast: convierte AppointmentViewModel al tipo genérico T que Android espera
        }
        throw IllegalArgumentException("Unknown ViewModel class")         // Si se solicitó un ViewModel diferente, lanza una excepción
                                                                            // Esto nunca debería pasar si usamos la factory correctamente
    }
}