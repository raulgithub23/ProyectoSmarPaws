import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
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
import com.example.smartpaws.ui.screen.AppointmentScreen
import com.example.smartpaws.ui.screen.HistoryScreen
import com.example.smartpaws.ui.screen.HomeScreen
import com.example.smartpaws.ui.screen.LoginScreenVm
import com.example.smartpaws.ui.screen.RegisterScreenVm
import com.example.smartpaws.ui.screen.UserScreen
import com.example.smartpaws.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable // Gráfico de navegación + Drawer + Scaffold
fun AppNavGraph(navController: NavHostController,
                authViewModel: AuthViewModel) { // Recibe el controlador

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed) // Estado del drawer
    val scope = rememberCoroutineScope() // Necesario para abrir/cerrar drawer

    // Obtener la ruta actual para el bottom navigation
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Helpers de navegación (reutilizamos en topbar/drawer/botones y bottom bar)
    val goHome: () -> Unit    = { navController.navigate(Route.Home.path) }    // Ir a Home
    val goPets: () -> Unit    = { navController.navigate(Route.Pets.path) }    // Ir a Mascotas
    val goHistory: () -> Unit    = { navController.navigate(Route.History.path) }    // Ir a historial
    val goLogin: () -> Unit   = { navController.navigate(Route.Login.path) }   // Ir a Login
    val goRegister: () -> Unit = { navController.navigate(Route.Register.path) } // Ir a Registro
    val goAppointment: () -> Unit = { navController.navigate(Route.Appointment.path) } // Ir a citas
    val goUser: () -> Unit = { navController.navigate(Route.User.path)} //IR AL PERFIL DE USUARIO


    ModalNavigationDrawer( // Capa superior con drawer lateral
        drawerState = drawerState, // Estado del drawer
        drawerContent = { // Contenido del drawer (menú)
            AppDrawer( // Nuestro componente Drawer
                currentRoute = null, // Puedes pasar navController.currentBackStackEntry?.destination?.route
                items = defaultDrawerItems( // Lista estándar
                    onHome = {
                        scope.launch { drawerState.close() } // Cierra drawer
                        goHome() // Navega a Home
                    },
                    onLogin = {
                        scope.launch { drawerState.close() } // Cierra drawer
                        goLogin() // Navega a Login
                    },
                    onRegister = {
                        scope.launch { drawerState.close() } // Cierra drawer
                        goRegister() // Navega a Registro
                    },

                    onUser = {
                        scope.launch { drawerState.close() }
                        goUser()
                    }


                )
            )
        }
    ) {
        Scaffold( // Estructura base de pantalla
            topBar = { // Barra superior con íconos/menú
                AppTopBar(
                    onOpenDrawer = { scope.launch { drawerState.open() } }, // Abre drawer
                    onHome = goHome,     // Botón Home
                    onLogin = goLogin,   // Botón Login
                    onRegister = goRegister, // Botón Registro
                    onUser = goUser

                )
            },
            bottomBar = { // Barra inferior con navegación
                BottomNavigationBar(
                    onHome = goHome,
                    onAppointment = goAppointment,
                    onPets = goPets,
                    onHistory = goHistory,

                    )
            }
        ) { innerPadding -> // Padding que evita solapar contenido
            NavHost( // Contenedor de destinos navegables
                navController = navController, // Controlador
                startDestination = Route.Home.path, // Inicio: Home
                modifier = Modifier.padding(innerPadding) // Respeta topBar
            ) {
                composable(Route.Home.path) { // Destino Home
                    HomeScreen(
                        onGoLogin = goLogin,     // Botón para ir a Login
                        onGoRegister = goRegister // Botón para ir a Registro
                    )
                }

                composable(Route.Login.path) { // Destino Login
                    LoginScreenVm(
                        vm = authViewModel,
                        onLoginOkNavigateHome = goHome,
                        onGoRegister = goRegister
                    )
                }
                composable(Route.Register.path) { // Destino Registro
                    RegisterScreenVm(
                        vm = authViewModel,
                        onRegisteredNavigateLogin = goLogin,
                        onGoLogin = goLogin
                    )
                }
                composable(Route.History.path) { // Destino Mascotas
                    HistoryScreen(
                    )
                }
                composable(Route.Appointment.path) { // Destino AgendarCitas
                    AppointmentScreen(
                    )
                }
                composable(Route.Pets.path) { // Destino Mascotas
                    val viewModel: PetsViewModel = viewModel(
                        viewModelStoreOwner = LocalActivity.current as ComponentActivity
                    )
                    PetsScreen(viewModel = viewModel)
                }
                composable(Route.User.path) { // Destino Mascotas
                    UserScreen(
                    )
                }
            }
        }
    }
}