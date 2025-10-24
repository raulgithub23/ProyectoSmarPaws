package com.example.smartpaws.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartpaws.viewmodel.AdminViewModel

@Composable
fun AdminPanelScreen(viewModel: AdminViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    // UI básica de ejemplo
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Panel de Administración", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar Estadísticas
        Text("Total Usuarios: ${uiState.stats.totalUsers}")
        Text("Admins: ${uiState.stats.adminCount}")
        Text("Doctores: ${uiState.stats.doctorCount}")
        Text("Usuarios: ${uiState.stats.userCount}")

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoading) {
            CircularProgressIndicator()
        }

        uiState.errorMsg?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        // Lista de usuarios (simplificada)
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(uiState.users) { user ->
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(user.name, fontWeight = FontWeight.Bold)
                    Text("Email: ${user.email}")
                    Text("Rol: ${user.rol}")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}