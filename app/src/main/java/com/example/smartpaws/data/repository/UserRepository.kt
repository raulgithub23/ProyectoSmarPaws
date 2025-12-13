package com.example.smartpaws.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.example.smartpaws.data.remote.AuthApiService
import com.example.smartpaws.data.remote.RemoteModule
import com.example.smartpaws.data.remote.dto.*
import retrofit2.HttpException
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

class UserRepository(
    private val api: AuthApiService = RemoteModule.createAuthService(AuthApiService::class.java),
    private val context: Context
) {

    private fun handleHttpError(e: Exception, defaultMessage: String): String {
        return when (e) {
            is HttpException -> {
                when (e.code()) {
                    400 -> "Datos incorrectos. Por favor verifica la información ingresada"
                    401 -> "Credenciales incorrectas. Email o contraseña inválidos"
                    403 -> "No tienes permisos para realizar esta acción"
                    404 -> "No se encontró el recurso solicitado"
                    405 -> "Método no permitido. Error en la configuración del servidor"
                    409 -> "El email ya está registrado"
                    500 -> "Error del servidor. Intenta más tarde"
                    else -> "Error de conexión: ${e.code()}"
                }
            }
            is IOException -> "Sin conexión a internet. Verifica tu conexión"
            else -> defaultMessage
        }
    }

    suspend fun login(email: String, password: String): Result<UserDto> {
        return try {
            val response = api.login(LoginRequest(email, password))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(
                IllegalArgumentException(handleHttpError(e, "Error al iniciar sesión"))
            )
        }
    }

    suspend fun register(
        name: String,
        email: String,
        phone: String,
        password: String
    ): Result<Long> {
        return try {
            val request = RegisterRequest(
                name = name,
                email = email,
                phone = phone,
                password = password
            )
            val response = api.register(request)
            Result.success(response.id)
        } catch (e: Exception) {
            Result.failure(
                IllegalStateException(handleHttpError(e, "Error al registrar usuario"))
            )
        }
    }

    suspend fun getUserById(userId: Long): Result<UserDto> {
        return try {
            val response = api.getUserById(userId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(
                IllegalArgumentException(handleHttpError(e, "Usuario no encontrado"))
            )
        }
    }

    suspend fun updateUser(userId: Long, name: String, phone: String): Result<UserDto> {
        return try {
            val request = UpdateProfileRequest(name, phone)
            val response = api.updateUserProfile(userId, request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(
                IllegalStateException(handleHttpError(e, "Error al actualizar perfil"))
            )
        }
    }

    suspend fun uploadProfileImage(userId: Long, imageUriOrPath: String): Result<String> {
        return try {
            val bitmap = loadBitmapFromUriOrPath(imageUriOrPath)
                ?: return Result.failure(IllegalStateException("No se pudo cargar la imagen"))

            // Comprimir imagen a un tamaño razonable
            val resizedBitmap = resizeBitmap(bitmap, 800, 800)

            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val imageBytes = outputStream.toByteArray()
            val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

            // Reciclar bitmaps para liberar memoria
            if (bitmap != resizedBitmap) bitmap.recycle()
            resizedBitmap.recycle()

            val request = ImageUploadRequest(
                fileName = "profile_${userId}_${System.currentTimeMillis()}.jpg",
                contentType = "image/jpeg",
                imageBase64 = base64Image
            )

            val response = api.uploadProfileImage(userId, request)

            if (response.success) {
                Result.success(response.message)
            } else {
                Result.failure(IllegalStateException(response.message))
            }
        } catch (e: OutOfMemoryError) {
            Result.failure(
                IllegalStateException("La imagen es demasiado grande. Intenta con una imagen más pequeña.")
            )
        } catch (e: Exception) {
            Result.failure(
                IllegalStateException(handleHttpError(e, "Error al subir imagen: ${e.message}"))
            )
        }
    }

    private fun loadBitmapFromUriOrPath(imageUriOrPath: String): Bitmap? {
        return try {
            // Intentar como URI primero (para galería y cámara)
            val uri = Uri.parse(imageUriOrPath)

            when {
                uri.scheme == "content" || uri.scheme == "file" -> {
                    // URI de content provider o file
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        // Primero obtener dimensiones sin cargar la imagen completa
                        val options = BitmapFactory.Options().apply {
                            inJustDecodeBounds = true
                        }
                        BitmapFactory.decodeStream(inputStream, null, options)
                        inputStream.close()

                        // Calcular factor de escala
                        val scale = calculateInSampleSize(options, 1600, 1600)

                        // Ahora cargar la imagen con escala
                        context.contentResolver.openInputStream(uri)?.use { scaledStream ->
                            BitmapFactory.Options().apply {
                                inSampleSize = scale
                                inJustDecodeBounds = false
                            }.let { scaledOptions ->
                                BitmapFactory.decodeStream(scaledStream, null, scaledOptions)
                            }
                        }
                    }
                }
                else -> {
                    // Path de archivo directo
                    val file = File(imageUriOrPath)
                    if (file.exists()) {
                        // Cargar con escala desde archivo
                        val options = BitmapFactory.Options().apply {
                            inJustDecodeBounds = true
                        }
                        BitmapFactory.decodeFile(imageUriOrPath, options)

                        val scale = calculateInSampleSize(options, 1600, 1600)

                        BitmapFactory.Options().apply {
                            inSampleSize = scale
                            inJustDecodeBounds = false
                        }.let { scaledOptions ->
                            BitmapFactory.decodeFile(imageUriOrPath, scaledOptions)
                        }
                    } else {
                        null
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while ((halfHeight / inSampleSize) >= reqHeight &&
                (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }

        val aspectRatio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int

        if (width > height) {
            newWidth = maxWidth
            newHeight = (maxWidth / aspectRatio).toInt()
        } else {
            newHeight = maxHeight
            newWidth = (maxHeight * aspectRatio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    suspend fun getProfileImage(userId: Long): Result<String?> {
        return try {
            val response = api.getProfileImage(userId)
            if (response.success && response.imageBase64 != null) {
                Result.success(response.imageBase64)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            if (e is HttpException && e.code() == 404) {
                Result.success(null)
            } else {
                Result.failure(
                    IllegalStateException(handleHttpError(e, "Error al obtener imagen"))
                )
            }
        }
    }

    suspend fun deleteProfileImage(userId: Long): Result<String> {
        return try {
            val response = api.deleteProfileImage(userId)
            if (response.success) {
                Result.success(response.message)
            } else {
                Result.failure(IllegalStateException(response.message))
            }
        } catch (e: Exception) {
            Result.failure(
                IllegalStateException(handleHttpError(e, "Error al eliminar imagen"))
            )
        }
    }

    suspend fun requestPasswordReset(email: String): Result<String> {
        return try {
            val request = ForgotPasswordRequest(email)
            val response = api.forgotPassword(request)
            if (response.success) {
                Result.success(response.message)
            } else {
                Result.failure(IllegalStateException(response.message))
            }
        } catch (e: Exception) {
            Result.failure(
                IllegalStateException(handleHttpError(e, "Error al verificar email"))
            )
        }
    }

    suspend fun resetPasswordByEmail(email: String, newPassword: String): Result<String> {
        return try {
            val request = ResetPasswordByEmailRequest(email, newPassword)
            val response = api.resetPasswordByEmail(request)
            if (response.success) {
                Result.success(response.message)
            } else {
                Result.failure(IllegalStateException(response.message))
            }
        } catch (e: Exception) {
            Result.failure(
                IllegalStateException(handleHttpError(e, "Error al restablecer contraseña"))
            )
        }
    }

    // ==================== MÉTODOS PARA ADMIN ====================

    suspend fun getAllUsers(): Result<List<UserDto>> {
        return try {
            val response = api.getAllUsersDetailed(adminRol = "ADMIN")
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(
                IllegalStateException(handleHttpError(e, "Error al obtener usuarios"))
            )
        }
    }

    suspend fun searchUsers(query: String): Result<List<UserDto>> {
        return try {
            val response = api.searchUsers(query = query, adminRol = "ADMIN")
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(
                IllegalStateException(handleHttpError(e, "Error en la búsqueda"))
            )
        }
    }

    suspend fun getUsersByRole(role: String): Result<List<UserDto>> {
        return try {
            val response = api.getUsersByRole(role = role, adminRol = "ADMIN")
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(
                IllegalStateException(handleHttpError(e, "Error al filtrar por rol"))
            )
        }
    }

    suspend fun updateUserRole(userId: Long, newRole: String): Result<UserDto> {
        return try {
            val request = UpdateRoleRequest(newRole)
            val response = api.updateUserRole(userId, request, adminRol = "ADMIN")
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(
                IllegalStateException(handleHttpError(e, "Error al actualizar rol"))
            )
        }
    }

    suspend fun deleteUser(userId: Long): Result<Unit> {
        return try {
            api.deleteUser(userId, adminRol = "ADMIN")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(
                IllegalStateException(handleHttpError(e, "Error al eliminar usuario"))
            )
        }
    }
}