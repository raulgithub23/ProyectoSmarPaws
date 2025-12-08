package com.example.smartpaws.ui.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.smartpaws.R
import com.example.smartpaws.ui.theme.DarkGreen
import com.example.smartpaws.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreenVm(
    vm: AuthViewModel,
    onBackToLogin: () -> Unit,
    onSuccessNavigateToLogin: () -> Unit
) {
    val forgotPasswordState by vm.forgotPassword.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Mostrar Toast cuando se cambie exitosamente la contraseña
    LaunchedEffect(forgotPasswordState.resetSuccess) {
        if (forgotPasswordState.resetSuccess) {
            Toast.makeText(
                context,
                "¡Contraseña cambiada exitosamente!",
                Toast.LENGTH_LONG
            ).show()
            kotlinx.coroutines.delay(1500)
            vm.clearForgotPasswordResult()
            onSuccessNavigateToLogin()
        }
    }

    ForgotPasswordScreen(
        // Estado general
        email = forgotPasswordState.email,
        emailError = forgotPasswordState.emailError,
        emailSubmitEnabled = forgotPasswordState.canSubmitEmail && !forgotPasswordState.isSubmittingEmail,
        isSubmittingEmail = forgotPasswordState.isSubmittingEmail,
        emailSent = forgotPasswordState.emailSent,
        emailErrorMsg = forgotPasswordState.emailErrorMsg,

        // Estado de reset (sin token)
        newPassword = forgotPasswordState.newPassword,
        confirmPassword = forgotPasswordState.confirmPassword,
        passwordError = forgotPasswordState.passwordError,
        confirmError = forgotPasswordState.confirmError,
        resetSubmitEnabled = forgotPasswordState.canSubmitReset && !forgotPasswordState.isSubmittingReset,
        isSubmittingReset = forgotPasswordState.isSubmittingReset,
        resetErrorMsg = forgotPasswordState.resetErrorMsg,

        // Callbacks
        onEmailChange = vm::onForgotPasswordEmailChange,
        onSubmitEmail = vm::submitForgotPassword,
        onNewPasswordChange = vm::onForgotPasswordNewPasswordChange,
        onConfirmPasswordChange = vm::onForgotPasswordConfirmPasswordChange,
        onSubmitReset = vm::submitForgotPasswordReset,
        onBackToLogin = onBackToLogin
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ForgotPasswordScreen(
    // Forgot password fields
    email: String,
    emailError: String?,
    emailSubmitEnabled: Boolean,
    isSubmittingEmail: Boolean,
    emailSent: Boolean,
    emailErrorMsg: String?,

    // Reset password fields (sin token)
    newPassword: String,
    confirmPassword: String,
    passwordError: String?,
    confirmError: String?,
    resetSubmitEnabled: Boolean,
    isSubmittingReset: Boolean,
    resetErrorMsg: String?,

    // Callbacks
    onEmailChange: (String) -> Unit,
    onSubmitEmail: () -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSubmitReset: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val bg = DarkGreen
    var showNewPassword by rememberSaveable { mutableStateOf(false) }
    var showConfirmPassword by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (!emailSent) "Recuperar Contraseña" else "Nueva Contraseña") },
                navigationIcon = {
                    IconButton(onClick = onBackToLogin) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = bg,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.logoblanco),
                    contentDescription = "Logo Smart Paws",
                    modifier = Modifier
                        .size(120.dp)
                        .padding(bottom = 16.dp)
                )

                if (!emailSent) {
                    // ========== PASO 1: SOLICITAR RECUPERACIÓN ==========
                    Text(
                        text = "¿Olvidaste tu contraseña?",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Ingresa tu correo electrónico para recuperar tu contraseña",
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(Modifier.height(32.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = onEmailChange,
                        label = { Text("Email", color = Color.White.copy(alpha = 0.8f)) },
                        singleLine = true,
                        isError = emailError != null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
                            cursorColor = Color.White,
                            errorBorderColor = Color(0xFFFFCDD2),
                            errorCursorColor = Color(0xFFFFCDD2),
                            errorTextColor = Color.White
                        )
                    )
                    if (emailError != null) {
                        Text(
                            text = emailError,
                            color = Color(0xFFFFCDD2),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, top = 4.dp)
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = onSubmitEmail,
                        enabled = emailSubmitEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = DarkGreen
                        )
                    ) {
                        if (isSubmittingEmail) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(24.dp),
                                color = DarkGreen
                            )
                            Spacer(Modifier.width(12.dp))
                            Text("Verificando...", fontSize = 16.sp)
                        } else {
                            Text("Continuar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (emailErrorMsg != null) {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = emailErrorMsg,
                            color = Color(0xFFFFCDD2),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    // ========== PASO 2: INGRESAR SOLO NUEVA CONTRASEÑA ==========
                    Text(
                        text = "Nueva Contraseña",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF4CAF50).copy(alpha = 0.2f)
                        )
                    ) {
                        Text(
                            text = "✓ Email verificado. Ingresa tu nueva contraseña",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // Campo Nueva Contraseña
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = onNewPasswordChange,
                        label = { Text("Nueva Contraseña", color = Color.White.copy(alpha = 0.8f)) },
                        singleLine = true,
                        isError = passwordError != null,
                        visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showNewPassword = !showNewPassword }) {
                                Icon(
                                    imageVector = if (showNewPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (showNewPassword) "Ocultar" else "Mostrar",
                                    tint = Color.White
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
                            cursorColor = Color.White,
                            errorBorderColor = Color(0xFFFFCDD2)
                        )
                    )
                    if (passwordError != null) {
                        Text(
                            text = passwordError,
                            color = Color(0xFFFFCDD2),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, top = 4.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Campo Confirmar Contraseña
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = onConfirmPasswordChange,
                        label = { Text("Confirmar Contraseña", color = Color.White.copy(alpha = 0.8f)) },
                        singleLine = true,
                        isError = confirmError != null,
                        visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                Icon(
                                    imageVector = if (showConfirmPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (showConfirmPassword) "Ocultar" else "Mostrar",
                                    tint = Color.White
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
                            cursorColor = Color.White,
                            errorBorderColor = Color(0xFFFFCDD2)
                        )
                    )
                    if (confirmError != null) {
                        Text(
                            text = confirmError,
                            color = Color(0xFFFFCDD2),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, top = 4.dp)
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = onSubmitReset,
                        enabled = resetSubmitEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = DarkGreen
                        )
                    ) {
                        if (isSubmittingReset) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(24.dp),
                                color = DarkGreen
                            )
                            Spacer(Modifier.width(12.dp))
                            Text("Cambiando...", fontSize = 16.sp)
                        } else {
                            Text("Cambiar Contraseña", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (resetErrorMsg != null) {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = resetErrorMsg,
                            color = Color(0xFFFFCDD2),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    TextButton(onClick = onBackToLogin) {
                        Text(
                            text = "Volver al inicio de sesión",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}