package com.example.smartpaws.ui.screen.screenprocesspayment

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartpaws.viewmodel.AppointmentViewModel
import com.example.smartpaws.viewmodel.YearMonth
import kotlinx.datetime.*
//SCREEN PARA CREAR UN CITA PARA LA MASCOTA

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppointmentScreen(viewModel: AppointmentViewModel = viewModel()) {
    val green = Color(0xFF00C853)
    val lightGreen = Color(0xFFE8F5E9)

    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedTime by viewModel.selectedTime.collectAsState()
    val currentMonth by viewModel.currentMonth.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // ... Título y card del doctor igual ...

        // Selector de mes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.previousMonth() }) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Mes anterior", tint = green)
            }

            Text(
                text = "${currentMonth.monthName()} ${currentMonth.year}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF1B5E20)
            )

            IconButton(onClick = { viewModel.nextMonth() }) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Mes siguiente", tint = green)
            }
        }

        Spacer(Modifier.height(8.dp))

        CalendarView(
            yearMonth = currentMonth,
            selectedDate = selectedDate,
            onDateSelected = { viewModel.selectDate(it) },
            primaryColor = green,
            lightColor = lightGreen
        )

        Spacer(Modifier.height(24.dp))

        if (selectedDate != null) {
            val availableTimes = viewModel.getAvailableTimesForDate(selectedDate!!)

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
                        onClick = { viewModel.selectTime(time) },
                        color = green
                    )
                }
            }

            Spacer(Modifier.height(30.dp))

            Button(
                onClick = { /* Agendar cita */ },
                enabled = selectedTime != null,
                colors = ButtonDefaults.buttonColors(containerColor = green, disabledContainerColor = Color.Gray),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text(
                    text = selectedTime?.let { "Agendar para ${selectedDate!!.dayOfMonth}/${selectedDate!!.monthNumber} a las $it" }
                        ?: "Selecciona un horario",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        } else {
            // Mensaje si no hay fecha seleccionada
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFFF8F8F8), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📅", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Selecciona una fecha en el calendario", color = Color.Gray, fontSize = 14.sp, textAlign = TextAlign.Center)
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

// Extensión para mostrar nombre del mes en español
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
fun CalendarView(
    yearMonth: YearMonth?, // Nuestro YearMonth personalizado
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    primaryColor: Color,
    lightColor: Color
) {
    if (yearMonth == null) return

    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfMonth = yearMonth.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.ordinal % 7 // 0 = domingo
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Encabezado con días de la semana
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
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
            var dayCounter = 1
            for (week in 0..5) {
                if (dayCounter > daysInMonth) break

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    for (dayOfWeek in 0..6) {
                        if ((week == 0 && dayOfWeek < firstDayOfWeek) || dayCounter > daysInMonth) {
                            Box(modifier = Modifier.weight(1f)) // Celda vacía
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
                                onClick = { if (!isPast) onDateSelected(currentDate) },
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


@RequiresApi(Build.VERSION_CODES.O)
@Composable
@Preview(showBackground = true)
fun AppointmentScreenPreview(){
    AppointmentScreen()
}

