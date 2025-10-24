package com.example.smartpaws.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartpaws.data.local.user.UserEntity
import com.example.smartpaws.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estados UI
data class AdminUiState(
    val users: List<UserEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMsg: String? = null,
    val successMsg: String? = null,
    val searchQuery: String = "",
    val selectedRole: String? = null,
    val stats: AdminStats = AdminStats()
)

data class AdminStats(
    val totalUsers: Int = 0,
    val adminCount: Int = 0,
    val doctorCount: Int = 0,
    val userCount: Int = 0
)

class AdminViewModel(
    private val repository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState

    init {
        loadUsers()
    }

    // Cargar todos los usuarios
    fun loadUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMsg = null) }
            try {
                val users = repository.getAllUsers()
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

    // Buscar usuarios
    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        if (query.isBlank()) {
            loadUsers()
        } else {
            viewModelScope.launch {
                try {
                    val results = repository.searchUsers(query)
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
                    repository.getAllUsers()
                } else {
                    repository.getUsersByRole(role)
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
            val result = repository.updateUserRole(userId, newRole)

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
            val result = repository.deleteUser(userId)

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