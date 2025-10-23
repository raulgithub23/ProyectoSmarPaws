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
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartpaws.data.local.doctors.DoctorWithSchedules
import com.example.smartpaws.viewmodel.AppointmentViewModel
import com.example.smartpaws.viewmodel.YearMonth
import kotlinx.datetime.*


//SCREEN PARA CREAR UN CITA PARA LA MASCOTA
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppointmentScreen(
    viewModel: AppointmentViewModel,
    petId: Long,
    onAppointmentCreated: () -> Unit = {}
) {
    val green = Color(0xFF00C853)
    val lightGreen = Color(0xFFE8F5E9)

    val uiState by viewModel.uiState.collectAsState()

    // Mostrar mensaje de éxito
    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            onAppointmentCreated()
        }
    }

    // Mostrar error
    uiState.errorMsg?.let { error ->
        LaunchedEffect(error) {
            // Aquí podrías mostrar un Snackbar o Toast
            viewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // Título
        Text(
            text = "Agendar Cita",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1B5E20),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // PASO 1: Selector de Doctor
        Text(
            text = "1. Selecciona un doctor",
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
                lightColor = lightGreen
            )
        }

        Spacer(Modifier.height(32.dp))

        // PASO 2: Selector de Fecha
        Text(
            text = "2. Selecciona una fecha",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1B5E20),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Navegación de mes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.previousMonth() }) {
                Icon(
                    Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Mes anterior",
                    tint = green
                )
            }

            Text(
                text = "${uiState.currentMonth.monthName()} ${uiState.currentMonth.year}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF1B5E20)
            )

            IconButton(onClick = { viewModel.nextMonth() }) {
                Icon(
                    Icons.Default.KeyboardArrowRight,
                    contentDescription = "Mes siguiente",
                    tint = green
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Calendario
        CalendarView(
            yearMonth = uiState.currentMonth,
            selectedDate = uiState.selectedDate,
            onDateSelected = { viewModel.selectDate(it) },
            primaryColor = green,
            lightColor = lightGreen,
            enabled = uiState.selectedDoctor != null
        )

        Spacer(Modifier.height(32.dp))

        // PASO 3: Selector de Horario
        Text(
            text = "3. Selecciona un horario",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1B5E20),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (uiState.selectedDate != null && uiState.selectedDoctor != null) {
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
                        "Primero selecciona un doctor y una fecha",
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
            onClick = { viewModel.scheduleAppointment(petId) },
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
                        "Agendar para ${uiState.selectedDate?.dayOfMonth}/${uiState.selectedDate?.monthNumber} a las ${uiState.selectedTime}"
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
fun DoctorSelector(
    doctors: List<DoctorWithSchedules>,
    selectedDoctor: DoctorWithSchedules?,
    onDoctorSelected: (DoctorWithSchedules) -> Unit,
    primaryColor: Color,
    lightColor: Color
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
                onClick = { onDoctorSelected(doctor) },
                primaryColor = primaryColor,
                lightColor = lightColor
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
    lightColor: Color
) {
    val borderColor = if (isSelected) primaryColor else Color.LightGray
    val backgroundColor = if (isSelected) lightColor else Color.White

    Card(
        modifier = Modifier
            .width(160.dp)
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
            // Ícono del doctor
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(primaryColor.copy(alpha = 0.1f), CircleShape),
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
                color = Color.Black,
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
            // Encabezado días de la semana
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

            // Grid de días
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

