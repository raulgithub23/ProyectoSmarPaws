package com.example.smartpaws.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.smartpaws.navigation.Route


// COMPOSABLE PARA MOSTRAR CUANDO ESTE EN MODO EXTENDED LA PANTALLA
// HACE QUE LA BARRA DE BOTTOMBAR ESTA A LA IZQUIERDA AL DETECTAR UN ANCHO MAYOR
// A COMPACT
@Composable
fun AppNavigationRail(
    currentRoute: String?,
    onHome: () -> Unit,
    onAppointment: () -> Unit,
    onPets: () -> Unit,
    onHistory: () -> Unit
) {
    NavigationRail {
        NavigationRailItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
            label = { Text("Inicio") },
            selected = currentRoute == Route.Home.path,
            onClick = onHome
        )
        NavigationRailItem(
            icon = { Icon(Icons.Default.Pets, contentDescription = "Mascotas") },
            label = { Text("Mascotas") },
            selected = currentRoute == Route.Pets.path,
            onClick = onPets
        )
        NavigationRailItem(
            icon = { Icon(Icons.Default.DateRange, contentDescription = "Agendar") },
            label = { Text("Agendar") },
            selected = currentRoute == Route.Appointment.path,
            onClick = onAppointment
        )
        NavigationRailItem(
            icon = { Icon(Icons.AutoMirrored.Filled.ListAlt, contentDescription = "Historial") },
            label = { Text("Historial") },
            selected = currentRoute == Route.History.path,
            onClick = onHistory
        )
    }
}