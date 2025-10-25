package com.example.smartpaws.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartpaws.data.local.doctors.DoctorEntity
import com.example.smartpaws.data.local.doctors.DoctorScheduleEntity
import com.example.smartpaws.data.local.doctors.DoctorWithSchedules
import com.example.smartpaws.data.local.user.UserEntity
import com.example.smartpaws.data.repository.DoctorRepository
import com.example.smartpaws.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estados UI
data class AdminUiState(
    // Pestaña de Usuarios
    val users: List<UserEntity> = emptyList(),
    val stats: AdminStats = AdminStats(),
    val searchQuery: String = "",
    val selectedRole: String? = null,

    // Pestaña de Doctores
    val doctors: List<DoctorWithSchedules> = emptyList(),
    // Estado general
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
    private val userRepository: UserRepository,
    private val doctorRepository: DoctorRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState

    init {
        loadAllData()
    }

    fun loadAllData() {
        loadUsers()
        loadDoctors()
    }

    // Cargar todos los usuarios
    fun loadUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMsg = null) }
            try {
                val users = userRepository.getAllUsers()
                val stats = calculateStats(users)
                _uiState.update {
                    it.copy(
                        users = users,
                        stats = stats,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMsg = "Error al cargar usuarios: ${e.message}"
                    )
                }
            }
        }
    }

    fun loadDoctors() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMsg = null) }
            try {
                val doctors = doctorRepository.getAllDoctorsWithSchedules()
                _uiState.update {
                    it.copy(
                        doctors = doctors,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMsg = "Error al cargar doctores: ${e.message}"
                    )
                }
            }
        }
    }

    fun createDoctor(name: String, email: String, phone: String, pass: String, specialty: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMsg = null, successMsg = null) }

            // 1. Registrar el UserEntity (login)
            val registerResult = userRepository.register(name, email, phone, pass)

            if (registerResult.isFailure) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMsg = registerResult.exceptionOrNull()?.message ?: "Error al registrar usuario"
                    )
                }
                return@launch
            }

            val newUserId = registerResult.getOrNull()
            if (newUserId == null) {
                _uiState.update { it.copy(isLoading = false, errorMsg = "Error al obtener ID de nuevo usuario") }
                return@launch
            }

            // 2. Actualizar rol a "DOCTOR"
            val updateResult = userRepository.updateUserRole(newUserId, "DOCTOR")
            if (updateResult.isFailure) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMsg = updateResult.exceptionOrNull()?.message ?: "Error al asignar rol de Doctor"
                    )
                }
                loadUsers() // Recargar usuarios para mostrar el estado (fallido)
                return@launch
            }

            // 3. Crear el DoctorEntity (perfil)
            val doctorProfileResult = doctorRepository.createDoctorWithSchedules(
                name = name,
                specialty = specialty,
                email = email,
                phone = phone,
                schedules = emptyList() // Se crea con horario vacío
            )

            if (doctorProfileResult.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMsg = "Doctor (Usuario y Perfil) creado correctamente"
                    )
                }
                loadAllData() // Recargar ambas listas
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMsg = doctorProfileResult.exceptionOrNull()?.message ?: "Usuario creado, pero falló al crear perfil de doctor"
                    )
                }
                loadUsers() // Recargar usuarios de todos modos
            }
        }
    }

    // --- NUEVA FUNCIÓN ---
    // Actualizar los horarios de un doctor
    fun updateDoctorSchedules(doctorId: Long, newSchedules: List<DoctorScheduleEntity>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMsg = null, successMsg = null) }
            val result = doctorRepository.updateSchedules(doctorId, newSchedules)

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMsg = "Horario actualizado correctamente"
                    )
                }
                loadDoctors() // Recargar lista de doctores
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMsg = result.exceptionOrNull()?.message ?: "Error al actualizar horario"
                    )
                }
            }
        }
    }

    // --- NUEVA FUNCIÓN ---
    // Eliminar solo el perfil del doctor (no el usuario de login)
    fun deleteDoctorProfile(doctor: DoctorEntity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMsg = null) }
            val result = doctorRepository.deleteDoctor(doctor)

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMsg = "Perfil de doctor eliminado"
                    )
                }
                loadDoctors()
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMsg = result.exceptionOrNull()?.message ?: "Error al eliminar perfil"
                    )
                }
            }
        }
    }

    // Buscar usuarios
    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        if (query.isBlank()) {
            loadUsers()
        } else {
            viewModelScope.launch {
                try {
                    val results = userRepository.searchUsers(query)
                    _uiState.update { it.copy(users = results) }
                } catch (e: Exception) {
                    _uiState.update { it.copy(errorMsg = "Error en la búsqueda") }
                }
            }
        }
    }

    // Filtrar por rol
    fun filterByRole(role: String?) {
        _uiState.update { it.copy(selectedRole = role) }
        viewModelScope.launch {
            try {
                val users = if (role == null) {
                    userRepository.getAllUsers()
                } else {
                    userRepository.getUsersByRole(role)
                }
                _uiState.update { it.copy(users = users) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMsg = "Error al filtrar") }
            }
        }
    }

    // Cambiar rol de usuario
    fun updateUserRole(userId: Long, newRole: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMsg = null, successMsg = null) }
            val result = userRepository.updateUserRole(userId, newRole)

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMsg = "Rol actualizado correctamente"
                    )
                }
                loadUsers() // Recargar lista
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMsg = result.exceptionOrNull()?.message ?: "Error al actualizar rol"
                    )
                }
            }
        }
    }

    // Eliminar usuario
    fun deleteUser(userId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMsg = null) }
            val result = userRepository.deleteUser(userId)

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMsg = "Usuario eliminado"
                    )
                }
                loadUsers()
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMsg = result.exceptionOrNull()?.message ?: "Error al eliminar"
                    )
                }
            }
        }
    }

    // Calcular estadísticas
    private fun calculateStats(users: List<UserEntity>): AdminStats {
        return AdminStats(
            totalUsers = users.size,
            adminCount = users.count { it.rol == "ADMIN" },
            doctorCount = users.count { it.rol == "DOCTOR" },
            userCount = users.count { it.rol == "USER" }
        )
    }

    // Limpiar mensajes
    fun clearMessages() {
        _uiState.update { it.copy(errorMsg = null, successMsg = null) }
    }
}