package com.example.smartpaws.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartpaws.data.local.storage.UserPreferences
import com.example.smartpaws.data.remote.dto.UserDto
import com.example.smartpaws.data.repository.UserRepository
import com.example.smartpaws.domain.validation.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val userId: Long? = null,
    val email: String = "",
    val pass: String = "",
    val emailError: String? = null,
    val passError: String? = null,
    val isSubmitting: Boolean = false,
    val canSubmit: Boolean = false,
    val success: Boolean = false,
    val errorMsg: String? = null
)

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val pass: String = "",
    val confirm: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val passError: String? = null,
    val confirmError: String? = null,
    val isSubmitting: Boolean = false,
    val canSubmit: Boolean = false,
    val success: Boolean = false,
    val errorMsg: String? = null
)

// NUEVO: Estado unificado para recuperación Y cambio de contraseña
data class ForgotPasswordUiState(
    // Paso 1: Validar email
    val email: String = "",
    val emailError: String? = null,
    val isSubmittingEmail: Boolean = false,
    val canSubmitEmail: Boolean = false,
    val emailSent: Boolean = false,
    val emailErrorMsg: String? = null,

    // Paso 2: Nueva contraseña (sin token visible)
    val newPassword: String = "",
    val confirmPassword: String = "",
    val passwordError: String? = null,
    val confirmError: String? = null,
    val isSubmittingReset: Boolean = false,
    val canSubmitReset: Boolean = false,
    val resetSuccess: Boolean = false,
    val resetErrorMsg: String? = null,

    // Token interno (no lo ve el usuario)
    val internalToken: String = ""
)

class AuthViewModel(
    private val repository: UserRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _isLoadingSession = MutableStateFlow(true)
    val isLoadingSession: StateFlow<Boolean> = _isLoadingSession

    private val _login = MutableStateFlow(LoginUiState())
    val login: StateFlow<LoginUiState> = _login

    private val _register = MutableStateFlow(RegisterUiState())
    val register: StateFlow<RegisterUiState> = _register

    private val _userProfile = MutableStateFlow<UserDto?>(null)
    val userProfile: StateFlow<UserDto?> = _userProfile

    // Estado unificado de recuperación de contraseña
    private val _forgotPassword = MutableStateFlow(ForgotPasswordUiState())
    val forgotPassword: StateFlow<ForgotPasswordUiState> = _forgotPassword

    init {
        checkActiveSession()
    }

    private fun checkActiveSession() {
        viewModelScope.launch {
            val userId = userPreferences.loggedInUserId.firstOrNull()

            if (userId != null) {
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

    // ----------------- LOGIN -----------------

    fun onLoginEmailChange(value: String) {
        _login.update { it.copy(email = value, emailError = validateEmail(value)) }
        recomputeLoginCanSubmit()
    }

    fun onLoginPassChange(value: String) {
        _login.update { it.copy(pass = value) }
        recomputeLoginCanSubmit()
    }

    private fun recomputeLoginCanSubmit() {
        val s = _login.value
        val can = s.emailError == null &&
                s.email.isNotBlank() &&
                s.pass.isNotBlank()
        _login.update { it.copy(canSubmit = can) }
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
                        viewModelScope.launch {
                            userPreferences.saveUserId(user.id)
                        }
                        loadUserProfile(user.id)
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

    private fun loadUserProfile(userId: Long) {
        viewModelScope.launch {
            val result = repository.getUserById(userId)
            result.fold(
                onSuccess = { userDto ->
                    _userProfile.value = userDto
                },
                onFailure = {
                    _userProfile.value = null
                    userPreferences.clearSession()
                }
            )
        }
    }

    fun updateUserProfile(name: String, phone: String) {
        viewModelScope.launch {
            val currentUser = _userProfile.value
            if (currentUser != null) {
                val result = repository.updateUser(currentUser.id, name, phone)
                result.onSuccess { updatedUser ->
                    _userProfile.value = updatedUser
                }
                result.onFailure {
                    loadUserProfile(currentUser.id)
                }
            }
        }
    }

    fun updateProfileImage(imagePath: String) {
        viewModelScope.launch {
            val currentUser = _userProfile.value
            if (currentUser != null) {
                val result = repository.updateProfileImage(currentUser.id, imagePath)
                result.onSuccess { updatedUser ->
                    _userProfile.value = updatedUser
                }
                result.onFailure {
                    loadUserProfile(currentUser.id)
                }
            }
        }
    }

    fun clearLoginResult() {
        _login.update { it.copy(success = false, errorMsg = null) }
    }

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
        _userProfile.value = null
    }

    // ----------------- REGISTRO -----------------

    fun onNameChange(value: String) {
        val filtered = value.filter { it.isLetter() || it.isWhitespace() }
        _register.update {
            it.copy(name = filtered, nameError = validateNameLettersOnly(filtered))
        }
        recomputeRegisterCanSubmit()
    }

    fun onRegisterEmailChange(value: String) {
        _register.update { it.copy(email = value, emailError = validateEmail(value)) }
        recomputeRegisterCanSubmit()
    }

    fun onPhoneChange(value: String) {
        val digitsOnly = value.filter { it.isDigit() }
        _register.update {
            it.copy(phone = digitsOnly, phoneError = validatePhoneDigitsOnly(digitsOnly))
        }
        recomputeRegisterCanSubmit()
    }

    fun onRegisterPassChange(value: String) {
        _register.update { it.copy(pass = value, passError = validateStrongPassword(value)) }
        _register.update { it.copy(confirmError = validateConfirm(it.pass, it.confirm)) }
        recomputeRegisterCanSubmit()
    }

    fun onConfirmChange(value: String) {
        _register.update { it.copy(confirm = value, confirmError = validateConfirm(it.pass, value)) }
        recomputeRegisterCanSubmit()
    }

    private fun recomputeRegisterCanSubmit() {
        val s = _register.value
        val noErrors = listOf(s.nameError, s.emailError, s.phoneError, s.passError, s.confirmError).all { it == null }
        val filled = s.name.isNotBlank() && s.email.isNotBlank() && s.phone.isNotBlank() && s.pass.isNotBlank() && s.confirm.isNotBlank()
        _register.update { it.copy(canSubmit = noErrors && filled) }
    }

    fun submitRegister() {
        val s = _register.value
        if (!s.canSubmit || s.isSubmitting) return

        viewModelScope.launch {
            _register.update { it.copy(isSubmitting = true, errorMsg = null, success = false) }
            delay(700)

            val result = repository.register(
                name = s.name.trim(),
                email = s.email.trim(),
                phone = s.phone.trim(),
                password = s.pass
            )

            _register.update {
                if (result.isSuccess) {
                    it.copy(isSubmitting = false, success = true, errorMsg = null)
                } else {
                    it.copy(
                        isSubmitting = false,
                        success = false,
                        errorMsg = result.exceptionOrNull()?.message ?: "No se pudo registrar"
                    )
                }
            }
        }
    }

    fun clearRegisterResult() {
        _register.update { it.copy(success = false, errorMsg = null) }
    }

    // ----------------- RECUPERACION DE CONTRASEÑA (PASO 1: EMAIL) -----------------

    fun onForgotPasswordEmailChange(value: String) {
        _forgotPassword.update {
            it.copy(email = value, emailError = validateEmail(value))
        }
        recomputeForgotPasswordCanSubmitEmail()
    }

    private fun recomputeForgotPasswordCanSubmitEmail() {
        val s = _forgotPassword.value
        val can = s.emailError == null && s.email.isNotBlank()
        _forgotPassword.update { it.copy(canSubmitEmail = can) }
    }

    fun submitForgotPassword() {
        val s = _forgotPassword.value
        if (!s.canSubmitEmail || s.isSubmittingEmail) return

        viewModelScope.launch {
            _forgotPassword.update {
                it.copy(isSubmittingEmail = true, emailErrorMsg = null, emailSent = false)
            }
            delay(500)

            val result = repository.requestPasswordReset(s.email.trim())

            _forgotPassword.update {
                if (result.isSuccess) {
                    // Extraemos el token de la respuesta (si el backend lo retorna)
                    // En este caso, asumimos que el mensaje contiene el token o lo guardamos internamente
                    it.copy(
                        isSubmittingEmail = false,
                        emailSent = true,
                        emailErrorMsg = null,
                        internalToken = extractTokenFromMessage(result.getOrNull() ?: "")
                    )
                } else {
                    it.copy(
                        isSubmittingEmail = false,
                        emailSent = false,
                        emailErrorMsg = result.exceptionOrNull()?.message ?: "Error al verificar email"
                    )
                }
            }
        }
    }

    // Función auxiliar para extraer token (si el backend lo envía en el mensaje)
    // Si no, puedes modificar el backend para que retorne el token directamente
    private fun extractTokenFromMessage(message: String): String {
        // Por ahora, retornamos un string vacío
        // Deberás modificar tu backend para que retorne el token en la respuesta
        return ""
    }

    // ----------------- RECUPERACION DE CONTRASEÑA (PASO 2: NUEVA CONTRASEÑA) -----------------

    fun onForgotPasswordNewPasswordChange(value: String) {
        _forgotPassword.update {
            it.copy(
                newPassword = value,
                passwordError = validateStrongPassword(value)
            )
        }
        _forgotPassword.update {
            it.copy(confirmError = validateConfirm(it.newPassword, it.confirmPassword))
        }
        recomputeForgotPasswordCanSubmitReset()
    }

    fun onForgotPasswordConfirmPasswordChange(value: String) {
        _forgotPassword.update {
            it.copy(
                confirmPassword = value,
                confirmError = validateConfirm(it.newPassword, value)
            )
        }
        recomputeForgotPasswordCanSubmitReset()
    }

    private fun recomputeForgotPasswordCanSubmitReset() {
        val s = _forgotPassword.value
        val noErrors = s.passwordError == null && s.confirmError == null
        val filled = s.newPassword.isNotBlank() && s.confirmPassword.isNotBlank()
        _forgotPassword.update { it.copy(canSubmitReset = noErrors && filled) }
    }

    fun submitForgotPasswordReset() {
        val s = _forgotPassword.value
        if (!s.canSubmitReset || s.isSubmittingReset) return

        viewModelScope.launch {
            _forgotPassword.update {
                it.copy(isSubmittingReset = true, resetErrorMsg = null, resetSuccess = false)
            }
            delay(500)

            // Usamos el email en lugar del token para el reset
            // Deberás modificar tu backend para aceptar email en lugar de token
            val result = repository.resetPasswordByEmail(s.email.trim(), s.newPassword)

            _forgotPassword.update {
                if (result.isSuccess) {
                    it.copy(
                        isSubmittingReset = false,
                        resetSuccess = true,
                        resetErrorMsg = null
                    )
                } else {
                    it.copy(
                        isSubmittingReset = false,
                        resetSuccess = false,
                        resetErrorMsg = result.exceptionOrNull()?.message ?: "Error al cambiar contraseña"
                    )
                }
            }
        }
    }

    fun clearForgotPasswordResult() {
        _forgotPassword.value = ForgotPasswordUiState()
    }
}