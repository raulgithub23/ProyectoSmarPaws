package com.example.smartpaws.ui.mascota

import com.example.smartpaws.data.model.Mascota

data class PetsUiState(
    val petsList: List<Mascota> = emptyList()
)
