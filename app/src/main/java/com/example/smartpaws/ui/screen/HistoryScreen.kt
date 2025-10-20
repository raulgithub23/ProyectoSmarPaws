package com.example.smartpaws.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.smartpaws.R

//SCREEN DEL HISTORIAL DE LA MASCOTA
data class MascotaHistorial(
    val nombre: String,
    val fecha: String,
    val observacion: String
)
@Composable
fun HistoryScreen() {
    val bg = Color(0xFFEAF9E7)
    val cardColor = Color(0xFFC0E6BA)
    val textColor = Color(0xFF013237)

    // Lista de ejemplo
    val historialMascotas = listOf(
        MascotaHistorial("Luna", "12/10/2025", "Vacunación al día"),
        MascotaHistorial("Max", "02/09/2025", "Control general, sin novedades"),
        MascotaHistorial("Rocky", "20/08/2025", "Irritación ocular leve"),
        MascotaHistorial("Luna", "12/10/2025", "Diarrea continua"),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "HISTORIAL MASCOTA",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            // Aquí usamos forEach
            historialMascotas.forEach { mascota ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = cardColor),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {

                            Image(
                                painter = painterResource(R.drawable.catmeme),
                                contentDescription = "Imagen del logito",
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape) // Esto la hace redonda  a la imagen
                            )
                            Text(
                                text = mascota.nombre,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = textColor
                            )
                            Text(
                                text = "Fecha: ${mascota.fecha}\n${mascota.observacion}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = textColor
                            )
                        }

                        Button(
                            onClick = { /* acción: ver más */ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = textColor,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Ver más")
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun HistoryScreenPreview(){
    HistoryScreen()
}
