package com.example.smartpaws.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.smartpaws.data.local.doctors.DoctorEntity
import com.example.smartpaws.data.local.doctors.DoctorScheduleEntity
import com.example.smartpaws.data.local.doctors.DoctorWithSchedules
import com.example.smartpaws.data.local.user.UserEntity
import com.example.smartpaws.domain.validation.validateEmail
import com.example.smartpaws.domain.validation.validateNameLettersOnly
import com.example.smartpaws.domain.validation.validateNotEmpty
import com.example.smartpaws.domain.validation.validatePhoneDigitsOnly
import com.example.smartpaws.domain.validation.validateStrongPassword
import com.example.smartpaws.viewmodel.AdminStats
import com.example.smartpaws.viewmodel.AdminUiState
import com.example.smartpaws.viewmodel.AdminViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(viewModel: AdminViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Estados para los diálogos
    var showCreateDoctorDialog by remember { mutableStateOf(false) }
    var showDeleteUserDialog by remember { mutableStateOf<UserEntity?>(null) }
    var showChangeRoleDialog by remember { mutableStateOf<UserEntity?>(null) }
    var showDeleteDoctorDialog by remember { mutableStateOf<DoctorEntity?>(null) }
    var showScheduleDialog by remember { mutableStateOf<DoctorWithSchedules?>(null) }

    // Estado para las pestañas
    var tabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Usuarios", "Doctores")

    //Para el re-render del composable asi e
    LaunchedEffect(Unit) {
        viewModel.loadAllData()
    }

    // Observador para mostrar Snackbars de éxito o error
    LaunchedEffect(uiState.errorMsg, uiState.successMsg) {
        uiState.errorMsg?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.clearMessages()
        }
        uiState.successMsg?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (tabIndex == 0) { // Pestaña Usuarios
                FloatingActionButton(onClick = { /* para usuarios */ }) {
                    Icon(Icons.Default.Add, contentDescription = "Crear Usuario")
                }
            } else { // Pestaña Doctores
                FloatingActionButton(onClick = { showCreateDoctorDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Crear Doctor")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // PESTAÑAS
            PrimaryTabRow(selectedTabIndex = tabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        text = { Text(title) },
                        selected = tabIndex == index,
                        onClick = { tabIndex = index }
                    )
                }
            }

            // CONTENIDO DE LAS PESTAÑAS
            when (tabIndex) {
                // --- Pestaña 0: GESTIÓN DE USUARIOS ---
                0 -> UsersTabContent(
                    uiState = uiState,
                    viewModel = viewModel,
                    onChangeRoleClick = { showChangeRoleDialog = it },
                    onDeleteClick = { showDeleteUserDialog = it }
                )

                // --- Pestaña 1: GESTIÓN DE DOCTORES ---
                1 -> DoctorsTabContent(
                    uiState = uiState,
                    onEditSchedulesClick = { showScheduleDialog = it },
                    onDeleteProfileClick = { showDeleteDoctorDialog = it.doctor }
                )
            }
        }
    }

    // --- Diálogos ---

    // Diálogo para CREAR Doctor (User + Profile)
    if (showCreateDoctorDialog) {
        CreateDoctorDialog(
            onDismiss = { showCreateDoctorDialog = false },
            onCreate = { name, email, phone, password, specialty ->
                viewModel.createDoctor(name, email, phone, password, specialty)
                showCreateDoctorDialog = false
            }
        )
    }

    // Diálogo para GESTIONAR HORARIOS
    showScheduleDialog?.let { doctorWithSchedules ->
        ManageScheduleDialog(
            doctorWithSchedules = doctorWithSchedules,
            onDismiss = { showScheduleDialog = null },
            onConfirm = { doctorId, newSchedules ->
                viewModel.updateDoctorSchedules(doctorId, newSchedules)
                showScheduleDialog = null
            }
        )
    }

    // Diálogo para ELIMINAR PERFIL de Doctor
    showDeleteDoctorDialog?.let { doctor ->
        DeleteConfirmationDialog(
            title = "Eliminar Perfil de Doctor",
            text = "¿Estás seguro de que deseas eliminar el perfil de ${doctor.name}? Esto no eliminará su cuenta de usuario, solo su perfil de doctor.",
            onDismiss = { showDeleteDoctorDialog = null },
            onConfirm = {
                viewModel.deleteDoctorProfile(doctor)
                showDeleteDoctorDialog = null
            }
        )
    }

    // Diálogo para ELIMINAR Usuario
    showDeleteUserDialog?.let { user ->
        DeleteConfirmationDialog(
            title = "Eliminar Usuario",
            text = "¿Estás seguro de que deseas eliminar a ${user.name}? Esta acción no se puede deshacer.",
            onDismiss = { showDeleteUserDialog = null },
            onConfirm = {
                viewModel.deleteUser(user.id)
                showDeleteUserDialog = null
            }
        )
    }

    // Diálogo para CAMBIAR ROL
    showChangeRoleDialog?.let { user ->
        ChangeRoleDialog(
            user = user,
            onDismiss = { showChangeRoleDialog = null },
            onConfirm = { newRole ->
                viewModel.updateUserRole(user.id, newRole)
                showChangeRoleDialog = null
            }
        )
    }
}

// --- Contenido de la Pestaña USUARIOS ---
@Composable
fun UsersTabContent(
    uiState: AdminUiState,
    viewModel: AdminViewModel,
    onChangeRoleClick: (UserEntity) -> Unit,
    onDeleteClick: (UserEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
//            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Tarjeta de Estadísticas
        Spacer(modifier = Modifier.height(16.dp))
        AdminStatsCard(stats = uiState.stats)
        Spacer(modifier = Modifier.height(16.dp))

        // 2. Controles de Búsqueda y Filtro
        UserListControls(
            searchQuery = uiState.searchQuery,
            selectedRole = uiState.selectedRole,
            onSearchQueryChange = viewModel::onSearchQueryChange,
            onRoleFilterChange = viewModel::filterByRole
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 3. Indicador de Carga
        if (uiState.isLoading) {
            CircularProgressIndicator()
        }

        // 4. Lista de Usuarios
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.users, key = { it.id }) { user ->
                UserListItem(
                    user = user,
                    onChangeRoleClick = { onChangeRoleClick(user) },
                    onDeleteClick = { onDeleteClick(user) }
                )
            }
        }
    }
}

// --- Contenido de la Pestaña DOCTORES ---
@Composable
fun DoctorsTabContent(
    uiState: AdminUiState,
    onEditSchedulesClick: (DoctorWithSchedules) -> Unit,
    onDeleteProfileClick: (DoctorWithSchedules) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
//            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoading) {
            CircularProgressIndicator()
        }

        if (uiState.doctors.isEmpty() && !uiState.isLoading) {
            Text("No hay perfiles de doctor creados.")
        }

        // Lista de Doctores
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.doctors, key = { it.doctor.id }) { doctorWithSchedules ->
                DoctorListItem(
                    doctorWithSchedules = doctorWithSchedules,
                    onEditSchedules = { onEditSchedulesClick(doctorWithSchedules) },
                    onDeleteProfile = { onDeleteProfileClick(doctorWithSchedules) }
                )
            }
        }
    }
}

// --- Composables Auxiliares ---

@Composable
fun AdminStatsCard(stats: AdminStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Estadísticas de Usuarios",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatItem("Total", stats.totalUsers)
                StatItem("Admins", stats.adminCount)
                StatItem("Doctores", stats.doctorCount)
                StatItem("Usuarios", stats.userCount)
            }
        }
    }
}

@Composable
fun StatItem(label: String, count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserListControls(
    searchQuery: String,
    selectedRole: String?,
    onSearchQueryChange: (String) -> Unit,
    onRoleFilterChange: (String?) -> Unit
) {
    var filterExpanded by remember { mutableStateOf(false) }
    val roles = listOf("TODOS", "ADMIN", "DOCTOR", "USER")

    Column {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            label = { Text("Buscar por nombre o email") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = filterExpanded,
            onExpandedChange = { filterExpanded = !filterExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedRole ?: "Filtrar por rol",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = filterExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )
            ExposedDropdownMenu(
                expanded = filterExpanded,
                onDismissRequest = { filterExpanded = false }
            ) {
                roles.forEach { role ->
                    DropdownMenuItem(
                        text = { Text(role) },
                        onClick = {
                            val roleToFilter = if (role == "TODOS") null else role
                            onRoleFilterChange(roleToFilter)
                            filterExpanded = false
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun UserListItem(
    user: UserEntity,
    onChangeRoleClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(user.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Email: ${user.email}", style = MaterialTheme.typography.bodyMedium)
                Text("Tel: ${user.phone}", style = MaterialTheme.typography.bodySmall)
                Text(
                    "Rol: ${user.rol}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = when (user.rol) {
                        "ADMIN" -> MaterialTheme.colorScheme.error
                        "DOCTOR" -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
            }
            IconButton(onClick = onChangeRoleClick) {
                Icon(Icons.Default.Edit, contentDescription = "Cambiar Rol")
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDoctorDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, email: String, phone: String, password: String, specialty: String) -> Unit
) {
    // --- Estados de los campos
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var specialty by rememberSaveable { mutableStateOf("") }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }

    // --- Estados de error
    var nameError by rememberSaveable { mutableStateOf<String?>(null) }
    var emailError by rememberSaveable { mutableStateOf<String?>(null) }
    var phoneError by rememberSaveable { mutableStateOf<String?>(null) }
    var passwordError by rememberSaveable { mutableStateOf<String?>(null) }
    var specialtyError by rememberSaveable { mutableStateOf<String?>(null) }


    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Crear Nuevo Doctor") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                // --- Campo Nombre ---
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (nameError != null) nameError = null
                    },
                    label = { Text("Nombre Completo") },
                    singleLine = true,
                    isError = nameError != null,
                    supportingText = {
                        nameError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    // Validar al perder el foco
                    modifier = Modifier.onFocusChanged { focusState ->
                        if (!focusState.isFocused) {
                            nameError = validateNameLettersOnly(name)
                        }
                    }
                )

                // --- Campo Email ---
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        if (emailError != null) emailError = null
                    },
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    isError = emailError != null,
                    supportingText = {
                        emailError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    modifier = Modifier.onFocusChanged { focusState ->
                        if (!focusState.isFocused) {
                            emailError = validateEmail(email)
                        }
                    }
                )

                // --- Campo Teléfono ---
                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        if (phoneError != null) phoneError = null
                    },
                    label = { Text("Teléfono") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    isError = phoneError != null,
                    supportingText = {
                        phoneError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    modifier = Modifier.onFocusChanged { focusState ->
                        if (!focusState.isFocused) {
                            phoneError = validatePhoneDigitsOnly(phone)
                        }
                    }
                )

                // --- Campo Especialidad ---
                OutlinedTextField(
                    value = specialty,
                    onValueChange = {
                        specialty = it
                        if (specialtyError != null) specialtyError = null
                    },
                    label = { Text("Especialidad") },
                    singleLine = true,
                    isError = specialtyError != null,
                    supportingText = {
                        specialtyError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    modifier = Modifier.onFocusChanged { focusState ->
                        if (!focusState.isFocused) {
                            specialtyError = validateNotEmpty(specialty, "Especialidad")
                        }
                    }
                )

                // --- Campo Contraseña ---
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        if (passwordError != null) passwordError = null
                    },
                    label = { Text("Contraseña Temporal") },
                    isError = passwordError != null,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                        val description = if (isPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(image, description)
                        }
                    },
                    supportingText = {
                        passwordError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    modifier = Modifier.onFocusChanged { focusState ->
                        if (!focusState.isFocused) {
                            passwordError = validateStrongPassword(password)
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // 3. Validar TODO al hacer clic en crear
                    val vName = validateNameLettersOnly(name)
                    val vEmail = validateEmail(email)
                    val vPhone = validatePhoneDigitsOnly(phone)
                    val vSpecialty = validateNotEmpty(specialty, "Especialidad")
                    val vPassword = validateStrongPassword(password)

                    // Asignar todos los errores (para mostrarlos si fallan)
                    nameError = vName
                    emailError = vEmail
                    phoneError = vPhone
                    specialtyError = vSpecialty
                    passwordError = vPassword

                    // Comprobar si todo es válido
                    val isValid = vName == null && vEmail == null && vPhone == null &&
                            vSpecialty == null && vPassword == null

                    if (isValid) {
                        onCreate(name, email, phone, password, specialty)
                        onDismiss()
                    }
                }
            ) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun DoctorListItem(
    doctorWithSchedules: DoctorWithSchedules,
    onEditSchedules: () -> Unit,
    onDeleteProfile: () -> Unit
) {
    val doctor = doctorWithSchedules.doctor
    val schedules = doctorWithSchedules.schedules

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(doctor.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Especialidad: ${doctor.specialty}", style = MaterialTheme.typography.bodyMedium)
                Text("Email: ${doctor.email}", style = MaterialTheme.typography.bodySmall)

                Spacer(modifier = Modifier.height(8.dp))
                Text("Horarios:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                if (schedules.isEmpty()) {
                    Text("Sin horarios asignados", style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic)
                } else {
                    schedules.forEach {
                        Text(
                            "• ${it.dayOfWeek}: ${it.startTime} - ${it.endTime}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            Column {
                IconButton(onClick = onEditSchedules) {
                    Icon(Icons.Default.EditCalendar, contentDescription = "Editar Horarios")
                }
                IconButton(onClick = onDeleteProfile) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar Perfil", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

// --- NUEVO: Diálogo para gestionar horarios ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageScheduleDialog(
    doctorWithSchedules: DoctorWithSchedules,
    onDismiss: () -> Unit,
    onConfirm: (doctorId: Long, newSchedules: List<DoctorScheduleEntity>) -> Unit
) {
    // Lista mutable interna para gestionar los cambios
    val schedules = remember { mutableStateListOf<DoctorScheduleEntity>().apply {
        addAll(doctorWithSchedules.schedules)
    }}

    // Estado para los inputs de nuevo horario
    var selectedDay by remember { mutableStateOf("Lunes") }
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("18:00") }
    var dayDropdownExpanded by remember { mutableStateOf(false) }
    val daysOfWeek = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gestionar Horarios de ${doctorWithSchedules.doctor.name}") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // --- Sección para añadir nuevos horarios ---
                Text("Añadir nuevo horario:", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = dayDropdownExpanded,
                    onExpandedChange = { dayDropdownExpanded = !dayDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedDay,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Día") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dayDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = dayDropdownExpanded,
                        onDismissRequest = { dayDropdownExpanded = false }
                    ) {
                        daysOfWeek.forEach { day ->
                            DropdownMenuItem(
                                text = { Text(day) },
                                onClick = {
                                    selectedDay = day
                                    dayDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Inicio (HH:mm)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("Fin (HH:mm)") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Button(
                    onClick = {
                        // Añade el nuevo horario a la lista local
                        schedules.add(
                            DoctorScheduleEntity(
                                doctorId = doctorWithSchedules.doctor.id,
                                dayOfWeek = selectedDay,
                                startTime = startTime,
                                endTime = endTime
                            )
                        )
                        // Resetear inputs (opcional)
                        startTime = "09:00"
                        endTime = "18:00"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Añadir Horario")
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // --- Sección para ver horarios actuales (y eliminarlos) ---
                Text("Horarios actuales:", style = MaterialTheme.typography.titleSmall)
                LazyColumn {
                    items(schedules) { schedule ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "• ${schedule.dayOfWeek}: ${schedule.startTime} - ${schedule.endTime}",
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { schedules.remove(schedule) }) {
                                Icon(
                                    imageVector = Icons.Outlined.RemoveCircleOutline,
                                    contentDescription = "Quitar",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(doctorWithSchedules.doctor.id, schedules) }) {
                Text("Guardar Cambios")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun DeleteConfirmationDialog(
    title: String,
    text: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun ChangeRoleDialog(
    user: UserEntity,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val roles = listOf("USER", "DOCTOR", "ADMIN")
    var selectedRole by remember { mutableStateOf(user.rol) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cambiar Rol de ${user.name}") },
        text = {
            Column {
                roles.forEach { role ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (role == selectedRole),
                                onClick = { selectedRole = role }
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (role == selectedRole),
                            onClick = { selectedRole = role }
                        )
                        Text(
                            text = role,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedRole) }) {
                Text("Actualizar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}