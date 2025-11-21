package com.example.smartpaws.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartpaws.data.remote.dto.DoctorDto
import com.example.smartpaws.data.remote.dto.ScheduleDto
import com.example.smartpaws.data.remote.dto.UserDto
import com.example.smartpaws.data.repository.DoctorRepository
import com.example.smartpaws.data.repository.UserRepository
// import com.example.smartpaws.data.repository.UserRepository // Asegúrate de tener este repo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminUiState(
    val users: List<UserDto> = emptyList(),
    val doctors: List<DoctorDto> = emptyList(),
    val stats: AdminStats = AdminStats(),
    val searchQuery: String = "",
    val selectedRole: String? = null,
    val isLoading: Boolean = false,
    val errorMsg: String? = null,
    val successMsg: String? = null
)

data class AdminStats(
    val totalUsers: Int = 0,
    val adminCount: Int = 0,
    val doctorCount: Int = 0,
    val userCount: Int = 0
)

class AdminViewModel(
    private val doctorRepository: DoctorRepository,
    userRepository: UserRepository,
    // private val userRepository: UserRepository // Inyecta tu repo de usuarios aquí
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    // Cache local para filtrar sin volver a llamar a la API
    private var allUsersCache: List<UserDto> = emptyList()

    init {
        loadAllData()
    }

    fun loadAllData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // 1. Cargar Doctores (DTOs)
                val doctors = doctorRepository.getAllDoctorsWithSchedules()

                // 2. Cargar Usuarios (Simulado por ahora si no tienes el repo listo)
                // val users = userRepository.getAllUsers()
                val users = emptyList<UserDto>() // TODO: Reemplazar con llamada real

                allUsersCache = users

                // Calcular estadísticas
                val stats = calculateStats(users, doctors)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        doctors = doctors,
                        users = users,
                        stats = stats
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMsg = "Error cargando datos: ${e.message}") }
            }
        }
    }

    private fun calculateStats(users: List<UserDto>, doctors: List<DoctorDto>): AdminStats {
        return AdminStats(
            totalUsers = users.size,
            adminCount = users.count { it.rol == "ADMIN" },
            doctorCount = doctors.size, // O users.count { it.rol == "DOCTOR" }
            userCount = users.count { it.rol == "USER" }
        )
    }

    // --- Filtros ---

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun filterByRole(role: String?) {
        _uiState.update { it.copy(selectedRole = role) }
        applyFilters()
    }

    private fun applyFilters() {
        val query = _uiState.value.searchQuery.lowercase()
        val role = _uiState.value.selectedRole

        val filteredUsers = allUsersCache.filter { user ->
            val matchesSearch = user.name.lowercase().contains(query) || user.email.lowercase().contains(query)
            val matchesRole = role == null || user.rol == role
            matchesSearch && matchesRole
        }

        _uiState.update { it.copy(users = filteredUsers) }
    }

    // --- Acciones de Doctor ---

    fun createDoctor(name: String, email: String, phone: String, pass: String, specialty: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // Lógica para crear doctor usando doctorRepository.createDoctorWithSchedules...
            // Al finalizar, llamar a loadAllData()
            _uiState.update { it.copy(isLoading = false, successMsg = "Doctor creado (Simulado)") }
        }
    }

    fun updateDoctorSchedules(doctorId: Long, newSchedules: List<ScheduleDto>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = doctorRepository.updateSchedules(doctorId, newSchedules)
            result.onSuccess {
                loadAllData()
                _uiState.update { it.copy(successMsg = "Horarios actualizados") }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, errorMsg = e.message) }
            }
        }
    }

    fun deleteDoctorProfile(doctorId: Long) {
        viewModelScope.launch {
            val result = doctorRepository.deleteDoctor(doctorId)
            result.onSuccess {
                loadAllData()
                _uiState.update { it.copy(successMsg = "Perfil eliminado") }
            }
        }
    }

    // --- Acciones de Usuario ---

    fun deleteUser(userId: Long) {
        // Llama a userRepository.deleteUser(userId)
    }

    fun updateUserRole(userId: Long, newRole: String) {
        // Llama a userRepository.updateRole(userId, newRole)
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMsg = null, successMsg = null) }
    }
}