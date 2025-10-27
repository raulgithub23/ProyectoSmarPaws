package com.example.smartpaws.navigation

import HistoryViewModel
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.smartpaws.data.repository.AppointmentRepository
import com.example.smartpaws.data.repository.DoctorRepository
import com.example.smartpaws.ui.components.AppDrawer
import com.example.smartpaws.ui.components.AppNavigationRail
import com.example.smartpaws.ui.components.AppTopBar
import com.example.smartpaws.ui.components.BottomNavigationBar
import com.example.smartpaws.ui.components.defaultDrawerItems
import com.example.smartpaws.ui.mascota.PetsScreen
import com.example.smartpaws.ui.mascota.PetsViewModel
import com.example.smartpaws.ui.screen.AppointmentScreen
import com.example.smartpaws.ui.screen.HistoryScreen
import com.example.smartpaws.ui.screen.HomeScreen
import com.example.smartpaws.ui.screen.LoginScreenVm
import com.example.smartpaws.ui.screen.RegisterScreenVm
import com.example.smartpaws.ui.screen.UserScreen
import com.example.smartpaws.view.AdminPanelScreen
import com.example.smartpaws.viewmodel.AdminViewModel
import com.example.smartpaws.viewmodel.AppointmentViewModel
import com.example.smartpaws.viewmodel.AppointmentViewModelFactory
import com.example.smartpaws.viewmodel.AuthViewModel
import com.example.smartpaws.viewmodel.HistoryViewModelFactory
import com.example.smartpaws.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String,
    windowSizeClass: WindowSizeClass,
    authViewModel: AuthViewModel,
    appointmentRepository: AppointmentRepository,
    doctorRepository: DoctorRepository,
    petsViewModel: PetsViewModel,
    homeViewModel: HomeViewModel,
    adminViewModel: AdminViewModel,
    historyViewModelFactory: HistoryViewModelFactory
) {

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.fillMaxSize()
    ) {
        // ========== PANTALLAS DE AUTENTICACIÓN (sin Scaffold) ==========
        composable(Route.Login.path) {
            LoginScreenVm(
                vm = authViewModel,
                onLoginOkNavigateHome = {
                    // Navega a Home y limpia el back stack para que no pueda volver al login
                    navController.navigate(Route.Home.path) {
                        popUpTo(Route.Login.path) { inclusive = true }
                    }
                },
                onGoRegister = {
                    navController.navigate(Route.Register.path)
                }
            )
        }

        composable(Route.Register.path) {
            RegisterScreenVm(
                vm = authViewModel,
                onRegisteredNavigateLogin = {
                    navController.navigate(Route.Login.path) {
                        popUpTo(Route.Register.path) { inclusive = true }
                    }
                },
                onGoLogin = {
                    navController.popBackStack() // Vuelve al login
                }
            )
        }

        // ========== PANTALLAS PRINCIPALES (con Scaffold completo) ==========
        // Envolvemos todas las pantallas autenticadas en un Scaffold compartido
        composable(Route.Home.path) {
            MainScaffoldWrapper(navController, authViewModel, windowSizeClass) {
                HomeScreen(viewModel = homeViewModel)
            }
        }

        composable(Route.Pets.path) {
            MainScaffoldWrapper(navController, authViewModel, windowSizeClass) {
                PetsScreen(
                    petsViewModel = petsViewModel,
                    authViewModel = authViewModel
                )
            }
        }

        composable(Route.History.path) {
            val historyViewModel: HistoryViewModel = viewModel(
                factory = historyViewModelFactory
            )
            MainScaffoldWrapper(navController, authViewModel, windowSizeClass) {
                HistoryScreen(viewModel = historyViewModel)
            }
        }

        // ========== RUTA ALTERNATIVA: Appointment sin petId específico ==========
        // Si el usuario va directamente desde el bottom bar sin seleccionar mascota
        composable(Route.Appointment.path) {
            val loginState by authViewModel.login.collectAsState()
            val userId = loginState.userId ?: 1L

            val appointmentViewModel: AppointmentViewModel = viewModel(
                factory = AppointmentViewModelFactory(
                    appointmentRepository = appointmentRepository,
                    doctorRepository = doctorRepository,
                    petsViewModel = petsViewModel,
                    userId = userId
                ),
                key = "appointment_$userId"
            )

            MainScaffoldWrapper(navController, authViewModel, windowSizeClass) {
                // Aquí podrías mostrar primero un selector de mascotas
                // o redirigir a la pantalla de mascotas
                AppointmentScreen(
                    viewModel = appointmentViewModel,
                    onAppointmentCreated = {
                        // PARA REDIRIGIR EN CASO QUE SE NECESITE (para home u otra)
                        navController.navigate(Route.Appointment.path) {
                            popUpTo(Route.Home.path)
                        }
                    }
                )
            }
        }

        composable(Route.User.path) {
            MainScaffoldWrapper(navController, authViewModel, windowSizeClass) {
                UserScreen(authViewModel = authViewModel)
            }
        }

        composable(Route.AdminPanel.path) {
            MainScaffoldWrapper(navController, authViewModel, windowSizeClass) {
                // Aquí iría tu pantalla de UI para el admin
                // Asumimos que tienes un composable llamado AdminPanelScreen
                AdminPanelScreen(viewModel = adminViewModel)
            }
        }
    }
}

@Composable
private fun MainScaffoldWrapper(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    windowSizeClass: WindowSizeClass,
    content: @Composable () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    // Obtener la ruta actual para el bottom navigation
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    // Helpers de navegación
    val goHome: () -> Unit = {
        navController.navigate(Route.Home.path) {
            popUpTo(Route.Home.path) { inclusive = true }
        }
    }
    val goPets: () -> Unit = {
        navController.navigate(Route.Pets.path) {
            popUpTo(Route.Home.path)
        }
    }
    val goHistory: () -> Unit = {
        navController.navigate(Route.History.path) {
            popUpTo(Route.Home.path)
        }
    }
    val goAppointment: () -> Unit = {
        // Navega a appointment sin petId específico
        navController.navigate(Route.Appointment.path) {
            popUpTo(Route.Home.path)
        }
    }
    val goUser: () -> Unit = {
        navController.navigate(Route.User.path) {
            popUpTo(Route.Home.path)
        }
    }
    val goLogin: () -> Unit = {
        navController.navigate(Route.Login.path) {
            popUpTo(0) { inclusive = true }
        }
    }

    val goAdminPanel: () -> Unit = {
        navController.navigate(Route.AdminPanel.path) {
            popUpTo(Route.Home.path)
        }
    }

    val onLogout: () -> Unit = {
        authViewModel.logout()
        goLogin()
    }

    val userProfile by authViewModel.userProfile.collectAsState()
    val isAdmin = userProfile?.rol == "ADMIN"

    // Para saber el tamaño de la pantalla actual si es compata o mas grande
    val isCompact = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                currentRoute = currentRoute,
                // --- 2. PASAR LA NUEVA ACCIÓN AL DRAWER ---
                items = defaultDrawerItems(
                    onHome = {
                        scope.launch { drawerState.close() }
                        goHome()
                    },
                    onUser = {
                        scope.launch { drawerState.close() }
                        goUser()
                    },
                    isAdmin = isAdmin,
                    onAdminPanel = {
                        scope.launch { drawerState.close() }
                        goAdminPanel()
                    },
                    onLogout = {
                        scope.launch { drawerState.close() }
                        onLogout()
                    }
                )
            )
        }
    ) {
        Scaffold(
            topBar = {
                AppTopBar(
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onHome = goHome,
                    onLogin = goLogin,
                    onRegister = {},
                    onUser = goUser
                )
            },
            bottomBar = {
                if (isCompact) {
                    BottomNavigationBar(
                        currentRoute = currentRoute,
                        onHome = goHome,
                        onAppointment = goAppointment,
                        onPets = goPets,
                        onHistory = goHistory
                    )
                }
            }
        ) { innerPadding ->
            Row(modifier = Modifier.padding(innerPadding)) {
                if (!isCompact) {
                    AppNavigationRail(
                        currentRoute = currentRoute,
                        onHome = goHome,
                        onAppointment = goAppointment,
                        onPets = goPets,
                        onHistory = goHistory
                    )
                }
                Box(modifier = Modifier.fillMaxSize()) {
                    content()
                }
            }
        }
    }
}