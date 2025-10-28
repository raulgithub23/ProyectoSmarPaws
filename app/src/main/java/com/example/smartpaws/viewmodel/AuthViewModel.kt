package com.example.smartpaws.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartpaws.data.local.storage.UserPreferences
import com.example.smartpaws.data.local.user.UserEntity
import com.example.smartpaws.data.repository.UserRepository
import com.example.smartpaws.domain.validation.validateConfirm
import com.example.smartpaws.domain.validation.validateEmail
import com.example.smartpaws.domain.validation.validateNameLettersOnly
import com.example.smartpaws.domain.validation.validatePhoneDigitsOnly
import com.example.smartpaws.domain.validation.validateStrongPassword
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


// Data class que representa el estado completo de la pantalla de Login

data class LoginUiState(                                   // Estado de la pantalla Login
    val userId: Long? = null,
    val email: String = "",                                // Campo email
    val pass: String = "",                                 // Campo contraseña (texto)
    val emailError: String? = null,                        // Error de email
    val passError: String? = null,                         // (Opcional) error de pass en login
    val isSubmitting: Boolean = false,                     // Flag de carga
    val canSubmit: Boolean = false,                        // Habilitar botón
    val success: Boolean = false,                          // Resultado OK
    val errorMsg: String? = null                           // Error global (credenciales inválidas)
)

// Data class que representa el estado completo de la pantalla de Registro

data class RegisterUiState(                                // Estado de la pantalla Registro (<= 5 campos)
    val name: String = "",                                 // 1) Nombre
    val email: String = "",                                // 2) Email
    val phone: String = "",                                // 3) Teléfono
    val pass: String = "",                                 // 4) Contraseña
    val confirm: String = "",                              // 5) Confirmación

    val nameError: String? = null,                         // Errores por campo
    val emailError: String? = null,
    val phoneError: String? = null,
    val passError: String? = null,
    val confirmError: String? = null,

    val isSubmitting: Boolean = false,                     // Flag de carga
    val canSubmit: Boolean = false,                        // Habilitar botón
    val success: Boolean = false,                          // Resultado OK
    val errorMsg: String? = null                           // Error global (ej: duplicado)
)

// ----------------- COLECCIÓN EN MEMORIA (solo para la demo) -----------------

//2.- Eliminamos la estructura de DemoUser

class AuthViewModel(
    // NUEVO: 4.- inyectamos el repositorio real que usa Room/SQLite
    private val repository: UserRepository, // Repositorio que accede a la BD de usuarios
    private val userPreferences: UserPreferences  // Almacenamiento local para persistir sesion
) : ViewModel() {                         // ViewModel que maneja Login/Registro

    private val _isLoadingSession = MutableStateFlow(true) // Estado para indicar si está verificando si hay sesión activa
    val isLoadingSession: StateFlow<Boolean> = _isLoadingSession

    // Flujos de estado para observar desde la UI
    private val _login = MutableStateFlow(LoginUiState())   // Estado interno (Login)
    val login: StateFlow<LoginUiState> = _login             // Exposición inmutable

    private val _register = MutableStateFlow(RegisterUiState()) // Estado interno (Registro)

    private val _userProfile = MutableStateFlow<UserEntity?>(null)     // Estado del perfil del usuario logueado
    val userProfile: StateFlow<UserEntity?> = _userProfile
    val register: StateFlow<RegisterUiState> = _register        // Exposición inmutable

    init { // Bloque init: se ejecuta al crear el ViewModel
        checkActiveSession() // Verifica si hay una sesión activa al iniciar la app
    }

    private fun checkActiveSession() {
        viewModelScope.launch {
            //lee el primer valor del Flow (o null) y se detiene
            val userId = userPreferences.loggedInUserId.firstOrNull()

            if (userId != null) {
                // Cargamos su perfil
                try {
                    loadUserProfile(userId)
                    _login.update { it.copy(success = true, userId = userId) }
                } catch (e: Exception) {
                    userPreferences.clearSession()
                }
            }
            _isLoadingSession.value = false
        }
    }

    // ----------------- LOGIN: handlers y envío -----------------

    fun onLoginEmailChange(value: String) {                 // Handler cuando cambia el email
        _login.update { it.copy(email = value, emailError = validateEmail(value)) } // Guardamos + validamos
        recomputeLoginCanSubmit()                           // Recalculamos habilitado
    }

    fun onLoginPassChange(value: String) {                  // Handler cuando cambia la contraseña
        _login.update { it.copy(pass = value) }             // Guardamos (sin validar fuerza aquí)
        recomputeLoginCanSubmit()                           // Recalculamos habilitado
    }

    private fun recomputeLoginCanSubmit() {                 // Regla para habilitar botón "Entrar"
        val s = _login.value                                // Tomamos el estado actual
        val can = s.emailError == null &&                   // Email válido
                s.email.isNotBlank() &&                   // Email no vacío
                s.pass.isNotBlank()                       // Password no vacía
        _login.update { it.copy(canSubmit = can) }          // Actualizamos el flag
    }

    fun submitLogin() {
        val s = _login.value
        if (!s.canSubmit || s.isSubmitting) return
        viewModelScope.launch {
            _login.update { it.copy(isSubmitting = true, errorMsg = null, success = false) }
            delay(500)

            val result = repository.login(s.email.trim(), s.pass)

            _login.update {
                if (result.isSuccess) {
                    val user = result.getOrNull()
                    if (user != null) {
                        // corrutina para no bloquear
                        viewModelScope.launch {
                            userPreferences.saveUserId(user.id)
                        }
                        user.id.let { userId -> loadUserProfile(userId) }
                    }

                    it.copy(
                        isSubmitting = false,
                        success = true,
                        errorMsg = null,
                        userId = user?.id
                    )
                } else {
                    it.copy(
                        isSubmitting = false,
                        success = false,
                        errorMsg = result.exceptionOrNull()?.message ?: "Error de autenticación"
                    )
                }
            }
        }
    }


    /**
     * Carga el perfil completo del usuario desde la BD
     *
     * @param userId: ID del usuario a cargar
     */
    private fun loadUserProfile(userId: Long) {
        viewModelScope.launch {
            try {
                val user = repository.getUserById(userId)
                _userProfile.value = user
            } catch (e: Exception) {
                _userProfile.value = null
            }
        }
    }

    /**
     * Actualiza la imagen de perfil del usuario
     *
     * @param imagePath: Ruta o URI de la nueva imagen
     */
    fun updateProfileImage(imagePath: String) {
        viewModelScope.launch {
            try {
                val currentUser = _userProfile.value
                if (currentUser != null) {
                    val updatedUser = currentUser.copy(profileImagePath = imagePath)
                    repository.updateUser(updatedUser)
                    _userProfile.value = updatedUser
                }
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }
    fun clearLoginResult() {                                // Limpia banderas tras navegar
        _login.update { it.copy(success = false, errorMsg = null) }
    }


    /**
     * Cierra la sesión del usuario
     *
     * ACCIONES:
     * 1. Limpia el userId de UserPreferences
     * 2. Reinicia el estado de login
     * 3. Limpia el perfil del usuario
     */
    fun logout() {
        viewModelScope.launch {
            userPreferences.clearSession()
        }

        _login.value = LoginUiState(
            success = false,
            userId = null,
            email = "",
            pass = "",
            canSubmit = false
        )
        // limpiar el perfil de usuario
        _userProfile.value = null
    }


    // ----------------- REGISTRO: handlers y envío -----------------

    fun onNameChange(value: String) {                       // Handler del nombre
        val filtered = value.filter { it.isLetter() || it.isWhitespace() } // Filtramos números/símbolos (solo letras/espacios)
        _register.update {                                  // Guardamos + validamos
            it.copy(name = filtered, nameError = validateNameLettersOnly(filtered))
        }
        recomputeRegisterCanSubmit()                        // Recalculamos habilitado
    }

    fun onRegisterEmailChange(value: String) {              // Handler del email
        _register.update { it.copy(email = value, emailError = validateEmail(value)) } // Guardamos + validamos
        recomputeRegisterCanSubmit()
    }

    fun onPhoneChange(value: String) {                      // Handler del teléfono
        val digitsOnly = value.filter { it.isDigit() }      // Dejamos solo dígitos
        _register.update {                                  // Guardamos + validamos
            it.copy(phone = digitsOnly, phoneError = validatePhoneDigitsOnly(digitsOnly))
        }
        recomputeRegisterCanSubmit()
    }

    fun onRegisterPassChange(value: String) {               // Handler de la contraseña
        _register.update { it.copy(pass = value, passError = validateStrongPassword(value)) } // Validamos seguridad
        // Revalidamos confirmación con la nueva contraseña
        _register.update { it.copy(confirmError = validateConfirm(it.pass, it.confirm)) }
        recomputeRegisterCanSubmit()
    }

    fun onConfirmChange(value: String) {                    // Handler de confirmación
        _register.update { it.copy(confirm = value, confirmError = validateConfirm(it.pass, value)) } // Guardamos + validamos
        recomputeRegisterCanSubmit()
    }

    private fun recomputeRegisterCanSubmit() {              // Habilitar "Registrar" si todo OK
        val s = _register.value                              // Tomamos el estado actual
        val noErrors = listOf(s.nameError, s.emailError, s.phoneError, s.passError, s.confirmError).all { it == null } // Sin errores
        val filled = s.name.isNotBlank() && s.email.isNotBlank() && s.phone.isNotBlank() && s.pass.isNotBlank() && s.confirm.isNotBlank() // Todo lleno
        _register.update { it.copy(canSubmit = noErrors && filled) } // Actualizamos flag
    }

    fun submitRegister() {                                  // Acción de registro (simulación async)
        val s = _register.value                              // Snapshot del estado
        if (!s.canSubmit || s.isSubmitting) return          // Evitamos reentradas
        viewModelScope.launch {                             // Corrutina
            _register.update { it.copy(isSubmitting = true, errorMsg = null, success = false) } // Loading
            delay(700)                                      // Simulamos IO

            // 7.- Se cambia esto por lo anterior NUEVO: inserta en BD (con teléfono) vía repositorio
            val result = repository.register(
                name = s.name.trim(),
                email = s.email.trim(),
                phone = s.phone.trim(),                     // Incluye teléfono
                pass = s.pass
            )

            // Interpreta resultado y actualiza estado
            _register.update {
                if (result.isSuccess) {
                    it.copy(isSubmitting = false, success = true, errorMsg = null)  // OK
                } else {
                    it.copy(isSubmitting = false, success = false,
                        errorMsg = result.exceptionOrNull()?.message ?: "No se pudo registrar")
                }
            }
        }
    }

    fun clearRegisterResult() {                             // Limpia banderas tras navegar
        _register.update { it.copy(success = false, errorMsg = null) }
    }
}