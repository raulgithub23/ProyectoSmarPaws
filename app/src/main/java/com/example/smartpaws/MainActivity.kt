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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppRoot(windowSizeClass: WindowSizeClass) {
    // ====== CONSTRUCCIÓN DE DEPENDENCIAS ======
    val context = LocalContext.current.applicationContext

    val userPreferences = remember { UserPreferences(context) }

    // CRÍTICO: Pasar el context al UserRepository
    val userRepository = remember { UserRepository(context = context) }

    val appointmentRepository = remember { AppointmentRepository() }
    val petsRepository = remember { PetsRepository() }
    val doctorRepository = remember { DoctorRepository() }

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
            authViewModel = authViewModel,
            petsRepository = petsRepository,
            doctorRepository = doctorRepository
        )
    )

    val petsViewModel: PetsViewModel = viewModel(
        factory = PetsViewModelFactory(
            petsRepository,
            authViewModel = authViewModel
        )
    )

    val adminViewModel: AdminViewModel = viewModel(
        factory = AdminViewModelFactory(userRepository, doctorRepository)
    )

    // Estados clave del AuthViewModel para mantener la sesión iniciada
    val isLoadingSession by authViewModel.isLoadingSession.collectAsState()
    val loginState by authViewModel.login.collectAsState()

    // ====== NAVEGACIÓN ======
    val navController = rememberNavController()
    SMARTPAWSTheme(dynamicColor = false) {
        Surface(color = MaterialTheme.colorScheme.background) {

            if (isLoadingSession) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Ruta de inicio dinámica
                val startDestination = if (loginState.userId != null) {
                    val userProfile = authViewModel.userProfile.collectAsState().value

                    when (userProfile?.rol) {
                        "ADMIN" -> Route.AdminPanel.path
                        "DOCTOR" -> Route.DoctorAppointments.path
                        "USER" -> Route.Home.path
                        else -> Route.Home.path
                    }
                } else {
                    Route.Login.path
                }

                AppNavGraph(
                    navController = navController,
                    windowSizeClass = windowSizeClass,
                    authViewModel = authViewModel,
                    historyViewModelFactory = historyViewModelFactory,
                    petsViewModel = petsViewModel,
                    appointmentRepository = appointmentRepository,
                    doctorRepository = doctorRepository,
                    homeViewModel = homeViewModel,
                    adminViewModel = adminViewModel,
                    startDestination = startDestination,
                    userRepository = userRepository,
                    petsRepository = petsRepository
                )
            }
        }
    }
}