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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartpaws.R

@Composable
fun UserScreen() {
    val bg = Color(0xFFEAF9E7) // Fondo consistente con tu app
    val cardColor = Color(0xFFF8F9FA) // Color de las tarjetas

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Espacio superior
        Spacer(modifier = Modifier.height(32.dp))

        // Imagen del usuario del Larry
        Card(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.larry),
                contentDescription = "Foto de perfil de Larry",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Tarjeta con informazao del usuario
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = cardColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Información Personal",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CA771),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                InfoRow(
                    label = "Nombre:",
                    value = "Raulsiño"
                )

                InfoRow(
                    label = "Edad:",
                    value = "1000 años"
                )

                InfoRow(
                    label = "Correo:",
                    value = "ra.fernandez@duocuc.cl"
                )

                InfoRow(
                    label = "Dirección:",
                    value = "Huechuraba, DuocUC"
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF013237),
            modifier = Modifier.width(100.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = value,
            fontSize = 16.sp,
            color = Color(0xFF333333)
        )
    }
}