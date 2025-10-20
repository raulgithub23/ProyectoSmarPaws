package com.example.smartpaws.ui.mascota

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartpaws.data.model.Mascota
import com.example.smartpaws.ui.theme.SMARTPAWSTheme

@Composable
fun PetCard(
    pet: Mascota,
//    onClick: () -> Unit
) {
    Card(
//        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        var expanded by rememberSaveable() { mutableStateOf(false) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.AccountBox,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier
                .weight(1f)) {
                Text(
                    text = pet.nombre,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${pet.especie} - ${pet.raza}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            IconButton(
                onClick = { expanded = !expanded }
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null
                )
            }
        }

        if (expanded) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            ) {
                Text(
                    text = "Fecha de nacimiento: ${pet.fechaNacimiento}\n" +
                            "Peso: ${pet.peso} kg\n" +
                            "Color: ${pet.color}\n" +
                            "Notas: ${pet.notas}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F0EE, widthDp = 320)
@Composable
fun PetCardPreview() {
    SMARTPAWSTheme(dynamicColor = false) {
        PetCard(
            pet = Mascota(
                id = 1,
                nombre = "Max",
                especie = "Perro",
                raza = "Labrador Retriever",
                fechaNacimiento = "2021-05-10",
                peso = 25.4f,
                genero = "M",
                color = "Dorado",
                chip = "CHP-00123",
                notas = "Muy amigable y activo",
                estado = true,
                idUsuario = 101
            )
        )
    }
}