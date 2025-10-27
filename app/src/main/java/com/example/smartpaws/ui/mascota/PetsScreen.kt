package com.example.smartpaws.ui.mascota

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
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
    petsViewModel: PetsViewModel,
    authViewModel: AuthViewModel,
) {
    // Observamos los estados
    val petsUiState by petsViewModel.uiState.collectAsStateWithLifecycle()
    val loginState by authViewModel.login.collectAsStateWithLifecycle()

    // Variables locales para el diálogo
    var showAddPetDialog by rememberSaveable { mutableStateOf(false) }
    var editingPet by rememberSaveable { mutableStateOf<PetsEntity?>(null) }

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

        // Contenido principal
        when {
            petsUiState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            petsUiState.error != null -> {
                Text(
                    text = petsUiState.error ?: "Error desconocido",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            petsUiState.petsList.isEmpty() -> {
                EmptyPetsState()
            }

            else -> {
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
        }

        // Diálogo para agregar o editar mascota
        if (showAddPetDialog) {
            DialogAddPetForm(
                userId = editingPet?.userId ?: currentUserId, // userId correcto según login
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


//@Preview(showBackground = true, backgroundColor = 0xFFF5F0EE, widthDp = 320)
//@Composable
//fun PetsScreenPreview() {
//    SMARTPAWSTheme(dynamicColor = false) {
//        PetsScreen(
//            petsViewModel = TODO(),
//            authViewModel = TODO(),
//        )
//    }
//}
