package com.example.smartpaws.ui.mascota

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartpaws.data.local.pets.PetsEntity
import com.example.smartpaws.ui.theme.SMARTPAWSTheme
import com.example.smartpaws.viewmodel.AuthViewModel
import com.example.smartpaws.viewmodel.LoginUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetsScreen(
    viewModel: PetsViewModel,
) {
    // Estado de UI
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddPetDialog by rememberSaveable { mutableStateOf(false) }
    var editingPet by rememberSaveable { mutableStateOf<PetsEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header con botón de agregar
        PetsHeader(
            onAddPetClick = { showAddPetDialog = true }
        )

        // Lista de mascotas
        if (uiState.petsList.isNotEmpty()) {
            PetsList(
                pets = uiState.petsList,
                onEditPet = { pet ->
                    editingPet = pet
                    showAddPetDialog = true
                },
                onDeletePet = { pet ->
                    viewModel.onEvent(PetsEvent.RemovePetFromUser(pet))
                },
            )
        } else {
            EmptyPetsState()
        }

        // Diálogo para agregar o editar mascota
        if (showAddPetDialog) {
            DialogAddPetForm(
                userId = editingPet?.userId ?: 1L,
                initialPet = editingPet,
                onDismiss = {
                    showAddPetDialog = false
                    editingPet = null
                },
                onSavePet = { pet ->
                    if (editingPet == null) {
                        viewModel.onEvent(PetsEvent.AddNewPet(pet))
                    } else {
                        viewModel.onEvent(PetsEvent.EditPetInformation(pet))
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F0EE, widthDp = 320)
@Composable
fun PetsScreenPreview() {
    SMARTPAWSTheme(dynamicColor = false) {
        PetsScreen(
            viewModel = viewModel()
        )
    }
}
