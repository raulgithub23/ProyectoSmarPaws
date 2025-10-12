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
import com.example.smartpaws.ui.theme.SMARTPAWSTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetsScreen(
    //onGoHome: () -> Unit,
    //onGoLogin: () -> Unit,
    //onGoRegister: () -> Unit
    viewModel: PetsViewModel
) {
    //ESTADO DE UI DE MASCOTAS
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddPetDialog by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        PetsHeader(
            onAddPetClick = { showAddPetDialog = true }
        )

        if (uiState.petsList.isNotEmpty()) {
            PetsList(
                pets = uiState.petsList
            )
        } else {
            EmptyPetsState()
        }

        if (showAddPetDialog) {
            DialogAddPetForm(
                onDismiss = { showAddPetDialog = false },
                onSubmit = { pet ->
                    viewModel.onEvent(PetsEvent.AddNewPet(pet))
                    showAddPetDialog = false
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
