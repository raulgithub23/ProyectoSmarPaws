package com.example.smartpaws.repositories

import com.example.smartpaws.data.remote.AuthApiService
import com.example.smartpaws.data.remote.dto.LoginRequest
import com.example.smartpaws.data.remote.dto.UpdateRoleRequest
import com.example.smartpaws.data.remote.dto.UserDto
import com.example.smartpaws.data.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UserRepositoryTest {

    private val sampleUserDto = UserDto(
        id = 1L,
        rol = "USER",
        name = "Juan Pérez",
        email = "juan@example.com",
        phone = "123456789",
        profileImagePath = null
    )

    @Test
    fun login_devuelve_user_cuando_credenciales_son_correctas() = runBlocking {
        val api = mockk<AuthApiService>()
        val repo = UserRepository(api)

        coEvery { api.login(any()) } returns sampleUserDto

        val result = repo.login("juan@example.com", "Pass123$")

        assertTrue(result.isSuccess)
        assertEquals("Juan Pérez", result.getOrNull()?.name)
        assertEquals(1L, result.getOrNull()?.id)
        coVerify { api.login(LoginRequest("juan@example.com", "Pass123$")) }
    }

    @Test
    fun login_devuelve_error_cuando_credenciales_incorrectas() = runBlocking {
        val api = mockk<AuthApiService>()
        val repo = UserRepository(api)

        coEvery { api.login(any()) } throws Exception("Credenciales inválidas")

        val result = repo.login("juan@example.com", "wrongpass")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Credenciales inválidas") == true)
    }

    @Test
    fun register_devuelve_id_cuando_registro_exitoso() = runBlocking {
        val api = mockk<AuthApiService>()
        val repo = UserRepository(api)

        // ⭐ CAMBIO: register devuelve UserDto completo
        val newUser = sampleUserDto.copy(id = 2L, name = "Maria López", email = "maria@example.com")

        coEvery { api.register(any()) } returns newUser

        val result = repo.register(
            name = "Maria López",
            email = "maria@example.com",
            phone = "987654321",
            password = "Pass456$"
        )

        assertTrue(result.isSuccess)
        assertEquals(2L, result.getOrNull())
    }

    @Test
    fun register_devuelve_error_cuando_falla() = runBlocking {
        val api = mockk<AuthApiService>()
        val repo = UserRepository(api)

        coEvery { api.register(any()) } throws Exception("Email duplicado")

        val result = repo.register(
            name = "Maria López",
            email = "maria@example.com",
            phone = "987654321",
            password = "Pass456$"
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun getUserById_devuelve_usuario_exitosamente() = runBlocking {
        val api = mockk<AuthApiService>()
        val repo = UserRepository(api)

        coEvery { api.getUserById(1L) } returns sampleUserDto

        val result = repo.getUserById(1L)

        assertTrue(result.isSuccess)
        assertEquals("Juan Pérez", result.getOrNull()?.name)
        assertEquals(1L, result.getOrNull()?.id)
    }

    @Test
    fun getUserById_devuelve_error_cuando_no_existe() = runBlocking {
        val api = mockk<AuthApiService>()
        val repo = UserRepository(api)

        coEvery { api.getUserById(999L) } throws Exception("Usuario no encontrado")

        val result = repo.getUserById(999L)

        assertTrue(result.isFailure)
    }

    @Test
    fun getAllUsers_devuelve_lista_exitosamente() = runBlocking {
        val api = mockk<AuthApiService>()
        val repo = UserRepository(api)

        val userList = listOf(sampleUserDto, sampleUserDto.copy(id = 2L, name = "Pedro"))

        coEvery { api.getAllUsersDetailed("ADMIN") } returns userList

        val result = repo.getAllUsers()

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        assertEquals("Juan Pérez", result.getOrNull()?.get(0)?.name)
        assertEquals("Pedro", result.getOrNull()?.get(1)?.name)
    }

    @Test
    fun getAllUsers_devuelve_error_cuando_falla() = runBlocking {
        val api = mockk<AuthApiService>()
        val repo = UserRepository(api)

        coEvery { api.getAllUsersDetailed("ADMIN") } throws Exception("Error de servidor")

        val result = repo.getAllUsers()

        assertTrue(result.isFailure)
    }

    @Test
    fun searchUsers_devuelve_resultados_exitosamente() = runBlocking {
        val api = mockk<AuthApiService>()
        val repo = UserRepository(api)

        val searchResults = listOf(sampleUserDto)

        coEvery { api.searchUsers("Juan", "ADMIN") } returns searchResults

        val result = repo.searchUsers("Juan")

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("Juan Pérez", result.getOrNull()?.get(0)?.name)
    }

    @Test
    fun searchUsers_devuelve_lista_vacia_sin_resultados() = runBlocking {
        val api = mockk<AuthApiService>()
        val repo = UserRepository(api)

        coEvery { api.searchUsers("NoExiste", "ADMIN") } returns emptyList()

        val result = repo.searchUsers("NoExiste")

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.size)
    }

    @Test
    fun getUsersByRole_devuelve_usuarios_filtrados() = runBlocking {
        val api = mockk<AuthApiService>()
        val repo = UserRepository(api)

        val doctors = listOf(sampleUserDto.copy(rol = "DOCTOR"))

        coEvery { api.getUsersByRole("DOCTOR", "ADMIN") } returns doctors

        val result = repo.getUsersByRole("DOCTOR")

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("DOCTOR", result.getOrNull()?.get(0)?.rol)
    }

    @Test
    fun getUsersByRole_devuelve_error_cuando_falla() = runBlocking {
        val api = mockk<AuthApiService>()
        val repo = UserRepository(api)

        coEvery { api.getUsersByRole("DOCTOR", "ADMIN") } throws Exception("Error de red")

        val result = repo.getUsersByRole("DOCTOR")

        assertTrue(result.isFailure)
    }

    @Test
    fun updateUserRole_actualiza_exitosamente() = runBlocking {
        val api = mockk<AuthApiService>()
        val repo = UserRepository(api)

        // ⭐ CAMBIO: updateUserRole devuelve UserDto
        val updatedUser = sampleUserDto.copy(rol = "DOCTOR")

        coEvery { api.updateUserRole(1L, any(), "ADMIN") } returns updatedUser

        val result = repo.updateUserRole(1L, "DOCTOR")

        assertTrue(result.isSuccess)
        coVerify { api.updateUserRole(1L, UpdateRoleRequest("DOCTOR"), "ADMIN") }
    }

    @Test
    fun updateUserRole_devuelve_error_cuando_falla() = runBlocking {
        val api = mockk<AuthApiService>()
        val repo = UserRepository(api)

        coEvery { api.updateUserRole(1L, any(), "ADMIN") } throws Exception("Usuario no encontrado")

        val result = repo.updateUserRole(1L, "DOCTOR")

        assertTrue(result.isFailure)
    }

    @Test
    fun deleteUser_elimina_exitosamente() = runBlocking {
        val api = mockk<AuthApiService>()
        val repo = UserRepository(api)

        coEvery { api.deleteUser(1L, "ADMIN") } returns Unit

        val result = repo.deleteUser(1L)

        assertTrue(result.isSuccess)
        coVerify { api.deleteUser(1L, "ADMIN") }
    }

    @Test
    fun deleteUser_devuelve_error_cuando_falla() = runBlocking {
        val api = mockk<AuthApiService>()
        val repo = UserRepository(api)

        coEvery { api.deleteUser(1L, "ADMIN") } throws Exception("Error al eliminar")

        val result = repo.deleteUser(1L)

        assertTrue(result.isFailure)
    }

    @Test
    fun updateUser_devuelve_not_implemented() = runBlocking {
        val api = mockk<AuthApiService>()
        val repo = UserRepository(api)

        val result = repo.updateUser(1L, "Nuevo Nombre", "999888777")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NotImplementedError)
    }
}