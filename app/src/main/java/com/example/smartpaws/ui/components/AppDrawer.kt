package com.example.smartpaws.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

// Pequeña data class para representar cada opción del drawer
data class DrawerItem( // Estructura de un ítem de menú lateral
    val label: String, // Texto a mostrar
    val icon: ImageVector, // Ícono del ítem
    val onClick: () -> Unit // Acción al hacer click
)

@Composable // Componente Drawer para usar en ModalNavigationDrawer
fun AppDrawer(
    currentRoute: String?, // Ruta actual (para marcar seleccionado si quieres)
    items: List<DrawerItem>, // Lista de ítems a mostrar
    modifier: Modifier = Modifier // Modificador opcional
) {
    ModalDrawerSheet( // Hoja que contiene el contenido del drawer
        modifier = modifier // Modificador encadenable
    ) {
        // Recorremos las opciones y pintamos ítems
        items.forEach { item -> // Por cada ítem
            NavigationDrawerItem( // Ítem con estados Material
                label = { Text(item.label) }, // Texto visible
                selected = false, // Puedes usar currentRoute == ... si quieres marcar
                onClick = item.onClick, // Acción al pulsar
                icon = { Icon(item.icon, contentDescription = item.label) }, // Ícono
                modifier = Modifier, // Sin mods extra
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = Color(0xFF4CA771) // tu color personalizado
                )
            ) // Estilo por defecto
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
    onLogout: () -> Unit
): List<DrawerItem> {
    // Usamos una lista mutable para construirla dinámicamente
    val items = mutableListOf(
        DrawerItem("Home", Icons.Filled.Home, onHome),
        DrawerItem("Perfil", Icons.Filled.AccountBox, onUser)
    )

    // --- LÓGICA CONDICIONAL ---
    if (isAdmin) {
        items.add(
            DrawerItem(
                "Panel Admin",
                Icons.Filled.AdminPanelSettings,
                onAdminPanel
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