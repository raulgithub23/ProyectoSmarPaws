package com.example.smartpaws

import AppNavGraph
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.smartpaws.data.local.database.AppDatabase
import com.example.smartpaws.data.repository.AppointmentRepository
import com.example.smartpaws.data.repository.UserRepository
import com.example.smartpaws.ui.theme.SMARTPAWSTheme
import com.example.smartpaws.viewmodel.AuthViewModel
import com.example.smartpaws.viewmodel.AuthViewModelFactory
import com.example.smartpaws.viewmodel.HistoryViewModel
import com.example.smartpaws.viewmodel.HistoryViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppRoot()
        }
    }
}


/*
* En Compose, Surface es un contenedor visual que viene de Material 3.Crea un bloque
*  que puedes personalizar con color, forma, sombra (elevación).
Sirve para aplicar un fondo (color, borde, elevación, forma) siguiendo las guías de diseño
* de Material.
Tenemos que pensar en él como una "lona base" sobre la cual vas a pintar tu UI.
* Si cambias el tema a dark mode, colorScheme.background
* cambia automáticamente y el Surface pinta la pantalla con el nuevo color.
* */
@Composable // Indica que esta función dibuja UI
fun AppRoot() { // Raíz de la app para separar responsabilidades (se conserva)
    // ====== NUEVO: construcción de dependencias (Composition Root) ======
    val context = LocalContext.current.applicationContext
    // ^ Obtenemos el applicationContext para construir la base de datos de Room.

    val db = AppDatabase.getInstance(context)
    // ^ Singleton de Room. No crea múltiples instancias.

    val userDao = db.userDao()
    // ^ Obtenemos el DAO de usuarios desde la DB.

    val appointmentDao = db.appointmentDao()
    // ^ Obtenemos el DAO de citas desde la DB.
    // Los DAOs contienen las queries SQL (@Query, @Insert, @Update, @Delete)

    val userRepository = UserRepository(userDao)
    // ^ Repositorio que encapsula la lógica de login/registro contra Room.

    val appointmentRepository = AppointmentRepository(appointmentDao)
    // ^ Repositorio que encapsula la lógica de gestión de citas.
    // Expone Flows reactivos para que la UI se actualice automáticamente
    // cuando cambien los datos en la base de datos.

    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(userRepository)
    )

    val historyViewModel: HistoryViewModel = viewModel(
        factory = HistoryViewModelFactory(appointmentRepository)
    )
    // ^ Creamos los ViewModels con factory para inyectar los repositorios.
    //   Esto reemplaza cualquier uso anterior de listas en memoria (USERS).
    // La factory es necesaria porque el ViewModel necesita recibir parámetros
    // (el repository) en su constructor, y sin factory Android no sabría cómo crearlo.

    // ====== TU NAVEGACIÓN ORIGINAL ======
    val navController = rememberNavController() // Controlador de navegación (igual que antes)
    SMARTPAWSTheme(dynamicColor = false) { // Provee colores/tipografías Material 3 (igual que antes)
        Surface(color = MaterialTheme.colorScheme.background) { // Fondo general (igual que antes)

            // ====== MOD: pasamos los ViewModels a tu NavGraph ======
            // Si tu AppNavGraph ya recibía el VM o lo creaba adentro, lo mejor ahora es PASARLO
            // para que toda la app use la MISMA instancia que acabamos de inyectar.
            AppNavGraph(
                navController = navController,
                authViewModel = authViewModel, // VM para Login/Register
                historyViewModel = historyViewModel // VM para Historial de citas

            )
            // NOTA: Si tu AppNavGraph no tiene estos parámetros aún, basta con agregarlos:
            // fun AppNavGraph(navController: NavHostController, authViewModel: AuthViewModel, historyViewModel: HistoryViewModel) { ... }
            // y luego pasar esos ViewModels a las pantallas donde se usen.
            // Esto garantiza que todas las pantallas compartan la MISMA instancia del ViewModel
            // y por lo tanto compartan el mismo estado.
        }
    }
}