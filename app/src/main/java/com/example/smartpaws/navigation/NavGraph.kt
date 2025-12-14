package com.example.smartpaws.navigation

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
import com.example.smartpaws.data.repository.PetsRepository
import com.example.smartpaws.data.repository.UserRepository
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
import com.example.smartpaws.ui.screen.AdminPanelScreen
import com.example.smartpaws.ui.screen.DoctorAppointmentsScreen
import com.example.smartpaws.ui.screen.ForgotPasswordScreenVm
import com.example.smartpaws.viewmodel.AdminViewModel
import com.example.smartpaws.viewmodel.AppointmentViewModel
import com.example.smartpaws.viewmodel.AppointmentViewModelFactory
import com.example.smartpaws.viewmodel.AuthViewModel
import com.example.smartpaws.viewmodel.HistoryViewModel
import com.example.smartpaws.viewmodel.HistoryViewModelFactory
import com.example.smartpaws.viewmodel.HomeViewModel
import com.example.smartpaws.viewmodel.DoctorAppointmentsViewModel
import com.example.smartpaws.viewmodel.DoctorAppointmentsViewModelFactory

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
    userRepository: UserRepository,
    petsRepository: PetsRepository,
    historyViewModelFactory: HistoryViewModelFactory,
) {

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(Route.Login.path) {
            LoginScreenVm(
                vm = authViewModel,
                onLoginOkNavigateHome = {
                    // Verificamos el rol antes de navegar
                    val userProfile = authViewModel.userProfile.value
                    val destination = when (userProfile?.rol) {
                        "ADMIN" -> Route.AdminPanel.path
                        "DOCTOR" -> Route.DoctorAppointments.path
                        else -> Route.Home.path
                    }

                    navController.navigate(destination) {
                        popUpTo(Route.Login.path) { inclusive = true }
                    }
                },
                onGoRegister = { navController.navigate(Route.Register.path) },
                onGoForgotPassword = { navController.navigate(Route.ForgotPassword.path) }
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
                onGoLogin = { navController.popBackStack() }
            )
        }

        // NUEVA: Pantalla de recuperación de contraseña (TODO EN UNO)
        composable(Route.ForgotPassword.path) {
            ForgotPasswordScreenVm(
                vm = authViewModel,
                onBackToLogin = {
                    navController.popBackStack()
                },
                onSuccessNavigateToLogin = {
                    navController.navigate(Route.Login.path) {
                        popUpTo(Route.ForgotPassword.path) { inclusive = true }
                    }
                }
            )
        }

        composable(Route.Home.path) {
            MainScaffoldWrapper(navController, authViewModel, windowSizeClass) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToAppointments = {
                        navController.navigate(Route.Appointment.path) {
                            popUpTo(Route.Home.path)
                        }
                    }
                )
            }
        }

        composable(Route.Pets.path) {
            MainScaffoldWrapper(navController, authViewModel, windowSizeClass) {
                PetsScreen(petsViewModel = petsViewModel, authViewModel = authViewModel)
            }
        }

        composable(Route.History.path) {
            val historyViewModel: HistoryViewModel = viewModel(
                factory = HistoryViewModelFactory(
                    repository = appointmentRepository,
                    petsRepository = petsRepository,
                    doctorRepository = doctorRepository,
                    authViewModel = authViewModel
                )
            )
            MainScaffoldWrapper(navController, authViewModel, windowSizeClass) {
                HistoryScreen(viewModel = historyViewModel)
            }
        }

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
                AppointmentScreen(
                    viewModel = appointmentViewModel,
                    onAppointmentCreated = {
                        navController.navigate(Route.Appointment.path) { popUpTo(Route.Home.path) }
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
                AdminPanelScreen(viewModel = adminViewModel)
            }
        }

        composable(Route.DoctorAppointments.path) {
            val loginState by authViewModel.login.collectAsState()
            val userProfile by authViewModel.userProfile.collectAsState()

            val userId = loginState.userId ?: 0L
            val userEmail = userProfile?.email ?: ""

            val doctorVM: DoctorAppointmentsViewModel = viewModel(
                factory = DoctorAppointmentsViewModelFactory(
                    appointmentRepository = appointmentRepository,
                    doctorRepository = doctorRepository,
                    userRepository = userRepository,
                    petsRepository = petsRepository,
                    userId = userId,
                    userEmail = userEmail
                )
            )

            MainScaffoldWrapper(navController, authViewModel, windowSizeClass) {
                DoctorAppointmentsScreen(viewModel = doctorVM)
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
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val goHome = { navController.navigate(Route.Home.path) { popUpTo(Route.Home.path) { inclusive = true } } }
    val goPets = { navController.navigate(Route.Pets.path) { popUpTo(Route.Home.path) } }
    val goHistory = { navController.navigate(Route.History.path) { popUpTo(Route.Home.path) } }
    val goAppointment = { navController.navigate(Route.Appointment.path) { popUpTo(Route.Home.path) } }
    val goUser = { navController.navigate(Route.User.path) { popUpTo(Route.Home.path) } }
    val goLogin = { navController.navigate(Route.Login.path) { popUpTo(0) { inclusive = true } } }
    val goAdminPanel = { navController.navigate(Route.AdminPanel.path) { popUpTo(Route.Home.path) } }
    val goDoctorAppointments = { navController.navigate(Route.DoctorAppointments.path) { popUpTo(Route.Home.path) } }

    val onLogout = { authViewModel.logout(); goLogin() }

    val userProfile by authViewModel.userProfile.collectAsState()
    val isAdmin = userProfile?.rol == "ADMIN"
    val isDoctor = userProfile?.rol == "DOCTOR" || isAdmin

    val isCompact = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                currentRoute = currentRoute,
                items = defaultDrawerItems(
                    onHome = { scope.launch { drawerState.close() }; goHome() },
                    onUser = { scope.launch { drawerState.close() }; goUser() },
                    isAdmin = isAdmin,
                    onAdminPanel = { scope.launch { drawerState.close() }; goAdminPanel() },
                    isDoctor = isDoctor,
                    onDoctorAppointments = { scope.launch { drawerState.close() }; goDoctorAppointments() },
                    onLogout = { scope.launch { drawerState.close() }; onLogout() }
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