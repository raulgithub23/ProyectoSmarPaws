package com.example.smartpaws.data.remote

import com.example.smartpaws.data.remote.dto.*
import retrofit2.http.*

interface AuthApiService {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): UserDto

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): UserDto

    @GET("auth/user/{id}")
    suspend fun getUserById(@Path("id") userId: Long): UserDto

    @GET("auth/users")
    suspend fun getAllUsers(@Query("rol") rol: String): List<UserListDto>

    @GET("auth/users/detailed")
    suspend fun getAllUsersDetailed(@Query("adminRol") adminRol: String): List<UserDto>

    @GET("auth/users/search")
    suspend fun searchUsers(
        @Query("query") query: String,
        @Query("adminRol") adminRol: String
    ): List<UserDto>

    @GET("auth/users/by-role")
    suspend fun getUsersByRole(
        @Query("role") role: String,
        @Query("adminRol") adminRol: String
    ): List<UserDto>

    @PUT("auth/user/{id}/role")
    suspend fun updateUserRole(
        @Path("id") userId: Long,
        @Body request: UpdateRoleRequest,
        @Query("adminRol") adminRol: String
    ): UserDto

    @DELETE("auth/user/{id}")
    suspend fun deleteUser(
        @Path("id") userId: Long,
        @Query("adminRol") adminRol: String
    )

    @PUT("auth/user/{id}/profile")
    suspend fun updateUserProfile(
        @Path("id") userId: Long,
        @Body request: UpdateProfileRequest
    ): UserDto

    @POST("auth/user/{id}/upload-image")
    suspend fun uploadProfileImage(
        @Path("id") userId: Long,
        @Body request: ImageUploadRequest
    ): ApiResponse

    // NUEVOS ENDPOINTS PARA RECUPERACION DE CONTRASEÑA
    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): ApiResponse

    @GET("auth/user/{id}/image")
    suspend fun getProfileImage(@Path("id") userId: Long): ImageResponse

    @DELETE("auth/user/{id}/image")
    suspend fun deleteProfileImage(@Path("id") userId: Long): ApiResponse

    // Recuperación de contraseña
    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): ApiResponse

    @POST("auth/reset-password-by-email")
    suspend fun resetPasswordByEmail(@Body request: ResetPasswordByEmailRequest): ApiResponse
}
