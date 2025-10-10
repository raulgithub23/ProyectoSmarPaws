package com.example.smartpaws.ui.theme.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    onOpenDrawer: () -> Unit, // Abre el drawer (SmartPaws)
    onHome: () -> Unit,       // Navega a Home
    onLogin: () -> Unit,      // Navega a Login
    onRegister: () -> Unit,    // Navega a Registro
    onUser: () -> Unit //NAVEGA HACIA EL PERFIL
) { var showMenu by remember { mutableStateOf(false) } // Estado del menú overflow

    CenterAlignedTopAppBar( // Barra alineada al centro
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color(0xFF4CA771) //Verde medio para la barra de arriba
        ),
        title = { // Slot del título
            Text(
                text = "SmartPaws", // Título visible
                style = MaterialTheme.typography.titleLarge, // Estilo grande
                maxLines = 1,              // asegura una sola línea Int.MAX_VALUE   // permite varias líneas
                overflow = TextOverflow.Ellipsis, // agrega "..." si no cabe
                color = Color(0xFFFFFFFF)

            )
        },
        navigationIcon = { // Ícono a la izquierda (hamburguesa)
            IconButton(onClick = onOpenDrawer) { // Al presionar, abre drawer
                Icon(imageVector = Icons.Filled.Menu, contentDescription = "Menú", tint = Color.White) // Ícono

            }
        },
        actions = { // Acciones a la derecha (solo menú overflow)
            IconButton(onClick = { showMenu = true }) { // Abre menú overflow
                Icon(Icons.Filled.MoreVert, contentDescription = "Más", tint = Color.White) // Ícono 3 puntitos
            }
            DropdownMenu(
                expanded = showMenu, // Si está abierto
                onDismissRequest = { showMenu = false } // Cierra al tocar fuera
            ) {
                DropdownMenuItem( // Opción Home
                    text = { Text("Home") }, // Texto opción
                    onClick = { showMenu = false; onHome() } // Navega y cierra
                )
                DropdownMenuItem( // Opción Login
                    text = { Text("Login") },
                    onClick = { showMenu = false; onLogin() }
                )
                DropdownMenuItem( // Opción Registro
                    text = { Text("Registro") },
                    onClick = { showMenu = false; onRegister() }
                )

                DropdownMenuItem(
                    text = { Text("Perfil") },
                    onClick = { showMenu = false; onUser() }
                )
            }
        }
    )
}