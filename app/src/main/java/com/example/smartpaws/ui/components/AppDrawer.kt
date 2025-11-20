package com.example.smartpaws.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

data class DrawerItem(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun AppDrawer(
    currentRoute: String?,
    items: List<DrawerItem>,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier
    ) {
        items.forEach { item ->
            NavigationDrawerItem(
                label = { Text(item.label) },
                selected = false,
                onClick = item.onClick,
                icon = { Icon(item.icon, contentDescription = item.label) },
                modifier = Modifier,
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
fun defaultDrawerItems(
    onHome: () -> Unit,
    onUser: () -> Unit,
    isAdmin: Boolean,
    onAdminPanel: () -> Unit,
    isDoctor: Boolean = false,
    onDoctorAppointments: () -> Unit = {},
    onLogout: () -> Unit
): List<DrawerItem> {
    val items = mutableListOf<DrawerItem>()

    // DOCTOR (pero NO admin): Ver sus citas
    if (isDoctor && !isAdmin) {
        items.add(
            DrawerItem(
                "Mis Citas",
                Icons.Filled.DateRange,
                onDoctorAppointments
            )
        )
    }

    // ADMIN: Panel de administración
    if (isAdmin) {
        items.add(
            DrawerItem(
                "Panel de Administración",
                Icons.Filled.AdminPanelSettings,
                onAdminPanel
            )
        )
    }

    // Usuarios normales (o admin como comodín): Inicio
    if (!isDoctor || isAdmin) {
        items.add(
            DrawerItem(
                "Inicio",
                Icons.Filled.Home,
                onHome
            )
        )
    }

    // Todos: Perfil
    items.add(
        DrawerItem(
            "Perfil",
            Icons.Filled.AccountBox,
            onUser
        )
    )

    // Todos: Cerrar Sesión
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