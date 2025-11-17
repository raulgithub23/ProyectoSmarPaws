package com.example.smartpaws.ui.screen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.smartpaws.ui.theme.DarkGreen
import com.example.smartpaws.viewmodel.AuthViewModel
import com.example.smartpaws.R

@Composable
fun LoginScreenVm(
    vm: AuthViewModel,
    onLoginOkNavigateHome: () -> Unit,
    onGoRegister: () -> Unit
) {
    val state by vm.login.collectAsStateWithLifecycle()
    val userProfile by vm.userProfile.collectAsState()

    val  context  = LocalContext.current

    if (state.success) {
        Toast.makeText(context, "Bienvenido ${userProfile?.name ?: ""}!", Toast.LENGTH_LONG).show()
        vm.clearLoginResult()
        onLoginOkNavigateHome()
    }

    LoginScreen(
        email = state.email,
        pass = state.pass,
        emailError = state.emailError,
        passError = state.passError,
        canSubmit = state.canSubmit,
        isSubmitting = state.isSubmitting,
        errorMsg = state.errorMsg,
        onEmailChange = vm::onLoginEmailChange,
        onPassChange = vm::onLoginPassChange,
        onSubmit = vm::submitLogin,
        onGoRegister = onGoRegister,
    )
}

@Composable
private fun LoginScreen(
    modifier: Modifier = Modifier,
    email: String, //correo y contra se toma el valor actual
    pass: String,
    emailError: String?, //correo y contra errorms, indica sy hay error y null si no hay error
    passError: String?,
    canSubmit: Boolean,  //nos dice si podemso enviar el formulario
    isSubmitting: Boolean,
    errorMsg: String?,
    onEmailChange: (String) -> Unit, // el onemail y password son callbacks cuando se cambian
    onPassChange: (String) -> Unit,
    onSubmit: () -> Unit, //Submit y registro son callback
    onGoRegister: () -> Unit,
) {
    val bg = DarkGreen
    var showPass by remember { mutableStateOf(false) }
    // Box que ocupa TODA la pantalla, sin padding
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .systemBarsPadding(), // Solo respeta las barras del sistema (status bar, navigation bar)
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp) // Solo padding horizontal para márgenes laterales
                .verticalScroll(rememberScrollState()), // Permite scroll si el teclado aparece
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Image(
                painter = painterResource(R.drawable.logoblanco),
                contentDescription = "Logo Smart Paws",
                modifier = Modifier
                    .size(180.dp)
                    .padding(bottom = 16.dp)
            )

            // Título de bienvenida
            Text(
                text = "¡Bienvenido!",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "SMART PAWS",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Especialistas en brindar servicios a tus mascotas",
                color = Color.White.copy(alpha = 0.9f),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(32.dp))

            // Campo Email
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Email", color = Color.White.copy(alpha = 0.8f)) },
                singleLine = true, // Solo permite una línea de texto
                isError = emailError != null, // Muestra estilo de error si hay mensaje de error
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), // Teclado optimizado para email
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
                    cursorColor = Color.White,
                    errorBorderColor = Color(0xFFFFCDD2),
                    errorCursorColor = Color(0xFFFFCDD2)
                )
            )
            // Muestra el mensaje de error del email si existe si no retorna un null
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

            Spacer(Modifier.height(16.dp))

            // Campo Contraseña
            OutlinedTextField(
                value = pass,
                onValueChange = onPassChange,
                label = { Text("Contraseña", color = Color.White.copy(alpha = 0.8f)) },
                singleLine = true,
                visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(), // Transforma el texto en puntos si showPass es false
                // Icono para mostrar/ocultar contraseña
                trailingIcon = {
                    IconButton(onClick = { showPass = !showPass }) {
                        Icon(
                            imageVector = if (showPass) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (showPass) "Ocultar contraseña" else "Mostrar contraseña",
                            tint = Color.White
                        )
                    }
                },
                isError = passError != null,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
                    cursorColor = Color.White,
                    errorBorderColor = Color(0xFFFFCDD2),
                    errorCursorColor = Color(0xFFFFCDD2)
                )
            )
            // Muestra el mensaje de error de la contraseña si existe si no retonar un nulo
            if (passError != null) {
                Text(
                    text = passError,
                    color = Color(0xFFFFCDD2),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            // Botón Entrar
            Button(
                onClick = { onSubmit() },
                enabled = canSubmit && !isSubmitting, // Solo habilitado si puede enviar y no está procesando
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
                // nos muestra un indicador de carga mientras se está enviando
                if (isSubmitting) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp),
                        color = DarkGreen
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("Validando...", fontSize = 16.sp)
                } else {
                    Text("Entrar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Mensaje de error global
            if (errorMsg != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = errorMsg,
                    color = Color(0xFFFFCDD2),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(24.dp))

            // Texto y botón de registro
            Text(
                text = "¿No tienes cuenta?",
                color = Color.White.copy(alpha = 0.9f),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = onGoRegister,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                ),
                border = BorderStroke(1.5.dp, Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Crear cuenta", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

