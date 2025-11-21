package com.example.smartpaws.ui.screen
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.smartpaws.R
import com.example.smartpaws.data.local.pets.PetFactEntity
import com.example.smartpaws.ui.theme.DarkGreen
import com.example.smartpaws.ui.theme.LightBackground
import com.example.smartpaws.ui.theme.LightSecondary
import com.example.smartpaws.viewmodel.HomeAppointmentUiItem
import com.example.smartpaws.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel, //parametro que se conecta con viewmodel
    onNavigateToAppointments: () -> Unit = {} //lo usamos para el boton agendar citas
) {
    val homeState by viewModel.homeState.collectAsState() //variable que nos trae los datos del viewmodel a medida que cambian

    val bg = LightSecondary
    val cardColor = LightBackground
    val textColor = DarkGreen

    //contenedor principal que  nos va ocupar toda la pantalla
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título de bienvenida
            item {
                Text(
                    text = "¡Bienvenido a SmartPaws!",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                homeState.currentFact?.let { fact -> //es la variable que contieen el dato curioso
                    PetFactCard(
                        fact = fact,
                        cardColor = cardColor,
                        textColor = textColor,
                        onRefresh = { viewModel.refreshFact() }// Callback para refrescar el dato
                    )
                }
            }

            // Sección de próximas citas
            if (homeState.upcomingAppointments.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Próximas Citas",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                items(homeState.upcomingAppointments) { appointment -> //se itera sobre la lista de citas y la añade a la carta
                    AppointmentCard(
                        appointment = appointment,
                        cardColor = cardColor,
                        textColor = textColor
                    )
                }
            } else {
                item {
                    // Si no hay citas, muestra una tarjeta invitando a crear una
                    NoAppointmentsCard(
                        cardColor = cardColor,
                        textColor = textColor,
                        onCreateAppointment = onNavigateToAppointments
                    )
                }
            }
        }

        // Indicador de carga
        if (homeState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Mensaje de error
        homeState.errorMsg?.let { error ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("OK")
                    }
                }
            ) {
                Text(error)
            }
        }
    }
}

// Tarjeta de dato curioso con botón refrescar
@Composable
fun PetFactCard(
    fact: PetFactEntity, // Entidad o tabla que contiene el dato curioso
    cardColor: Color,
    textColor: Color,
    onRefresh: () -> Unit //funsion que se ejecuta al presionar refrescar
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = cardColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource(
                    if (fact.type == "cat") R.drawable.gatofrentev1
                    else R.drawable.perrofrentev1  // Cambia esto cuando tengas la imagen del perro
                ),
                contentDescription = "Mascota",
                modifier = Modifier.size(100.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = fact.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = fact.fact,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )

                // Boton refrescar a la derecha
                TextButton(
                    onClick = onRefresh,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refrescar",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Otro dato", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

// Tarjeta de cita (anuncio)
@Composable
fun AppointmentCard(
    appointment: HomeAppointmentUiItem,
    cardColor: Color,
    textColor: Color
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = cardColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icono de alerta/recordatorio
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = textColor.copy(alpha = 0.1f),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EventNote,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = appointment.petName,
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${appointment.date} - ${appointment.time}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = appointment.doctorName,
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor.copy(alpha = 0.8f)
                    )
                }

                if (!appointment.notes.isNullOrBlank()) {
                    Text(
                        text = appointment.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor.copy(alpha = 0.6f),
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }
    }
}

// Tarjeta cuando no hay citas
@Composable
fun NoAppointmentsCard(
    cardColor: Color,
    textColor: Color,
    onCreateAppointment: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = cardColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.EventAvailable,
                contentDescription = null,
                tint = textColor.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "No tienes citas pendientes",
                style = MaterialTheme.typography.titleMedium,
                color = textColor,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "¡Agenda una cita para el cuidado de tu mascota!",
                style = MaterialTheme.typography.bodyMedium,
                color = textColor.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Button(
                onClick = onCreateAppointment,
                colors = ButtonDefaults.buttonColors(
                    containerColor = textColor
                )
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Agendar Cita")
            }
        }
    }
}