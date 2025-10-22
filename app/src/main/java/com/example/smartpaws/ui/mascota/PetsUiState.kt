package com.example.smartpaws.ui.mascota

import com.example.smartpaws.data.local.pets.PetsEntity

data class PetsUiState(
    val isLoading: Boolean = false,
    val petsList: List<PetsEntity> = emptyList(),
    val error: String? = null
)
