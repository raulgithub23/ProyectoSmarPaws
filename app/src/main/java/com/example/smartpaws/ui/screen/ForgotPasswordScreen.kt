package com.example.smartpaws.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

@Composable
fun ForgotPasswordScreenVm(
    vm: AuthViewModel,
    onBackToLogin: () -> Unit,
    onSuccessNavigateToLogin: () -> Unit
) {
    val state by vm.forgotPassword.collectAsStateWithLifecycle()

    // Si el reset fue exitoso, navegar a login
    if (state.resetSuccess) {
        vm.clearForgotPasswordResult()
        onSuccessNavigateToLogin()
    }

    ForgotPasswordScreen(
        // Paso 1: Email
        email = state.email,
        emailError = state.emailError,
        canSubmitEmail = state.canSubmitEmail,
        isSubmittingEmail = state.isSubmittingEmail,
        emailSent = state.emailSent,
        emailErrorMsg = state.emailErrorMsg,
        onEmailChange = vm::onForgotPasswordEmailChange,
        onSubmitEmail = vm::submitForgotPassword,

        // Paso 2: Nueva contraseña
        newPassword = state.newPassword,
        confirmPassword = state.confirmPassword,
        passwordError = state.passwordError,
        confirmError = state.confirmError,
        canSubmitReset = state.canSubmitReset,
        isSubmittingReset = state.isSubmittingReset,
        resetErrorMsg = state.resetErrorMsg,
        onNewPasswordChange = vm::onForgotPasswordNewPasswordChange,
        onConfirmPasswordChange = vm::onForgotPasswordConfirmPasswordChange,
        onSubmitReset = vm::submitForgotPasswordReset,

        onBackToLogin = onBackToLogin
    )
}

@Composable
private fun ForgotPasswordScreen(
    // Paso 1: Email
    email: String,
    emailError: String?,
    canSubmitEmail: Boolean,
    isSubmittingEmail: Boolean,
    emailSent: Boolean,
    emailErrorMsg: String?,
    onEmailChange: (String) -> Unit,
    onSubmitEmail: () -> Unit,

    // Paso 2: Nueva contraseña
    newPassword: String,
    confirmPassword: String,
    passwordError: String?,
    confirmError: String?,
    canSubmitReset: Boolean,
    isSubmittingReset: Boolean,
    resetErrorMsg: String?,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSubmitReset: () -> Unit,

    onBackToLogin: () -> Unit
) {
    val bg = DarkGreen
    var showPassword by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .systemBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Image(
                painter = painterResource(R.drawable.logoblanco),
                contentDescription = "Logo Smart Paws",
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 16.dp)
            )

            // Título
            Text(
                text = "Recuperar Contraseña",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            if (!emailSent) {
                // ======== PASO 1: VERIFICAR EMAIL ========
                Text(
                    text = "Ingresa tu email para restablecer tu contraseña",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(Modifier.height(32.dp))

                // Campo Email - CORREGIDO CON COLORES BLANCOS
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
                        errorTextColor = Color.White,
                        focusedLabelColor = Color.White.copy(alpha = 0.8f),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                        errorLabelColor = Color(0xFFFFCDD2)
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

                // Botón Verificar
                Button(
                    onClick = onSubmitEmail,
                    enabled = canSubmitEmail && !isSubmittingEmail,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = DarkGreen,
                        disabledContainerColor = Color.Gray,
                        disabledContentColor = Color.LightGray
                    ),
                    shape = RoundedCornerShape(12.dp)
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
                        Text("Verificar Email", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (emailErrorMsg != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = emailErrorMsg,
                        color = Color(0xFFFFCDD2),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }

            } else {
                // ======== PASO 2: NUEVA CONTRASEÑA ========
                Text(
                    text = "Email verificado correctamente",
                    color = Color(0xFF81C784),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Ingresa tu nueva contraseña",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(32.dp))

                // Campo Nueva Contraseña - CORREGIDO CON COLORES BLANCOS
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = onNewPasswordChange,
                    label = { Text("Nueva contraseña", color = Color.White.copy(alpha = 0.8f)) },
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (showPassword) "Ocultar" else "Mostrar",
                                tint = Color.White
                            )
                        }
                    },
                    isError = passwordError != null,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
                        cursorColor = Color.White,
                        errorBorderColor = Color(0xFFFFCDD2),
                        errorCursorColor = Color(0xFFFFCDD2),
                        errorTextColor = Color.White,
                        focusedLabelColor = Color.White.copy(alpha = 0.8f),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                        errorLabelColor = Color(0xFFFFCDD2)
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

                // Campo Confirmar Contraseña - CORREGIDO CON COLORES BLANCOS
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    label = { Text("Confirmar contraseña", color = Color.White.copy(alpha = 0.8f)) },
                    singleLine = true,
                    visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showConfirm = !showConfirm }) {
                            Icon(
                                imageVector = if (showConfirm) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (showConfirm) "Ocultar" else "Mostrar",
                                tint = Color.White
                            )
                        }
                    },
                    isError = confirmError != null,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
                        cursorColor = Color.White,
                        errorBorderColor = Color(0xFFFFCDD2),
                        errorCursorColor = Color(0xFFFFCDD2),
                        errorTextColor = Color.White,
                        focusedLabelColor = Color.White.copy(alpha = 0.8f),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                        errorLabelColor = Color(0xFFFFCDD2)
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

                // Botón Cambiar Contraseña
                Button(
                    onClick = onSubmitReset,
                    enabled = canSubmitReset && !isSubmittingReset,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = DarkGreen,
                        disabledContainerColor = Color.Gray,
                        disabledContentColor = Color.LightGray
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSubmittingReset) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp),
                            color = DarkGreen
                        )
                        Spacer(Modifier.width(12.dp))
                        Text("Actualizando...", fontSize = 16.sp)
                    } else {
                        Text("Cambiar Contraseña", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (resetErrorMsg != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = resetErrorMsg,
                        color = Color(0xFFFFCDD2),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Botón Volver
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