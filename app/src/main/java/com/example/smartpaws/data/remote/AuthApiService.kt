package com.example.smartpaws.data.remote

import com.example.smartpaws.data.remote.dto.LoginRequest
import com.example.smartpaws.data.remote.dto.RegisterRequest
import com.example.smartpaws.data.remote.dto.UpdateImageRequest
import com.example.smartpaws.data.remote.dto.UpdateProfileRequest
import com.example.smartpaws.data.remote.dto.UpdateRoleRequest
import com.example.smartpaws.data.remote.dto.UserDto
import com.example.smartpaws.data.remote.dto.UserListDto
import retrofit2.http.*

interface AuthApiService {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): UserDto

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): UserDto

    //Obtener usuario por ID
    @GET("auth/user/{id}")
    suspend fun getUserById(@Path("id") userId: Long): UserDto

    //Obtener lista de usuario solo admin
    @GET("auth/users")
    suspend fun getAllUsers(@Query("rol") rol: String): List<UserListDto>
    @GET("auth/users/detailed")
    suspend fun getAllUsersDetailed(@Query("adminRol") adminRol: String): List<UserDto>

    //Buscar usuarios
    @GET("auth/users/search")
    suspend fun searchUsers(
        @Query("query") query: String,
        @Query("adminRol") adminRol: String
    ): List<UserDto>

    //Filtrar usuarios por rol
    @GET("auth/users/by-role")
    suspend fun getUsersByRole(
        @Query("role") role: String,
        @Query("adminRol") adminRol: String
    ): List<UserDto>

    //Actualizar rol de usuario
    @PUT("auth/user/{id}/role")
    suspend fun updateUserRole(
        @Path("id") userId: Long,
        @Body request: UpdateRoleRequest,
        @Query("adminRol") adminRol: String
    ): UserDto

    //Eliminar usuario
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

    @PUT("auth/user/{id}/image")
    suspend fun updateProfileImage(
        @Path("id") userId: Long,
        @Body request: UpdateImageRequest
    ): UserDto

}