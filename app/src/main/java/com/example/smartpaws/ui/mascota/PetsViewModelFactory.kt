package com.example.smartpaws.ui.mascota

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.smartpaws.data.repository.PetsRepository

class PetsViewModelFactory(
    private val repository: PetsRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PetsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PetsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}