package com.example.smartpaws.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartpaws.data.remote.dto.DoctorDto
import com.example.smartpaws.data.remote.dto.ScheduleDto
import com.example.smartpaws.data.remote.dto.UserDto
import com.example.smartpaws.data.repository.DoctorRepository
import com.example.smartpaws.data.repository.UserRepository
import kotlinx.coroutines.async
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
    private val userRepository: UserRepository,
    private val doctorRepository: DoctorRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    private var allUsersCache: List<UserDto> = emptyList()

    init {
        loadAllData()
    }

    fun loadAllData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMsg = null) }

            try {
                // Cargar datos en paralelo
                val usersDeferred = async { userRepository.getAllUsers() }
                val doctorsDeferred = async { doctorRepository.getAllDoctorsWithSchedules() }

                val usersResult = usersDeferred.await()
                val doctors = doctorsDeferred.await()

                usersResult.fold(
                    onSuccess = { users ->
                        allUsersCache = users
                        val stats = calculateStats(users, doctors)

                        _uiState.update {
                            it.copy(
                                users = users,
                                doctors = doctors,
                                stats = stats,
                                isLoading = false
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMsg = "Error al cargar datos: ${error.message}"
                            )
                        }
                    }
                )
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

    private fun calculateStats(users: List<UserDto>, doctors: List<DoctorDto>): AdminStats {
        return AdminStats(
            totalUsers = users.size,
            adminCount = users.count { it.rol == "ADMIN" },
            doctorCount = doctors.size,
            userCount = users.count { it.rol == "USER" }
        )
    }

    // --- FILTROS ---

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }

        if (query.isBlank()) {
            loadAllData()
        } else {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }

                val result = userRepository.searchUsers(query)
                result.fold(
                    onSuccess = { users ->
                        _uiState.update { it.copy(users = users, isLoading = false) }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                errorMsg = "Error en la búsqueda: ${error.message}",
                                isLoading = false
                            )
                        }
                    }
                )
            }
        }
    }

    fun filterByRole(role: String?) {
        _uiState.update { it.copy(selectedRole = role) }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = if (role == null) {
                userRepository.getAllUsers()
            } else {
                userRepository.getUsersByRole(role)
            }

            result.fold(
                onSuccess = { users ->
                    _uiState.update { it.copy(users = users, isLoading = false) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            errorMsg = "Error al filtrar: ${error.message}",
                            isLoading = false
                        )
                    }
                }
            )
        }
    }

    // --- ACCIONES DE DOCTOR ---

    fun createDoctor(name: String, email: String, phone: String, pass: String, specialty: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMsg = null, successMsg = null) }

            // 1. Registrar usuario
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

            // 2. Actualizar rol a DOCTOR
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

            // 3. Crear perfil de doctor
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
                        errorMsg = doctorProfileResult.exceptionOrNull()?.message
                            ?: "Usuario creado, pero falló al crear perfil de doctor"
                    )
                }
            }
            loadAllData()
        }
    }

    fun updateDoctorSchedules(doctorId: Long, newSchedules: List<ScheduleDto>) {
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

    fun deleteDoctorProfile(doctorId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMsg = null) }

            val result = doctorRepository.deleteDoctor(doctorId)

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

    // --- ACCIONES DE USUARIO ---

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
                        errorMsg = result.exceptionOrNull()?.message ?: "Error al eliminar usuario"
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMsg = null, successMsg = null) }
    }
}