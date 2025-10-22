import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.smartpaws.navigation.Route
import com.example.smartpaws.ui.components.AppDrawer
import com.example.smartpaws.ui.components.AppTopBar
import com.example.smartpaws.ui.components.BottomNavigationBar
import com.example.smartpaws.ui.components.defaultDrawerItems
import com.example.smartpaws.ui.mascota.PetsScreen
import com.example.smartpaws.ui.mascota.PetsViewModel
import com.example.smartpaws.ui.screen.screenprocesspayment.AppointmentScreen
import com.example.smartpaws.ui.screen.HistoryScreen
import com.example.smartpaws.ui.screen.HomeScreen
import com.example.smartpaws.ui.screen.LoginScreenVm
import com.example.smartpaws.ui.screen.RegisterScreenVm
import com.example.smartpaws.ui.screen.UserScreen
import com.example.smartpaws.viewmodel.AuthViewModel
import com.example.smartpaws.viewmodel.HistoryViewModel
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    historyViewModel: HistoryViewModel,
    petsViewModel: PetsViewModel
) {

    NavHost(
        navController = navController,
        startDestination = Route.Login.path, // Comienza en Login
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
            MainScaffoldWrapper(navController) {
                HomeScreen()
            }
        }

        composable(Route.Pets.path) {
            MainScaffoldWrapper(navController) {
                PetsScreen(
                    petsViewModel = petsViewModel,
                    authViewModel = authViewModel
                )
            }
        }

        composable(Route.History.path) {
            MainScaffoldWrapper(navController) {
                HistoryScreen(viewModel = historyViewModel)
            }
        }

        composable(Route.Appointment.path) {
            MainScaffoldWrapper(navController) {
                AppointmentScreen()
            }
        }

        composable(Route.User.path) {
            MainScaffoldWrapper(navController) {
                UserScreen()
            }
        }
    }
}

@Composable
private fun MainScaffoldWrapper(
    navController: NavHostController,
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
            popUpTo(0) { inclusive = true } // Limpia todo el back stack
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                currentRoute = currentRoute,
                items = defaultDrawerItems(
                    onHome = {
                        scope.launch { drawerState.close() }
                        goHome()
                    },
//                    onLogin = {
//                        scope.launch { drawerState.close() }
//                        goLogin()
//                    },
//                    onRegister = {
//                        scope.launch { drawerState.close() }
//                        // El registro solo está disponible desde login
//                    },
                    onUser = {
                        scope.launch { drawerState.close() }
                        goUser()
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
                    onRegister = {}, // No tiene sentido desde aquí
                    onUser = goUser
                )
            },
            bottomBar = {
                BottomNavigationBar(
                    currentRoute = currentRoute,
                    onHome = goHome,
                    onAppointment = goAppointment,
                    onPets = goPets,
                    onHistory = goHistory
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                content()
            }
        }
    }
}