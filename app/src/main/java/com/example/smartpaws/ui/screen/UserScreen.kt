package com.example.smartpaws.ui.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.smartpaws.R
import com.example.smartpaws.ui.theme.DarkGreen
import com.example.smartpaws.ui.theme.LightBackground
import com.example.smartpaws.ui.theme.LightSecondary
import com.example.smartpaws.viewmodel.AuthViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Función para crear archivo temporal en cache/images/
private fun createImageFile(context: Context): File? { // Retorna el archivo creado o null si hay algun error
    return try {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imagesDir = File(context.cacheDir, "images")
        if (!imagesDir.exists()) imagesDir.mkdirs()
        File(imagesDir, "PROFILE_${timeStamp}.jpg")
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// Función para obtener Uri con FileProvider
private fun getImageUriForFile(context: Context, file: File): Uri {
    val authority = "${context.packageName}.fileprovider"
    return FileProvider.getUriForFile(context, authority, file)
}

@Composable
fun UserScreen(
    authViewModel: AuthViewModel //parametro que se conecta con viewmodel y su logica
) {
    val context = LocalContext.current
    val userProfile by authViewModel.userProfile.collectAsState() //variable que nos trae los datos del viewmodel a medida que cambian

    var showImageDialog by remember { mutableStateOf(false) }  // Estado para mostrar/ocultar el diálogo de selección de imagen
    var pendingCaptureUri by remember { mutableStateOf<Uri?>(null) } // Uri temporal para la captura de la cámara
    var lastImagePath by remember { mutableStateOf<String?>(null) } // Ruta del último archivo de imagen creado
    val imagePath = userProfile?.profileImagePath // Obtiene la ruta de la imagen de perfil del usuario


    //Launcher para la cámara (debe estar ANTES de usarse) se ejecuta despues que tomamos una foto
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->         // Si la foto fue tomada exitosamente, actualiza la imagen de perfil
        if (success && lastImagePath != null) {
            authViewModel.updateProfileImage(lastImagePath!!)
            Toast.makeText(context, "Foto actualizada", Toast.LENGTH_SHORT).show()
        }
        lastImagePath = null
        pendingCaptureUri = null
    }

    // Launcher para solicitar permiso (usa takePictureLauncher)
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Si el permiso fue otorgado, crea el archivo y abre la cámara
            val file = createImageFile(context)
            if (file != null) {
                lastImagePath = file.absolutePath
                val uri = getImageUriForFile(context, file)
                pendingCaptureUri = uri
                takePictureLauncher.launch(uri) // AHORA Si ESTa DISPONIBLE
            } else {
                Toast.makeText(context, "Error al crear archivo", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Si el permiso fue denegado, muestra un mensaje
            Toast.makeText(
                context,
                "Se necesita permiso de cámara para tomar fotos",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Launcher para la galería pd -> no requiere permisos especiales
    val pickGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        // Si se seleccionó una imagen, actualiza el perfil
        if (uri != null) {
            authViewModel.updateProfileImage(uri.toString())
            Toast.makeText(context, "Imagen actualizada", Toast.LENGTH_SHORT).show()
        }
    }

    val bg = LightBackground
    val cardColor = LightSecondary
    val textColor = DarkGreen

    Box(    // Contenedor principal
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
    ) {
        if (userProfile == null) {        // Muestra un indicador de carga si aún no se ha cargado el perfil
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color(0xFF4CA771)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "MI PERFIL",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                // Foto de perfil con boton de camara
                Box(
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Card(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Transparent
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        // Mostrar imagen del usuario o el gato larry xd por defecto
                        if (imagePath != null && imagePath.startsWith("drawable://")) {
                            // Es una imagen por defecto de la BD de nosotros
                            Image(
                                painter = painterResource(R.drawable.larry),
                                contentDescription = "Foto de perfil",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .border(3.dp, textColor, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else if (imagePath != null) {
                            // Es una URI de archivo o galería del propio usuario
                            //Usa AsyncImage de Coil para cargar imágenes desde Uri
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(Uri.parse(imagePath))
                                    .crossfade(true) // Transición suave al cargar
                                    .build(),
                                contentDescription = "Foto de perfil",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .border(3.dp, textColor, CircleShape),
                                contentScale = ContentScale.Crop,
                                error = painterResource(R.drawable.larry) // Imagen de respaldo en caso de error
                            )
                        } else {
                            // Y si no encuentra una imagen en el perfil le muestra larry el gato
                            Image(
                                painter = painterResource(R.drawable.larry),
                                contentDescription = "Foto de perfil",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .border(3.dp, textColor, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    // Botón de cámara flotante que aparaece al lado del perfil
                    Surface(
                        modifier = Modifier
                            .size(36.dp)
                            .clickable { showImageDialog = true },
                        shape = CircleShape,
                        color = Color(0xFF4CA771),
                        shadowElevation = 4.dp
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Cambiar foto",
                            tint = Color.White,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Tarjeta con información del usuario
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = cardColor
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Información Personal",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CA771),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        InfoRow(
                            label = "Nombre:",
                            value = userProfile?.name ?: "N/A"
                        )

                        InfoRow(
                            label = "Correo:",
                            value = userProfile?.email ?: "N/A"
                        )

                        InfoRow(
                            label = "Teléfono:",
                            value = userProfile?.phone ?: "N/A"
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }

    // Diálogo para elegir camara o galería
    if (showImageDialog) {
        AlertDialog(
            onDismissRequest = { showImageDialog = false },
            title = { Text("Cambiar foto de perfil") },
            text = { Text("¿Como quieres seleccionar la imagen?") },
            confirmButton = {
                // Botón para abrir la cámara
                TextButton(onClick = {
                    showImageDialog = false
                    when {
                        ContextCompat.checkSelfPermission(  // VERIFICAR permiso antes de abrir csmara
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED -> {
                            // Ya tiene permiso abrir camara directamente
                            val file = createImageFile(context)
                            if (file != null) {
                                lastImagePath = file.absolutePath
                                val uri = getImageUriForFile(context, file)
                                pendingCaptureUri = uri
                                takePictureLauncher.launch(uri)
                            } else {
                                Toast.makeText(context, "Error al crear archivo", Toast.LENGTH_SHORT).show()
                            }
                        }
                        else -> {
                            // Solicitar permiso
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                }) {
                    Text("Cámara")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImageDialog = false
                    // Abrir galería (no necesita permiso)
                    pickGalleryLauncher.launch("image/*")
                }) {
                    Text("Galería")
                }
            }
        )
    }
}

@Composable
fun InfoRow( // Composable reutilizable para mostrar una fila de informacion su etiqueta y  el valor
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF013237),
            modifier = Modifier.width(100.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = value,
            fontSize = 16.sp,
            color = Color(0xFF333333)
        )
    }
}