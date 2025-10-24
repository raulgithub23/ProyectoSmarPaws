package com.example.smartpaws.ui.screen

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
import com.example.smartpaws.viewmodel.HistoryViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.smartpaws.data.local.appointment.AppointmentWithDetails

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel
) {
    val bg = Color(0xFFEAF9E7)
    val cardColor = Color(0xFFC0E6BA)
    val textColor = Color(0xFF013237)

    val state by viewModel.historyState.collectAsState()

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

            if (state.appointments.isEmpty()) {
                Text(
                    text = "No hay citas registradas",
                    color = textColor
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.appointments) { appointment ->
                        HistoryCard(
                            appointment = appointment,
                            cardColor = cardColor,
                            textColor = textColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryCard(
    appointment: AppointmentWithDetails,
    cardColor: Color,
    textColor: Color
) {
    var expanded by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = cardColor),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                            .clip(CircleShape)
                    )
                    Text(
                        text = appointment.petName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor
                    )
                    Text(
                        text = "Fecha: ${appointment.date}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor
                    )
                }

                Button(
                    onClick = { expanded = !expanded },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = textColor,
                        contentColor = Color.White
                    )
                ) {
                    Text(if (expanded) "Ver menos" else "Ver más")
                }
            }

            // Información expandida
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = textColor.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Doctor: ${appointment.doctorName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )
                Text(
                    text = "Hora: ${appointment.time}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )
                Text(
                    text = "Observaciones: ${appointment.notes ?: "Sin observaciones"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )
            }
        }
    }
}

//@Composable
//@Preview(showBackground = true)
//fun HistoryCardPreview() {
//    val fakeAppointment = AppointmentWithDetails(
//        id = 1,
//        userId = 1,
//        petId = 1,
//        doctorId = 1,
//        date = "2025-10-22",
//        time = "10:30",
//        notes = "Vacunación anual",
//        petName = "Luna",
//        doctorName = "Dra. María González"
//    )
//
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .background(Color(0xFFEAF9E7))
//            .padding(16.dp)
//    ) {
//        HistoryCard(
//            appointment = fakeAppointment,
//            cardColor = Color(0xFFC0E6BA),
//            textColor = Color(0xFF013237)
//        )
//    }
//}

