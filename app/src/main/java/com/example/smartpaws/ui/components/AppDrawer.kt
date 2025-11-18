package com.example.smartpaws.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.DateRange // Import necesario para el icono de citas
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class DrawerItem(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit // Acción al hacer click
)

@Composable
fun AppDrawer(
    currentRoute: String?, // Ruta actual (para marcar seleccionado)
    items: List<DrawerItem>,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier
    ) {
        items.forEach { item ->
            NavigationDrawerItem(
                label = { Text(item.label) },
                selected = false, // SE PUEDE PASAR CURRENTRUTE
                onClick = item.onClick,
                icon = { Icon(item.icon, contentDescription = item.label) },
                modifier = Modifier,
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary// 0xFF4CA771
                )
            )
        }
    }
}

// Helper para construir la lista estándar de ítems del drawer
@Composable
fun defaultDrawerItems(
    onHome: () -> Unit,   // Acción Home
//    onLogin: () -> Unit,  // Acción Login
//    onRegister: () -> Unit, // Acción Registro
    onUser: () -> Unit, //ACCION DEL PERFIL
    isAdmin: Boolean,
    onAdminPanel: () -> Unit,
    isDoctor: Boolean = false, // NUEVO: Saber si es doctor
    onDoctorAppointments: () -> Unit = {}, // NUEVO: Acción para ir a citas agendadas
    onLogout: () -> Unit
): List<DrawerItem> {
    // Usamos una lista mutable para construirla dinámicamente
    val items = mutableListOf(
        DrawerItem("Home", Icons.Filled.Home, onHome),
        DrawerItem("Perfil", Icons.Filled.AccountBox, onUser)
    )

    // LÓGICA CONDICIONAL PARA MOSTRAR LA OPCION DE ADMIN
    if (isAdmin) {
        items.add(
            DrawerItem(
                "Panel de Administración ",
                Icons.Filled.AdminPanelSettings,
                onAdminPanel
            )
        )
    }

    // LÓGICA CONDICIONAL PARA MOSTRAR LA OPCION DE DOCTOR
    if (isDoctor) {
        items.add(
            DrawerItem(
                "Citas Agendadas",
                Icons.Filled.DateRange,
                onDoctorAppointments
            )
        )
    }

    items.add(
        DrawerItem(
            "Cerrar Sesión",
            Icons.Filled.ExitToApp,
            onLogout
        )
    )

    return items.toList()
}

//= listOf(
//DrawerItem("Home", Icons.Filled.Home, onHome),          // Ítem Home
////    DrawerItem("Login", Icons.Filled.AccountCircle, onLogin),       // Ítem Login
////    DrawerItem("Registro", Icons.Filled.Person, onRegister), // Ítem Registro
//DrawerItem("Perfil", Icons.Filled.AccountBox, onUser)
//)