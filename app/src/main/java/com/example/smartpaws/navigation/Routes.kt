package com.example.smartpaws.navigation

// Clase sellada para rutas: evita "strings mágicos" y facilita refactors
sealed class Route(val path: String) { // Cada objeto representa una pantalla
    data object Home     : Route("home") // Ruta Home
    data object History     : Route("history")     // Ruta historial
    data object Login    : Route("login")    // Ruta Login
    data object Register : Route("register") // Ruta Registro
    data object Pets : Route("pets") //Ruta para las mascotas
    data object Appointment : Route("appointment") //Ruta para las citas
    data object User : Route("user") //RUTA PARA VER EL PERFIL
    data object AdminPanel : Route("admin_panel")
    data object DoctorAppointments : Route("doctor_appointments")
}

/*
* “Strings mágicos” se refiere a cuando pones un texto duro y repetido en varias partes del código,
* Si mañana cambias "home" por "inicio", tendrías que buscar todas las ocurrencias de "home" a mano.
* Eso es frágil y propenso a errores.
La idea es: mejor centralizar esos strings en una sola clase (Route), y usarlos desde ahí.*/