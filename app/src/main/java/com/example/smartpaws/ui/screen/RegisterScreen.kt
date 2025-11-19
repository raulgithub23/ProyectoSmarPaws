package com.example.smartpaws.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.smartpaws.R
import com.example.smartpaws.ui.theme.DarkGreen
import com.example.smartpaws.viewmodel.AuthViewModel

@Composable
fun RegisterScreenVm(
    vm: AuthViewModel,
    onRegisteredNavigateLogin: () -> Unit,
    onGoLogin: () -> Unit
) {
    val state by vm.register.collectAsStateWithLifecycle()

    if (state.success) {
        vm.clearRegisterResult()
        onRegisteredNavigateLogin()
    }

    RegisterScreen(
        name = state.name,
        email = state.email,
        phone = state.phone,
        pass = state.pass,
        confirm = state.confirm,
        nameError = state.nameError,
        emailError = state.emailError,
        phoneError = state.phoneError,
        passError = state.passError,
        confirmError = state.confirmError,
        canSubmit = state.canSubmit,
        isSubmitting = state.isSubmitting,
        errorMsg = state.errorMsg,
        onNameChange = vm::onNameChange,
        onEmailChange = vm::onRegisterEmailChange,
        onPhoneChange = vm::onPhoneChange,
        onPassChange = vm::onRegisterPassChange,
        onConfirmChange = vm::onConfirmChange,
        onSubmit = vm::submitRegister,
        onGoLogin = onGoLogin
    )
}

@Composable
private fun RegisterScreen(
    modifier: Modifier = Modifier,
    name: String,
    email: String,
    phone: String,
    pass: String,
    confirm: String,
    nameError: String?,
    emailError: String?,
    phoneError: String?,
    passError: String?,
    confirmError: String?,
    canSubmit: Boolean,
    isSubmitting: Boolean,
    errorMsg: String?,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onPassChange: (String) -> Unit,
    onConfirmChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onGoLogin: () -> Unit
) {
    val bg = DarkGreen
    var showPass by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.logoblanco),
                contentDescription = "Imagén del logo",
                modifier = Modifier
                    .size(130.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Text(
                color = Color.White,
                text = "Registro Usuario",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.height(12.dp))

            // ---------- NOMBRE ----------
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Nombre", color = Color.White.copy(alpha = 0.8f)) },
                singleLine = true,
                isError = nameError != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
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
            if (nameError != null) {
                Text(
                    nameError,
                    color = Color(0xFFFFCDD2),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            // ---------- EMAIL ----------
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
                    emailError,
                    color = Color(0xFFFFCDD2),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            // ---------- TELÉFONO ----------
            OutlinedTextField(
                value = phone,
                onValueChange = onPhoneChange,
                label = { Text("Teléfono", color = Color.White.copy(alpha = 0.8f)) },
                singleLine = true,
                isError = phoneError != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
            if (phoneError != null) {
                Text(
                    phoneError,
                    color = Color(0xFFFFCDD2),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            // ---------- PASSWORD ----------
            OutlinedTextField(
                value = pass,
                onValueChange = onPassChange,
                label = { Text("Contraseña", color = Color.White.copy(alpha = 0.8f)) },
                singleLine = true,
                isError = passError != null,
                visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPass = !showPass }) {
                        Icon(
                            imageVector = if (showPass) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (showPass) "Ocultar contraseña" else "Mostrar contraseña",
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
                    errorBorderColor = Color(0xFFFFCDD2),
                    errorCursorColor = Color(0xFFFFCDD2),
                    errorTextColor = Color.White
                )
            )
            if (passError != null) {
                Text(
                    passError,
                    color = Color(0xFFFFCDD2),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            // ---------- CONFIRMAR PASSWORD ----------
            OutlinedTextField(
                value = confirm,
                onValueChange = onConfirmChange,
                label = { Text("Confirmar contraseña", color = Color.White.copy(alpha = 0.8f)) },
                singleLine = true,
                isError = confirmError != null,
                visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showConfirm = !showConfirm }) {
                        Icon(
                            imageVector = if (showConfirm) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (showConfirm) "Ocultar confirmación" else "Mostrar confirmación",
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
                    errorBorderColor = Color(0xFFFFCDD2),
                    errorCursorColor = Color(0xFFFFCDD2),
                    errorTextColor = Color.White
                )
            )
            if (confirmError != null) {
                Text(
                    confirmError,
                    color = Color(0xFFFFCDD2),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            // ---------- BOTÓN REGISTRAR ----------
            Button(
                onClick = onSubmit,
                enabled = canSubmit && !isSubmitting,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = DarkGreen,
                    disabledContainerColor = Color.Gray,
                    disabledContentColor = Color.LightGray
                )
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp),
                        color = DarkGreen
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Creando cuenta...")
                } else {
                    Text("Registrar")
                }
            }

            if (errorMsg != null) {
                Spacer(Modifier.height(8.dp))
                Text(errorMsg, color = Color(0xFFFFCDD2))
            }

            Spacer(Modifier.height(12.dp))

            // ---------- BOTÓN IR A LOGIN ----------
            OutlinedButton(
                onClick = onGoLogin,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ir a inicio de sesión", color = Color.White)
            }
        }
    }
}