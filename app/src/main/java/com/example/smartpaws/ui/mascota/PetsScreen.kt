package com.example.smartpaws.ui.mascota

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.smartpaws.data.remote.pets.PetsDto
import com.example.smartpaws.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetsScreen(
    petsViewModel: PetsViewModel,
    authViewModel: AuthViewModel,
) {
    // Observamos los estados
    val petsUiState by petsViewModel.uiState.collectAsStateWithLifecycle()
    val loginState by authViewModel.login.collectAsStateWithLifecycle()

    // Variables locales para el diálogo
    var showAddPetDialog by rememberSaveable { mutableStateOf(false) }
    var editingPet by rememberSaveable { mutableStateOf<PetsDto?>(null) }

    // Obtenemos el ID del usuario logeado
    val currentUserId = loginState.userId ?: 0L

    // UI principal
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header con botón de agregar
        PetsHeader(
            onAddPetClick = { showAddPetDialog = true }
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (petsUiState.error != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = petsUiState.error ?: "Error desconocido",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (petsUiState.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        if (petsUiState.petsList.isEmpty()) {
            if (!petsUiState.isLoading && petsUiState.error == null) {
                EmptyPetsState()
            }
        } else {
            PetsList(
                pets = petsUiState.petsList,
                onEditPet = { pet ->
                    editingPet = pet
                    showAddPetDialog = true
                },
                onDeletePet = { pet ->
                    petsViewModel.onEvent(PetsEvent.RemovePetFromUser(pet))
                }
            )
        }

        // Diálogo para agregar o editar mascota
        if (showAddPetDialog) {
            DialogAddPetForm(
                userId = editingPet?.userId ?: currentUserId,
                initialPet = editingPet,
                onDismiss = {
                    showAddPetDialog = false
                    editingPet = null
                },
                onSavePet = { pet ->
                    if (editingPet == null) {
                        petsViewModel.onEvent(PetsEvent.AddNewPet(pet))
                    } else {
                        petsViewModel.onEvent(PetsEvent.EditPetInformation(pet))
                    }
                    showAddPetDialog = false
                    editingPet = null
                }
            )
        }
    }
}