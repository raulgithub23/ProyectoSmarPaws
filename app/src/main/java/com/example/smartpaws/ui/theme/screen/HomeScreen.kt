package com.example.smartpaws.ui.theme.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp


//ACÁ VA ESTAR LOS PRINCIPAL DE SMARTPAWS
@Composable // Pantalla Home (sin formularios, solo navegación/diseño)
fun HomeScreen(
    onGoLogin: () -> Unit,   // Acción a Login
    onGoRegister: () -> Unit // Acción a Registro
) {
    val bg = Color(0xFFC0E6BA) // Fondo agradable para Home
    val card = Color(0xFFEAF9E7) // COLOR DE CARTAS 0xFFEAF9E7
    val text = Color(0xFF013237) //COLO PARA TEXTO

    Box( // Contenedor a pantalla completa
        modifier = Modifier
            .fillMaxSize() // Ocupa todo
            .background(bg) // Aplica fondo
            .padding(16.dp), // Margen interior
        contentAlignment = Alignment.Center // Centra contenido

    ) {
        Column( // Estructura vertical
            horizontalAlignment = Alignment.CenterHorizontally // Centra hijos
        ) {
            // Cabecera como Row (ejemplo de estructura)
            Row(
                verticalAlignment = Alignment.CenterVertically // Centra vertical
            ) {
                Text( // Título Home
                    text = "Inicio",
                    style = MaterialTheme.typography.headlineSmall, // Estilo título
                    fontWeight = FontWeight.SemiBold // Seminegrita
                )
                Spacer(Modifier.width(8.dp)) // Separación horizontal
                AssistChip( // Chip decorativo (Material 3)
                    onClick = {}, // Sin acción (demo)
                    label = { Text("Estoy probando") } // Texto chip
                )
                Spacer(Modifier.width(8.dp)) // Separación horizontal
                AssistChip( // Chip decorativo (Material 3)
                    onClick = {}, // Sin acción (demo)
                    label = { Text("Quiero otro") } // Texto chip
                )
            }

            Spacer(Modifier.height(200.dp)) // Separación

            // Tarjeta con un mini “hero”
            ElevatedCard( // Card elevada para remarcar contenido
                modifier = Modifier.fillMaxWidth(), // Ancho completo
                colors = CardDefaults.elevatedCardColors(
                    containerColor = Color(0xFFEAF9E7), // Fondo de la card
                    contentColor = Color.White          // Color del texto por defecto
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp), // Margen interno de la card
                    horizontalAlignment = Alignment.CenterHorizontally, // Centrado
                ) {
                    Text(
                        "Demostración de con TopBar + Drawer + Botones",
                        style = MaterialTheme.typography.titleMedium, // Estilo medio
                        textAlign = TextAlign.Center, // Alineación centrada
                        color = Color(0xFF013237)
                    )
                    Spacer(Modifier.height(12.dp)) // Separación
                    Text(
                        "Usa la barra superior (íconos y menú), el menú lateral o estos botones.",
                        style = MaterialTheme.typography.bodyMedium, // Texto base
                        color = Color(0xFF013237)
                    )
                }
            }

            Spacer(Modifier.height(24.dp)) // Separación

            // Botones de navegación principales
            Row( // Dos botones en fila
                horizontalArrangement = Arrangement.spacedBy(12.dp) // Espacio entre botones
            ) {
                Button(
                    onClick = onGoLogin,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF013237),
                        contentColor = Color.White
                    )
                ) {
                    Text("Ir a Login")
                } // Navega a Login
                OutlinedButton(onClick = onGoRegister,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEAF9E7),
                        contentColor = Color(0xFF013237))
                ) { Text("Ir a Registro") } // A Registro
            }
        }
    }
}