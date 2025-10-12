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
import com.example.smartpaws.data.model.Mascota

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPetForm(
    onSubmit: (Mascota) -> Unit,
    modifier: Modifier = Modifier
) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var especie by rememberSaveable { mutableStateOf("Perro") }
    var especieExpanded by rememberSaveable { mutableStateOf(false) }
    var raza by rememberSaveable { mutableStateOf("") }
    var fechaNacimiento by rememberSaveable { mutableStateOf("") }
    var peso by rememberSaveable { mutableStateOf("") }
    var genero by rememberSaveable { mutableStateOf("M") }
    var generoExpanded by rememberSaveable { mutableStateOf(false) }
    var color by rememberSaveable { mutableStateOf("") }
    var chip by rememberSaveable { mutableStateOf("") }
    var notas by rememberSaveable { mutableStateOf("") }

    val especiesOptions = listOf("Perro", "Gato")
    val generoOptions = listOf("M", "F")

    Column(modifier = modifier.padding(16.dp)) {
        Text(text = "Agregar Nueva Mascota", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = especieExpanded,
            onExpandedChange = { especieExpanded = it }
        ) {
            OutlinedTextField(
                value = especie,
                onValueChange = {},
                readOnly = true,
                label = { Text("Especie") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = especieExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = especieExpanded,
                onDismissRequest = { especieExpanded = false }
            ) {
                especiesOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            especie = option
                            especieExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = raza,
            onValueChange = { raza = it },
            label = { Text("Raza") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = fechaNacimiento,
            onValueChange = { fechaNacimiento = it },
            label = { Text("Fecha Nacimiento (yyyy-mm-dd)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = peso,
            onValueChange = { peso = it },
            label = { Text("Peso (kg)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = generoExpanded,
            onExpandedChange = { generoExpanded = it }
        ) {
            OutlinedTextField(
                value = genero,
                onValueChange = {},
                readOnly = true,
                label = { Text("Género") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = generoExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = generoExpanded,
                onDismissRequest = { generoExpanded = false }
            ) {
                generoOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            genero = option
                            generoExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = color,
            onValueChange = { color = it },
            label = { Text("Color") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = chip,
            onValueChange = { chip = it },
            label = { Text("Chip") },
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
                val newPet = Mascota(
                    id = (0..1000).random(),
                    nombre = nombre,
                    especie = especie,
                    raza = raza,
                    fechaNacimiento = fechaNacimiento,
                    peso = peso.toFloatOrNull() ?: 0f,
                    genero = genero,
                    color = color,
                    chip = chip,
                    notas = notas,
                    estado = true,
                    idUsuario = 0
                )
                onSubmit(newPet)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar Mascota")
        }
    }
}