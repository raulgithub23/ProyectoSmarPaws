package com.example.smartpaws.ui.screen.screenprocesspayment

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartpaws.data.local.appointment.AppointmentWithDetails
import com.example.smartpaws.data.local.doctors.DoctorWithSchedules
import com.example.smartpaws.data.local.pets.PetsEntity
import com.example.smartpaws.viewmodel.AppointmentViewModel
import com.example.smartpaws.viewmodel.YearMonth
import kotlinx.datetime.*


//SCREEN PARA CREAR UN CITA PARA LA MASCOTA
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppointmentScreen(
    viewModel: AppointmentViewModel,
    onAppointmentCreated: () -> Unit = {}
) {
    val green = Color(0xFF00C853)
    val lightGreen = Color(0xFFE8F5E9)

    val uiState by viewModel.uiState.collectAsState()
    var showSuccessDialog by remember { mutableStateOf(false) }

    var appointmentToDelete by remember { mutableStateOf<AppointmentWithDetails?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }


    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            showSuccessDialog = true
        }
    }

    // Diálogo de éxito
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            icon = {
                Text("✅", fontSize = 48.sp)
            },
            title = {
                Text(
                    text = "¡Cita Agendada!",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Tu cita ha sido agendada exitosamente para:",
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "${uiState.selectedPet?.name}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = green
                    )
                    Text(
                        text = "${uiState.selectedDate?.dayOfMonth}/${uiState.selectedDate?.monthNumber}/${uiState.selectedDate?.year}",
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "a las ${uiState.selectedTime}",
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "con ${uiState.selectedDoctor?.doctor?.name}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        viewModel.acknowledgeSuccess()
                        onAppointmentCreated()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = green)
                ) {
                    Text("Ver mis citas")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        viewModel.acknowledgeSuccess()
                        viewModel.resetState()
                    }
                ) {
                    Text("Agendar otra", color = green)
                }
            }
        )
    }

    uiState.errorMsg?.let { error ->
        LaunchedEffect(error) {
            viewModel.clearError()
        }
    }

    // Si el usuario no tiene mascotas, mostrar mensaje
    if (uiState.hasNoPets) {
        NoPetsWarning(primaryColor = green)
        return
    }

    if (showDeleteDialog && appointmentToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar cita") },
            text = { Text("¿Estás seguro que deseas eliminar esta cita? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAppointment(appointmentToDelete!!.id)
                        showDeleteDialog = false
                        appointmentToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Eliminar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    appointmentToDelete = null
                }) {
                    Text("Cancelar")
                }
            }
        )
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "Agendar Cita",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1B5E20),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // SECCIÓN: Mis Próximas Citas
        if (uiState.scheduledAppointments.isNotEmpty()) {
            Text(
                text = "Mis Próximas Citas",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1B5E20),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            ScheduledAppointmentsList(
                appointments = uiState.scheduledAppointments,
                primaryColor = green,
                lightColor = lightGreen,
                onDeleteClick = { appointment ->
                    appointmentToDelete = appointment
                    showDeleteDialog = true}
            )

            Spacer(Modifier.height(24.dp))

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f))
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Agendar Nueva Cita",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1B5E20),
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // PASO 1: Selector de Mascota
        Text(
            text = "1. Selecciona la mascota",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1B5E20),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        PetSelector(
            pets = uiState.userPets,
            selectedPet = uiState.selectedPet,
            onPetSelected = { viewModel.selectPet(it) },
            primaryColor = green,
            lightColor = lightGreen
        )

        Spacer(Modifier.height(32.dp))

        // PASO 2: Selector de Doctor
        Text(
            text = "2. Selecciona un doctor",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1B5E20),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = green)
            }
        } else {
            DoctorSelector(
                doctors = uiState.doctors,
                selectedDoctor = uiState.selectedDoctor,
                onDoctorSelected = { viewModel.selectDoctor(it) },
                primaryColor = green,
                lightColor = lightGreen,
                enabled = uiState.selectedPet != null
            )
        }

        Spacer(Modifier.height(32.dp))

        // PASO 3: Selector de Fecha
        Text(
            text = "3. Selecciona una fecha",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1B5E20),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.previousMonth() },
                enabled = uiState.selectedPet != null && uiState.selectedDoctor != null
            ) {
                Icon(
                    Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Mes anterior",
                    tint = if (uiState.selectedPet != null && uiState.selectedDoctor != null) green else Color.LightGray
                )
            }

            Text(
                text = "${uiState.currentMonth.monthName()} ${uiState.currentMonth.year}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF1B5E20)
            )

            IconButton(
                onClick = { viewModel.nextMonth() },
                enabled = uiState.selectedPet != null && uiState.selectedDoctor != null
            ) {
                Icon(
                    Icons.Default.KeyboardArrowRight,
                    contentDescription = "Mes siguiente",
                    tint = if (uiState.selectedPet != null && uiState.selectedDoctor != null) green else Color.LightGray
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        CalendarView(
            yearMonth = uiState.currentMonth,
            selectedDate = uiState.selectedDate,
            onDateSelected = { viewModel.selectDate(it) },
            primaryColor = green,
            lightColor = lightGreen,
            enabled = uiState.selectedPet != null && uiState.selectedDoctor != null
        )

        Spacer(Modifier.height(32.dp))

        // PASO 4: Selector de Horario
        Text(
            text = "4. Selecciona un horario",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1B5E20),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (uiState.selectedDate != null && uiState.selectedDoctor != null && uiState.selectedPet != null) {
            if (uiState.availableTimes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(Color(0xFFFFF3E0), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⚠️", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "El doctor no atiende este día",
                            color = Color(0xFFE65100),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp, max = 400.dp)
                ) {
                    items(uiState.availableTimes) { time ->
                        TimeSlotButton(
                            time = time,
                            isSelected = time == uiState.selectedTime,
                            onClick = { viewModel.selectTime(time) },
                            color = green
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color(0xFFF8F8F8), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🕒", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Primero completa los pasos anteriores",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(Modifier.height(30.dp))

        // Botón de agendar
        Button(
            onClick = { viewModel.scheduleAppointment() },
            enabled = uiState.isReadyToSchedule,
            colors = ButtonDefaults.buttonColors(
                containerColor = green,
                disabledContainerColor = Color.Gray
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            if (uiState.isSubmitting) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = if (uiState.isReadyToSchedule) {
                        "Agendar para ${uiState.selectedPet?.name} - ${uiState.selectedDate?.dayOfMonth}/${uiState.selectedDate?.monthNumber} a las ${uiState.selectedTime}"
                    } else {
                        "Completa todos los pasos"
                    },
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun NoPetsWarning(primaryColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🐾", fontSize = 80.sp)

        Spacer(Modifier.height(24.dp))

        Text(
            text = "No tienes mascotas registradas",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1B5E20),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Para agendar una cita, primero debes registrar una mascota en la sección de Mascotas",
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PetSelector(
    pets: List<PetsEntity>,
    selectedPet: PetsEntity?,
    onPetSelected: (PetsEntity) -> Unit,
    primaryColor: Color,
    lightColor: Color
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(pets) { pet ->
            PetCard(
                pet = pet,
                isSelected = pet.id == selectedPet?.id,
                onClick = { onPetSelected(pet) },
                primaryColor = primaryColor,
                lightColor = lightColor
            )
        }
    }
}

@Composable
fun PetCard(
    pet: PetsEntity,
    isSelected: Boolean,
    onClick: () -> Unit,
    primaryColor: Color,
    lightColor: Color
) {
    val borderColor = if (isSelected) primaryColor else Color.LightGray
    val backgroundColor = if (isSelected) lightColor else Color.White

    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(2.dp, borderColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(primaryColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (pet.especie.lowercase()) {
                        "perro" -> "🐶"
                        "gato" -> "🐱"
                        else -> "🐾"
                    },
                    fontSize = 32.sp
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = pet.name,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.Black,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = pet.especie,
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            if (isSelected) {
                Spacer(Modifier.height(4.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Seleccionado",
                    tint = primaryColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun DoctorSelector(
    doctors: List<DoctorWithSchedules>,
    selectedDoctor: DoctorWithSchedules?,
    onDoctorSelected: (DoctorWithSchedules) -> Unit,
    primaryColor: Color,
    lightColor: Color,
    enabled: Boolean = true
) {
    if (doctors.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(Color(0xFFF8F8F8), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("No hay doctores disponibles", color = Color.Gray)
        }
        return
    }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(doctors) { doctor ->
            DoctorCard(
                doctor = doctor,
                isSelected = doctor.doctor.id == selectedDoctor?.doctor?.id,
                onClick = { if (enabled) onDoctorSelected(doctor) },
                primaryColor = primaryColor,
                lightColor = lightColor,
                enabled = enabled
            )
        }
    }
}

@Composable
fun DoctorCard(
    doctor: DoctorWithSchedules,
    isSelected: Boolean,
    onClick: () -> Unit,
    primaryColor: Color,
    lightColor: Color,
    enabled: Boolean = true
) {
    val borderColor = if (isSelected) primaryColor else Color.LightGray
    val backgroundColor = when {
        !enabled -> Color(0xFFF8F8F8)
        isSelected -> lightColor
        else -> Color.White
    }

    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(2.dp, borderColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(primaryColor.copy(alpha = if (enabled) 0.1f else 0.05f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "👨‍⚕️",
                    fontSize = 32.sp
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = doctor.doctor.name,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (enabled) Color.Black else Color.Gray,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = doctor.doctor.specialty,
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (isSelected && enabled) {
                Spacer(Modifier.height(4.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Seleccionado",
                    tint = primaryColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun CalendarView(
    yearMonth: YearMonth?,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    primaryColor: Color,
    lightColor: Color,
    enabled: Boolean = true
) {
    if (yearMonth == null) return

    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfMonth = yearMonth.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.ordinal % 7
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) Color.White else Color(0xFFF8F8F8)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("D", "L", "M", "M", "J", "V", "S").forEach { day ->
                    Text(
                        text = day,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (enabled) Color.Gray else Color.LightGray,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            var dayCounter = 1
            for (week in 0..5) {
                if (dayCounter > daysInMonth) break

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (dayOfWeek in 0..6) {
                        if ((week == 0 && dayOfWeek < firstDayOfWeek) || dayCounter > daysInMonth) {
                            Box(modifier = Modifier.weight(1f))
                        } else {
                            val currentDate = yearMonth.atDay(dayCounter)
                            val isSelected = currentDate == selectedDate
                            val isToday = currentDate == today
                            val isPast = currentDate < today

                            DayCell(
                                day = dayCounter,
                                isSelected = isSelected,
                                isToday = isToday,
                                isPast = isPast,
                                onClick = {
                                    if (!isPast && enabled) onDateSelected(currentDate)
                                },
                                primaryColor = primaryColor,
                                lightColor = lightColor,
                                enabled = enabled,
                                modifier = Modifier.weight(1f)
                            )
                            dayCounter++
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
fun DayCell(
    day: Int,
    isSelected: Boolean,
    isToday: Boolean,
    isPast: Boolean,
    onClick: () -> Unit,
    primaryColor: Color,
    lightColor: Color,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isSelected -> primaryColor
        isToday -> lightColor
        else -> Color.Transparent
    }

    val textColor = when {
        !enabled -> Color.LightGray
        isPast -> Color.LightGray
        isSelected -> Color.White
        isToday -> primaryColor
        else -> Color.Black
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .then(
                if (isToday && !isSelected) {
                    Modifier.border(1.dp, primaryColor, CircleShape)
                } else Modifier
            )
            .clickable(enabled = !isPast && enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            fontSize = 14.sp,
            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        )
    }
}

@Composable
fun TimeSlotButton(
    time: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    color: Color
) {
    val backgroundColor = if (isSelected) color else Color(0xFFF2F2F2)
    val textColor = if (isSelected) Color.White else Color.Black

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, color.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = time,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}

fun YearMonth.monthName(): String {
    return when (month) {
        1 -> "Enero"
        2 -> "Febrero"
        3 -> "Marzo"
        4 -> "Abril"
        5 -> "Mayo"
        6 -> "Junio"
        7 -> "Julio"
        8 -> "Agosto"
        9 -> "Septiembre"
        10 -> "Octubre"
        11 -> "Noviembre"
        12 -> "Diciembre"
        else -> ""
    }
}

@Composable
fun ScheduledAppointmentsList(
    appointments: List<AppointmentWithDetails>,
    primaryColor: Color,
    lightColor: Color,
    onDeleteClick: (AppointmentWithDetails) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        appointments.take(3).forEach { appointment ->
            ScheduledAppointmentCard(
                appointment = appointment,
                primaryColor = primaryColor,
                lightColor = lightColor,
                onDeleteClick = { onDeleteClick(appointment)}
            )
        }
    }
}

@Composable
fun ScheduledAppointmentCard(
    appointment: AppointmentWithDetails,
    primaryColor: Color,
    lightColor: Color,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = lightColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lado izquierdo: Información de la cita
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Nombre de la mascota
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🐾",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = appointment.petName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )


                }

                Spacer(Modifier.height(8.dp))

                // Doctor
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "👨‍⚕️",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Column {
                        Text(
                            text = appointment.doctorName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                        appointment.doctorSpecialty?.let { specialty ->
                            Text(
                                text = specialty,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Fecha y hora
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "📅",
                            fontSize = 14.sp,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            text = formatDate(appointment.date),
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🕒",
                            fontSize = 14.sp,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            text = appointment.time,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = primaryColor
                        )
                    }
                }

                // Notas (si existen)
                appointment.notes?.let { notes ->
                    if (notes.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "💬 $notes",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Lado derecho: Badge de estado
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar cita",
                    tint = Color.Red
                )
            }
        }
    }
}

fun formatDate(dateString: String): String {
    return try {
        val parts = dateString.split("-")
        if (parts.size == 3) {
            val day = parts[2]
            val month = parts[1]
            val year = parts[0]
            "$day/$month/$year"
        } else {
            dateString
        }
    } catch (e: Exception) {
        dateString
    }
}

