package com.example.smartpaws.ui.mascota

import com.example.smartpaws.data.remote.pets.PetsDto

data class PetsUiState(
    val isLoading: Boolean = false,
    val petsList: List<PetsDto> = emptyList(),
    val error: String? = null
)
