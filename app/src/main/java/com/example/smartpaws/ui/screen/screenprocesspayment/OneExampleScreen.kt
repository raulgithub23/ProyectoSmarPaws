package com.example.smartpaws.ui.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppointmentScreen1() {
    val green = Color(0xFF00C853)
    val lightGreen = Color(0xFFE8F5E9)

    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedTime by remember { mutableStateOf<String?>(null) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // Título
        Text(
            text = "Agendamiento de Cita",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1B5E20)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Card del servicio/doctor
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Dr. Raúl Fernández",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1B5E20)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Medicina Veterinaria General",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⭐", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "4.9 (127 reseñas)",
                            color = green,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(lightGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🐾", fontSize = 28.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Selector de mes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { currentMonth = currentMonth.minusMonths(1) }
            ) {
                Icon(
                    Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Mes anterior",
                    tint = green
                )
            }

            Text(
                text = currentMonth.format(
                    DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es", "ES"))
                ).replaceFirstChar { it.uppercase() },
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF1B5E20)
            )

            IconButton(
                onClick = { currentMonth = currentMonth.plusMonths(1) }
            ) {
                Icon(
                    Icons.Default.KeyboardArrowRight,
                    contentDescription = "Mes siguiente",
                    tint = green
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Calendario
        CalendarView(
            yearMonth = currentMonth,
            selectedDate = selectedDate,
            onDateSelected = { date ->
                selectedDate = date
                selectedTime = null // Reset hora al cambiar día
            },
            primaryColor = green,
            lightColor = lightGreen
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Horarios disponibles (solo si hay fecha seleccionada)
        if (selectedDate != null) {
            Text(
                text = "Horarios disponibles para ${
                    selectedDate!!.format(
                        DateTimeFormatter.ofPattern("d 'de' MMMM", Locale("es", "ES"))
                    )
                }",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF1B5E20)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Grid de horarios
            val availableTimes = getAvailableTimesForDate(selectedDate!!)

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                items(availableTimes) { time ->
                    TimeSlotButton(
                        time = time,
                        isSelected = time == selectedTime,
                        onClick = { selectedTime = time },
                        color = green
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Botón de agendar
            Button(
                onClick = {
                    // Aquí irá la lógica de agendar
                    if (selectedTime != null) {
                        // Confirmar cita
                    }
                },
                enabled = selectedTime != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = green,
                    disabledContainerColor = Color.Gray
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = if (selectedTime != null)
                        "Agendar para ${selectedDate!!.format(DateTimeFormatter.ofPattern("d/MM"))} a las $selectedTime"
                    else
                        "Selecciona un horario",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        } else {
            // Mensaje cuando no hay fecha seleccionada
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFFF8F8F8), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "📅",
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Selecciona una fecha en el calendario",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarView(
    yearMonth: YearMonth?,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    primaryColor: Color,
    lightColor: Color
) {
    val daysInMonth = yearMonth?.lengthOfMonth()
    val firstDayOfMonth = yearMonth?.atDay(1)
    val firstDayOfWeek = firstDayOfMonth?.dayOfWeek?.value % 7 // 0 = Domingo
    val today = LocalDate.now()

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Encabezado con días de la semana
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("D", "L", "M", "M", "J", "V", "S").forEach { day ->
                    Text(
                        text = day,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Grid de días
            Column {
                var dayCounter = 1
                for (week in 0..5) {
                    if (dayCounter > daysInMonth) break

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (dayOfWeek in 0..6) {
                            if ((week == 0 && dayOfWeek < firstDayOfWeek) || dayCounter > daysInMonth) {
                                // Celda vacía
                                Box(modifier = Modifier.weight(1f))
                            } else {
                                val currentDate = yearMonth.atDay(dayCounter)
                                val isSelected = currentDate == selectedDate
                                val isToday = currentDate == today
                                val isPast = currentDate.isBefore(today)

                                DayCell(
                                    day = dayCounter,
                                    isSelected = isSelected,
                                    isToday = isToday,
                                    isPast = isPast,
                                    onClick = {
                                        if (!isPast) {
                                            onDateSelected(currentDate)
                                        }
                                    },
                                    primaryColor = primaryColor,
                                    lightColor = lightColor,
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
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isSelected -> primaryColor
        isToday -> lightColor
        else -> Color.Transparent
    }

    val textColor = when {
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
            .clickable(enabled = !isPast) { onClick() },
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

// Función helper para obtener horarios disponibles (simulación)
@RequiresApi(Build.VERSION_CODES.O)
private fun getAvailableTimesForDate(date: LocalDate): List<String> {
    // En una app real, esto vendría de una API o base de datos
    val baseHours = listOf(
        "08:00", "08:30", "09:00", "09:30", "10:00", "10:30",
        "11:00", "11:30", "14:00", "14:30", "15:00", "15:30",
        "16:00", "16:30", "17:00", "17:30", "18:00"
    )

    // Simular algunos horarios ocupados aleatoriamente
    val today = LocalDate.now()
    return if (date == today) {
        // Si es hoy, filtrar horarios pasados
        baseHours.filter {
            val hour = it.split(":")[0].toInt()
            hour > java.time.LocalTime.now().hour
        }.shuffled().take(9)
    } else {
        baseHours.shuffled().take(12)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
@Preview(showBackground = true)
fun AppointmentScreenPreview(){
    AppointmentScreen1()
}