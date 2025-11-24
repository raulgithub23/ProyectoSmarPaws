package com.example.smartpaws

import com.example.smartpaws.navigation.AppNavGraph
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
//import com.example.smartpaws.data.local.database.AppDatabase
import com.example.smartpaws.data.repository.AppointmentRepository
import com.example.smartpaws.data.repository.DoctorRepository
import com.example.smartpaws.data.repository.PetsRepository
import com.example.smartpaws.data.repository.UserRepository
import com.example.smartpaws.ui.mascota.PetsViewModel
import com.example.smartpaws.ui.mascota.PetsViewModelFactory
import com.example.smartpaws.ui.theme.SMARTPAWSTheme
import com.example.smartpaws.viewmodel.AuthViewModel
import com.example.smartpaws.viewmodel.AuthViewModelFactory
import com.example.smartpaws.viewmodel.HistoryViewModelFactory
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.smartpaws.data.local.storage.UserPreferences
import com.example.smartpaws.navigation.Route
import com.example.smartpaws.viewmodel.AdminViewModel
import com.example.smartpaws.viewmodel.AdminViewModelFactory
import com.example.smartpaws.viewmodel.HomeViewModel
import com.example.smartpaws.viewmodel.HomeViewModelFactory

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            AppRoot(windowSizeClass = windowSizeClass)
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
@RequiresApi(Build.VERSION_CODES.O)
@Composable // Indica que esta función dibuja UI
fun AppRoot(windowSizeClass: WindowSizeClass) { // Raíz de la app para separar responsabilidades (se conserva)
    // ====== NUEVO: construcción de dependencias (Composition Root) ======
    val context = LocalContext.current.applicationContext
    // ^ Obtenemos el applicationContext para construir la base de datos de Room.

    val userPreferences = remember { UserPreferences(context) }

//    val db = AppDatabase.getInstance(context)
    // ^ Singleton de Room. No crea múltiples instancias.

//    val userDao = db.userDao()
    // ^ Obtenemos el DAO de usuarios desde la DB.

//    val appointmentDao = db.appointmentDao()
    // ^ Obtenemos el DAO de citas desde la DB.
    // Los DAOs contienen las queries SQL (@Query, @Insert, @Update, @Delete)

//    val doctorDao = db.doctorDao()

//    val petFactDao =  db.petFactDao()

//    val petsDao = db.petsDao()

    val userRepository = UserRepository()
    // ^ Repositorio que encapsula la lógica de login/registro contra Room.

    val appointmentRepository = AppointmentRepository()
    val petsRepository = PetsRepository()
    val doctorRepository = DoctorRepository()


    // ^ Repositorio que encapsula la lógica de gestión de citas.
    // Expone Flows reactivos para que la UI se actualice automáticamente
    // cuando cambien los datos en la base de datos.


    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(userRepository, userPreferences)
    )

    val historyViewModelFactory = HistoryViewModelFactory(
        repository = appointmentRepository,
        authViewModel = authViewModel,
        petsRepository = petsRepository,
        doctorRepository = doctorRepository
    )

    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(
            repository = appointmentRepository,
//            petFactDao = petFactDao,
            authViewModel = authViewModel,
            petsRepository = petsRepository,
            doctorRepository = doctorRepository
        )
    )
    // ^ Creamos los ViewModels con factory para inyectar los repositorios.
    //   Esto reemplaza cualquier uso anterior de listas en memoria (USERS).
    // La factory es necesaria porque el ViewModel necesita recibir parámetros
    // (el repository) en su constructor, y sin factory Android no sabría cómo crearlo.


    val petsViewModel: PetsViewModel = viewModel(
        factory = PetsViewModelFactory(
            petsRepository,
            authViewModel = authViewModel
        )
    )


    val adminViewModel: AdminViewModel = viewModel(
        factory = AdminViewModelFactory(userRepository, doctorRepository)
    )
    // estados clave del AuthViewModel para mantener la sesion iniciada por el localStarage (Por ID)
    val isLoadingSession by authViewModel.isLoadingSession.collectAsState()
    val loginState by authViewModel.login.collectAsState()

    // ====== TU NAVEGACIÓN ORIGINAL ======
    val navController = rememberNavController() // Controlador de navegación (igual que antes)
    SMARTPAWSTheme(dynamicColor = false) { // Provee colores/tipografías Material 3 (igual que antes)
        Surface(color = MaterialTheme.colorScheme.background) { // Fondo general (igual que antes)

            if (isLoadingSession) {
                // Mostramos una pantalla de carga simple. mientras obtenemos el localStorage del viewModel
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // ruta de inicio dinámicamente
                val startDestination = if (loginState.userId != null) {
                    val userProfile = authViewModel.userProfile.collectAsState().value

                    when (userProfile?.rol) {
                        "ADMIN" -> Route.AdminPanel.path
                        "DOCTOR" -> Route.DoctorAppointments.path
                        "USER" -> Route.Home.path
                        else -> Route.Home.path  // Por defecto
                    }
                } else {
                    Route.Login.path
                }

                // Le pasamos la ruta de inicio dinámica al NavGraph
                // ====== MOD: pasamos los ViewModels a tu NavGraph ======
                // Si tu AppNavGraph ya recibía el VM o lo creaba adentro, lo mejor ahora es PASARLO
                // para que toda la app use la MISMA instancia que acabamos de inyectar.
                AppNavGraph(
                    navController = navController,
                    windowSizeClass = windowSizeClass,
                    authViewModel = authViewModel, // VM para Login/Register
                    historyViewModelFactory = historyViewModelFactory, // VM para Historial de citas
                    petsViewModel = petsViewModel,
                    appointmentRepository = appointmentRepository,
                    doctorRepository = doctorRepository,
                    homeViewModel = homeViewModel,
                    adminViewModel = adminViewModel,
                    startDestination = startDestination,
                    userRepository = userRepository,
                    petsRepository = petsRepository
                )
                // NOTA: Si tu AppNavGraph no tiene estos parámetros aún, basta con agregarlos:
                // fun AppNavGraph(navController: NavHostController, authViewModel: AuthViewModel, historyViewModel: HistoryViewModel) { ... }
                // y luego pasar esos ViewModels a las pantallas donde se usen.
                // Esto garantiza que todas las pantallas compartan la MISMA instancia del ViewModel
                // y por lo tanto compartan el mismo estado.
            }
        }
    }
}