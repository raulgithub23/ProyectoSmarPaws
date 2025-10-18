package com.example.smartpaws.ui.screen.screenprocesspayment

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartpaws.R

@Composable
fun SecondExampleScreen() {
    val green = Color(0xFF00C853)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Ubicación
        Text(text = "Locación", color = Color.Gray, fontSize = 14.sp)
        Text(text = "Huechuraba Duoc UC", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(20.dp))

        // Categorías
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CategoryButton("Medico", R.drawable.logoblanco, Color.Green)
            CategoryButton("Catologo", R.drawable.logoblanco, Color.Green)
            CategoryButton("Perrologo", R.drawable.logoblanco, Color.Green)
            CategoryButton("Dentista", R.drawable.logoblanco, Color.Green)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Título
        Text(text = "Veterinarios Disponibles", fontSize = 18.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        // Tarjetas de especialistas
        SpecialistCard("Dr. Raul Johanson", "Especialista en gatos negros", "5.0", R.drawable.larry, green)
        SpecialistCard("Dr. Gabriel Sherman", "Especialista en gatos blancos", "4.9", R.drawable.catmeme, green)
    }
}

@Composable
fun CategoryButton(text: String, iconRes: Int, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(70.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(8.dp)
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = text,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = text, fontSize = 12.sp, color = Color.Black)
    }
}

@Composable
fun SpecialistCard(name: String, specialty: String, rating: String, imageRes: Int, accentColor: Color) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(imageRes),
                contentDescription = name,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, fontWeight = FontWeight.Bold)
                Text(text = specialty, color = Color.Gray, fontSize = 12.sp)
            }
            Text(text = "⭐ $rating", color = accentColor, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
@Preview(showBackground = true)
fun SecondScreenPreview(){
    SecondExampleScreen()
}