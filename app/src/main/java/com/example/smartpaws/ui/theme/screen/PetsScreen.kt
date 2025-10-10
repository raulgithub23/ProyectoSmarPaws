package com.example.smartpaws.ui.theme.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetsScreen(
    //onGoHome: () -> Unit,
    //onGoLogin: () -> Unit,
    //onGoRegister: () -> Unit
) {
    // Lista de mascotas de ejemplo
    val pets = remember {
        listOf(
            "Max - Perro Golden Retriever",
            "Luna - Gato Persa",
            "Rocky - Perro Bulldog",
            "Mittens - Gato Maine Coon"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Título de la pantalla
        Text(
            text = "Mis Mascotas",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4CA771),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Botón para agregar nueva mascota
        ElevatedButton(
            onClick = { /* TODO: Implementar agregar mascota */ },
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = Color(0xFF4CA771)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Agregar",
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Agregar Nueva Mascota",
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }

        // Lista de mascotas
        if (pets.isNotEmpty()) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(pets) { pet ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF8F9FA)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AccountBox,
                                contentDescription = "Mascota",
                                tint = Color(0xFF4CA771),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = pet,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        } else {
            // Estado vacío
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountBox,
                        contentDescription = "Sin mascotas",
                        tint = Color.Gray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No tienes mascotas registradas",
                        fontSize = 18.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

