package com.example.smartpaws.ui.screen.screenprocesspayment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

//SCREEN PARA CREAR UN CITA PARA LA MASCOTA

@Composable // Pantalla Login (solo navegación, sin formularios)
fun AppointmentScreen(
) {
    var name by remember { mutableStateOf("") }
    val bg = Color(0xFFEAF9E7) // Fondo distinto para contraste

    Box(
        modifier = Modifier
            .fillMaxSize() // Ocupa todo
            .background(bg) // Fondo
            .padding(16.dp), // Margen
        contentAlignment = Alignment.Center // Centro
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally // Centrado horizontal
        ) {
            Text("Nombre de la mascota", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp)) // Separación
            //OutlinedTextField(value = nombre, ) PENDIENTE

            Text(
                text = "CITAS MASCOTA",
                style = MaterialTheme.typography.headlineSmall // Título
            )
            Spacer(Modifier.height(12.dp)) // Separación
            Text(
                text = "Acá ira todo lo de las citas formularios y demás.",
                textAlign = TextAlign.Center // Alineación centrada
            )
            Spacer(Modifier.height(20.dp)) // Separación

        }
    }
}
