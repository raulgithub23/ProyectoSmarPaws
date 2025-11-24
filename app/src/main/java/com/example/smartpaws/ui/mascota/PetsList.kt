package com.example.smartpaws.ui.mascota

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.smartpaws.data.remote.pets.PetsDto

@Composable
fun PetsList(
    pets: List<PetsDto>,
    onEditPet: (PetsDto) -> Unit,
    onDeletePet: (PetsDto) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = pets,
            key = { it.id!! }
        ) { pet ->
            PetCard(
                pet = pet,
                onEdit = { onEditPet(pet) },
                onDelete = { onDeletePet(pet) }
            )
        }
    }
}
