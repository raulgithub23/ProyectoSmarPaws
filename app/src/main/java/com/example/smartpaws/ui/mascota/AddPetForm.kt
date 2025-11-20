package com.example.smartpaws.ui.mascota

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.smartpaws.data.remote.pets.PetsDto
import com.example.smartpaws.domain.validation.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPetForm(
    userId: Long,
    onSavePet: (PetsDto) -> Unit,
    onDismiss: () -> Unit,
    initialPet: PetsDto? = null,
    modifier: Modifier = Modifier
) {
    // --- ESTADOS ---
    var nombre by rememberSaveable { mutableStateOf(initialPet?.name ?: "") }
    var especie by rememberSaveable { mutableStateOf(initialPet?.especie ?: "Perro") }
    var fechaNacimiento by rememberSaveable { mutableStateOf(initialPet?.fechaNacimiento ?: "") }
    // Convertimos float a string, cuidando el null inicial
    var peso by rememberSaveable { mutableStateOf(initialPet?.peso?.let { it.toString() } ?: "") }
    var genero by rememberSaveable { mutableStateOf(initialPet?.genero ?: "M") }
    var color by rememberSaveable { mutableStateOf(initialPet?.color ?: "") }
    var notas by rememberSaveable { mutableStateOf(initialPet?.notas ?: "") }

    // --- ERRORES ---
    var nombreError by rememberSaveable { mutableStateOf<String?>(null) }
    var fechaError by rememberSaveable { mutableStateOf<String?>(null) }
    var pesoError by rememberSaveable { mutableStateOf<String?>(null) }
    var colorError by rememberSaveable { mutableStateOf<String?>(null) }
    var notasError by rememberSaveable { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = if (initialPet == null) "Agregar Nueva Mascota" else "Editar Mascota",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Nombre (Obligatorio)
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it; nombreError = null },
            label = { Text("Nombre *") },
            modifier = Modifier.fillMaxWidth(),
            isError = nombreError != null,
            supportingText = { nombreError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )

        Spacer(modifier = Modifier.height(8.dp))

        DropdownSelector(
            label = "Especie *",
            options = listOf("Perro", "Gato"),
            selectedOption = especie,
            onOptionSelected = { especie = it },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Fecha (Obligatorio)
        OutlinedTextField(
            value = fechaNacimiento,
            onValueChange = { fechaNacimiento = it; fechaError = null },
            label = { Text("Fecha Nacimiento (yyyy-mm-dd) *") },
            placeholder = { Text("Ej: 2024-01-30") },
            modifier = Modifier.fillMaxWidth(),
            isError = fechaError != null,
            supportingText = { fechaError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Peso (Obligatorio)
        OutlinedTextField(
            value = peso,
            onValueChange = { peso = it; pesoError = null },
            label = { Text("Peso (kg) *") },
            modifier = Modifier.fillMaxWidth(),
            isError = pesoError != null,
            supportingText = { pesoError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(8.dp))

        DropdownSelector(
            label = "Género",
            options = listOf("M", "F"),
            selectedOption = genero,
            onOptionSelected = { genero = it },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Color (Obligatorio)
        OutlinedTextField(
            value = color,
            onValueChange = { color = it; colorError = null },
            label = { Text("Color *") },
            modifier = Modifier.fillMaxWidth(),
            isError = colorError != null,
            supportingText = { colorError?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Notas (Opcional)
        OutlinedTextField(
            value = notas,
            onValueChange = { notas = it; notasError = null },
            label = { Text("Notas (Opcional)") },
            modifier = Modifier.fillMaxWidth(),
            isError = notasError != null,
            supportingText = { notasError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                nombreError = validatePetName(nombre)
                fechaError = validateDateFormat(fechaNacimiento)
                pesoError = validatePetWeight(peso)
                colorError = validatePetColor(color)
                notasError = validatePetNotes(notas)

                val hasError = listOf(nombreError, fechaError, pesoError, colorError, notasError)
                    .any { it != null }

                if (!hasError) {

                    val pesoString = peso.replace(',', '.').trim()
                    val pesoFinal = pesoString.toFloatOrNull()

                    val petToSave = PetsDto(
                        id = initialPet?.id,
                        userId = userId,
                        name = nombre.trim(),
                        especie = especie,
                        fechaNacimiento = fechaNacimiento.trim(),
                        peso = pesoFinal,
                        genero = genero,
                        color = color.trim(),
                        notas = notas.trim().ifBlank { null }
                    )
                    onSavePet(petToSave)
                    onDismiss()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (initialPet == null) "Guardar Mascota" else "Actualizar Mascota")
        }
    }
}