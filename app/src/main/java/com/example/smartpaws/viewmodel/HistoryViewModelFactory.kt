package com.example.smartpaws.viewmodel

import HistoryViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.smartpaws.data.repository.AppointmentRepository

class HistoryViewModelFactory(
    private val repository: AppointmentRepository,
    private val authViewModel: AuthViewModel //para obtener la id dle user
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            return HistoryViewModel(repository, authViewModel) as T //y aca tabien
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}