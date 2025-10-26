package com.example.smartpaws.ui.mascota

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smartpaws.data.local.pets.PetsEntity
import com.example.smartpaws.domain.validation.validateBirthDate
import com.example.smartpaws.domain.validation.validatePetName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPetForm(
    userId: Long,
    onSavePet: (PetsEntity) -> Unit,
    onDismiss: () -> Unit,
    initialPet: PetsEntity? = null,
    modifier: Modifier = Modifier
) {
    var nombre by rememberSaveable { mutableStateOf(initialPet?.name ?: "") }
    var especie by rememberSaveable { mutableStateOf(initialPet?.especie ?: "Perro") }
    var fechaNacimiento by rememberSaveable { mutableStateOf(initialPet?.fechaNacimiento ?: "") }
    var peso by rememberSaveable { mutableStateOf(initialPet?.peso?.toString() ?: "") }
    var genero by rememberSaveable { mutableStateOf(initialPet?.genero ?: "M") }
    var color by rememberSaveable { mutableStateOf(initialPet?.color ?: "") }
    var notas by rememberSaveable { mutableStateOf(initialPet?.notas ?: "") }

    var nombreError by rememberSaveable { mutableStateOf<String?>(null) }
    var fechaNacimientoError by rememberSaveable { mutableStateOf<String?>(null) }


    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = if (initialPet == null) "Agregar Nueva Mascota" else "Editar Mascota",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = {
                nombre = it
                nombreError = null
            },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth(),
            isError = nombreError != null,
            supportingText = {
                if (nombreError != null) {
                    Text(text = nombreError!!, color = MaterialTheme.colorScheme.error)
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        DropdownSelector(
            label = "Especie",
            options = listOf("Perro", "Gato"),
            selectedOption = especie,
            onOptionSelected = { especie = it },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = fechaNacimiento,
            onValueChange = {
                fechaNacimiento = it
                fechaNacimientoError = null
            },
            label = { Text("Fecha Nacimiento (yyyy-mm-dd)") },
            modifier = Modifier.fillMaxWidth(),
            isError = fechaNacimientoError != null,
            supportingText = {
                if (fechaNacimientoError != null) {
                    Text(text = fechaNacimientoError!!, color = MaterialTheme.colorScheme.error)
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = peso,
            onValueChange = { peso = it },
            label = { Text("Peso (kg)") },
            modifier = Modifier.fillMaxWidth()
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

        OutlinedTextField(
            value = color,
            onValueChange = { color = it },
            label = { Text("Color") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = notas,
            onValueChange = { notas = it },
            label = { Text("Notas") },
            modifier = Modifier.fillMaxWidth()
        )


        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                nombreError = validatePetName(nombre)
                fechaNacimientoError = validateBirthDate(fechaNacimiento)

                val isValid = nombreError == null && fechaNacimientoError == null

                if (isValid) {
                    val petToSave = PetsEntity(
                        id = initialPet?.id ?: 0L,
                        userId = userId,
                        name = nombre,
                        especie = especie,
                        fechaNacimiento = fechaNacimiento,
                        peso = peso.toFloatOrNull(),
                        genero = genero,
                        color = color.ifBlank { null },
                        notas = notas.ifBlank { null }
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
