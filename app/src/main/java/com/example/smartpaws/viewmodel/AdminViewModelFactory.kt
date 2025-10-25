package com.example.smartpaws.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.smartpaws.data.repository.DoctorRepository
import com.example.smartpaws.data.repository.UserRepository

class AdminViewModelFactory(
    private val repository: UserRepository,
    private val doctorRepository: DoctorRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminViewModel(userRepository = repository,
                                doctorRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}