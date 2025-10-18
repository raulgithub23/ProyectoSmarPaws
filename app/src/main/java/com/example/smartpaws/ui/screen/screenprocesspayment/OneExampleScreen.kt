
package com.example.smartpaws.ui.screen.screenprocesspayment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*

@Composable
fun OneExampleScreen() {
    val green = Color(0xFF00C853)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(20.dp)
    ) {
        Text(text = "Agendamiento", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        // Doctor seleccionado
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Dr. Raul Fernandez", fontWeight = FontWeight.Bold)
                Text("Experto en corrutinas", color = Color.Gray, fontSize = 12.sp)
                Text("⭐ 4.9", color = green, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Octubre 2025", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        // Calendario simplificado (solo diseño)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color(0xFFF8F8F8), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("Calendar Placeholder", color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Horarios disponibles", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            TimeButton("8:00", selected = true, color = green)
            TimeButton("10:30", selected = false, color = green)
            TimeButton("16:15", selected = false, color = green)
        }

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = { },
            colors = ButtonDefaults.buttonColors(containerColor = green),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Agendar ahora", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TimeButton(text: String, selected: Boolean, color: Color) {
    val bg = if (selected) color else Color(0xFFF2F2F2)
    val textColor = if (selected) Color.White else Color.Black

    Box(
        modifier = Modifier
            .width(90.dp)
            .height(40.dp)
            .background(bg, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = textColor, fontWeight = FontWeight.Bold)
    }
}

@Composable
@Preview(showBackground = true)
fun OneScreenPreview(){
    OneExampleScreen()
}
