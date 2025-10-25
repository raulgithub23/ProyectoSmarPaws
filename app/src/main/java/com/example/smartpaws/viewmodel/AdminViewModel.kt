package com.example.smartpaws.viewmodel // O tu paquete de ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartpaws.data.local.doctors.DoctorEntity
import com.example.smartpaws.data.local.doctors.DoctorScheduleEntity
import com.example.smartpaws.data.local.user.UserEntity
import com.example.smartpaws.data.repository.DoctorRepository
import com.example.smartpaws.data.repository.UserRepository
import com.example.smartpaws.data.local.doctors.DoctorWithSchedules
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMsg = null) }
            try {
                // Cargar ambas listas en paralelo
                val usersAsync = async { userRepository.getAllUsers() }
                val doctorsAsync = async { doctorRepository.getAllDoctorsWithSchedules() }

                val users = usersAsync.await()
                val doctors = doctorsAsync.await()

                val stats = calculateStats(users, doctors.size)

                _uiState.update {
                    it.copy(
                        users = users,
                        doctors = doctors,
                        stats = stats,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMsg = "Error al cargar datos: ${e.message}"
                    )
                }
            }
        }
    }

    fun createDoctor(name: String, email: String, phone: String, pass: String, specialty: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMsg = null, successMsg = null) }

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

            val updateResult = userRepository.updateUserRole(newUserId, "DOCTOR")
            if (updateResult.isFailure) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMsg = updateResult.exceptionOrNull()?.message ?: "Error al asignar rol de Doctor"
                    )
                }
                loadAllData()
                return@launch
            }

            val doctorProfileResult = doctorRepository.createDoctorWithSchedules(
                name = name,
                specialty = specialty,
                email = email,
                phone = phone,
                schedules = emptyList()
            )

            if (doctorProfileResult.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMsg = "Doctor (Usuario y Perfil) creado correctamente"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMsg = doctorProfileResult.exceptionOrNull()?.message ?: "Usuario creado, pero falló al crear perfil de doctor"
                    )
                }
            }
            loadAllData()
        }
    }

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
                loadAllData()
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
                loadAllData()
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

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        if (query.isBlank()) {
            loadAllData()
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
                loadAllData()
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
                loadAllData()
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

    private fun calculateStats(users: List<UserEntity>, doctorProfileCount: Int): AdminStats {
        return AdminStats(
            totalUsers = users.size,
            adminCount = users.count { it.rol == "ADMIN" },
            doctorCount = doctorProfileCount,
            userCount = users.count { it.rol == "USER" }
        )
    }

    // --- Limpiar mensajes
    fun clearMessages() {
        _uiState.update { it.copy(errorMsg = null, successMsg = null) }
    }
}