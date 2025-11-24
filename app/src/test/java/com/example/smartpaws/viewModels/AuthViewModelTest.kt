package com.example.smartpaws.viewmodel

import com.example.smartpaws.data.local.storage.UserPreferences
import com.example.smartpaws.data.remote.dto.UserDto
import com.example.smartpaws.data.repository.UserRepository
import com.example.smartpaws.domain.validation.validateConfirm
import com.example.smartpaws.domain.validation.validateEmail
import com.example.smartpaws.domain.validation.validateNameLettersOnly
import com.example.smartpaws.domain.validation.validatePhoneDigitsOnly
import com.example.smartpaws.domain.validation.validateStrongPassword
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var userRepository: UserRepository
    private lateinit var userPreferences: UserPreferences
    private lateinit var viewModel: AuthViewModel

    private val sampleUserDto = UserDto(
        id = 1L,
        rol = "USER",
        name = "Juan Pérez",
        email = "juan@example.com",
        phone = "123456789",
        profileImagePath = null
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        userRepository = mockk(relaxed = true)
        userPreferences = mockk(relaxed = true)

        mockkStatic("com.example.smartpaws.domain.validation.ValidatorsKt")

        every { validateEmail(any()) } answers {
            val email = firstArg<String>()
            // Simulamos lógica básica: válido si tiene @ y no es el string "email-invalido" usado en los tests
            if (email.contains("@") && !email.contains("invalido")) null else "Email inválido"
        }

        every { validateStrongPassword(any()) } answers {
            val pass = firstArg<String>()
            if (pass == "weak") "Contraseña débil" else null
        }

        every { validateConfirm(any(), any()) } answers {
            val pass = firstArg<String>()
            val confirm = secondArg<String>()
            if (pass != confirm) "No coinciden" else null
        }

        every { validateNameLettersOnly(any()) } returns null
        every { validatePhoneDigitsOnly(any()) } returns null
    }

    @After
    fun tearDown() {
        unmockkStatic("com.example.smartpaws.domain.validation.ValidatorsKt")
        Dispatchers.resetMain()
    }

    @Test
    fun `checkActiveSession restaura sesión cuando hay userId guardado`() = runTest {
        every { userPreferences.loggedInUserId } returns flowOf(1L)
        coEvery { userRepository.getUserById(1L) } returns Result.success(sampleUserDto)

        viewModel = AuthViewModel(userRepository, userPreferences)

        advanceUntilIdle()

        val loginState = viewModel.login.value
        assertTrue(loginState.success)
        assertEquals(1L, loginState.userId)

        val userProfile = viewModel.userProfile.value
        assertNotNull(userProfile)
        assertEquals("Juan Pérez", userProfile?.name)
    }

    @Test
    fun `checkActiveSession no restaura sesión cuando no hay userId`() = runTest {
        every { userPreferences.loggedInUserId } returns flowOf(null)

        viewModel = AuthViewModel(userRepository, userPreferences)
        advanceUntilIdle()

        val loginState = viewModel.login.value
        assertFalse(loginState.success)
        assertNull(loginState.userId)
    }

    @Test
    fun `onLoginEmailChange valida email correctamente`() = runTest {
        every { userPreferences.loggedInUserId } returns flowOf(null)
        viewModel = AuthViewModel(userRepository, userPreferences)

        viewModel.onLoginEmailChange("juan@example.com")

        val state = viewModel.login.value
        assertEquals("juan@example.com", state.email)
        assertNull(state.emailError)
    }

    @Test
    fun `onLoginEmailChange muestra error con email inválido`() = runTest {
        every { userPreferences.loggedInUserId } returns flowOf(null)
        viewModel = AuthViewModel(userRepository, userPreferences)

        viewModel.onLoginEmailChange("email-invalido")

        val state = viewModel.login.value
        assertEquals("email-invalido", state.email)
        assertNotNull(state.emailError)
    }

    @Test
    fun `submitLogin exitoso guarda userId y carga perfil`() = runTest {
        every { userPreferences.loggedInUserId } returns flowOf(null)
        coEvery { userRepository.login(any(), any()) } returns Result.success(sampleUserDto)
        coEvery { userRepository.getUserById(1L) } returns Result.success(sampleUserDto)

        viewModel = AuthViewModel(userRepository, userPreferences)

        viewModel.onLoginEmailChange("juan@example.com")
        viewModel.onLoginPassChange("Pass123$")
        viewModel.submitLogin()

        advanceUntilIdle()

        coVerify { userPreferences.saveUserId(1L) }
        coVerify { userRepository.getUserById(1L) }

        val state = viewModel.login.value
        assertTrue(state.success)
        assertEquals(1L, state.userId)
        assertNull(state.errorMsg)
    }

    @Test
    fun `submitLogin fallido muestra mensaje de error`() = runTest {
        every { userPreferences.loggedInUserId } returns flowOf(null)
        coEvery { userRepository.login(any(), any()) } returns Result.failure(
            IllegalArgumentException("Credenciales inválidas")
        )

        viewModel = AuthViewModel(userRepository, userPreferences)

        viewModel.onLoginEmailChange("juan@example.com")
        viewModel.onLoginPassChange("wrongpass")
        viewModel.submitLogin()

        advanceUntilIdle()

        val state = viewModel.login.value
        assertFalse(state.success)
        assertNotNull(state.errorMsg)
        assertTrue(state.errorMsg!!.contains("Credenciales inválidas"))
    }

    @Test
    fun `submitLogin no se ejecuta si canSubmit es false`() = runTest {
        every { userPreferences.loggedInUserId } returns flowOf(null)
        viewModel = AuthViewModel(userRepository, userPreferences)

        viewModel.onLoginEmailChange("")
        viewModel.submitLogin()

        advanceUntilIdle()

        coVerify(exactly = 0) { userRepository.login(any(), any()) }
    }

    @Test
    fun `onNameChange filtra caracteres no permitidos`() = runTest {
        every { userPreferences.loggedInUserId } returns flowOf(null)
        viewModel = AuthViewModel(userRepository, userPreferences)

        viewModel.onNameChange("Juan123")

        val state = viewModel.register.value
        assertEquals("Juan", state.name)
    }

    @Test
    fun `onPhoneChange solo permite dígitos`() = runTest {
        every { userPreferences.loggedInUserId } returns flowOf(null)
        viewModel = AuthViewModel(userRepository, userPreferences)

        viewModel.onPhoneChange("123abc456")

        val state = viewModel.register.value
        assertEquals("123456", state.phone)
    }

    @Test
    fun `onRegisterPassChange valida contraseña fuerte`() = runTest {
        every { userPreferences.loggedInUserId } returns flowOf(null)
        viewModel = AuthViewModel(userRepository, userPreferences)

        viewModel.onRegisterPassChange("weak")

        val state = viewModel.register.value
        assertNotNull(state.passError)

        viewModel.onRegisterPassChange("Strong123$")

        val state2 = viewModel.register.value
        assertNull(state2.passError)
    }

    @Test
    fun `onConfirmChange valida que coincida con contraseña`() = runTest {
        every { userPreferences.loggedInUserId } returns flowOf(null)
        viewModel = AuthViewModel(userRepository, userPreferences)

        viewModel.onRegisterPassChange("Pass123$")
        viewModel.onConfirmChange("Pass123$")

        val state = viewModel.register.value
        assertNull(state.confirmError)

        viewModel.onConfirmChange("Different123$")

        val state2 = viewModel.register.value
        assertNotNull(state2.confirmError)
    }

    @Test
    fun `submitRegister exitoso registra usuario`() = runTest {
        every { userPreferences.loggedInUserId } returns flowOf(null)
        coEvery { userRepository.register(any(), any(), any(), any()) } returns Result.success(2L)

        viewModel = AuthViewModel(userRepository, userPreferences)

        viewModel.onNameChange("Maria López")
        viewModel.onRegisterEmailChange("maria@example.com")
        viewModel.onPhoneChange("987654321")
        viewModel.onRegisterPassChange("Pass123$")
        viewModel.onConfirmChange("Pass123$")

        viewModel.submitRegister()

        advanceUntilIdle()

        coVerify { userRepository.register("Maria López", "maria@example.com", "987654321", "Pass123$") }

        val state = viewModel.register.value
        assertTrue(state.success)
        assertNull(state.errorMsg)
    }

    @Test
    fun `submitRegister fallido muestra mensaje de error`() = runTest {
        every { userPreferences.loggedInUserId } returns flowOf(null)
        coEvery { userRepository.register(any(), any(), any(), any()) } returns Result.failure(
            IllegalStateException("Email duplicado")
        )

        viewModel = AuthViewModel(userRepository, userPreferences)

        viewModel.onNameChange("Maria López")
        viewModel.onRegisterEmailChange("maria@example.com")
        viewModel.onPhoneChange("987654321")
        viewModel.onRegisterPassChange("Pass123$")
        viewModel.onConfirmChange("Pass123$")

        viewModel.submitRegister()

        advanceUntilIdle()

        val state = viewModel.register.value
        assertFalse(state.success)
        assertNotNull(state.errorMsg)
    }

    @Test
    fun `canSubmit solo es true cuando todos los campos son válidos`() = runTest {
        every { userPreferences.loggedInUserId } returns flowOf(null)
        viewModel = AuthViewModel(userRepository, userPreferences)

        viewModel.onNameChange("Maria")
        viewModel.onRegisterEmailChange("maria@example.com")
        viewModel.onPhoneChange("987654321")
        viewModel.onRegisterPassChange("Pass123$")

        var state = viewModel.register.value
        assertFalse(state.canSubmit)

        viewModel.onConfirmChange("Pass123$")

        state = viewModel.register.value
        assertTrue(state.canSubmit)
    }

    @Test
    fun `logout limpia sesión y resetea estados`() = runTest {
        every { userPreferences.loggedInUserId } returns flowOf(1L)
        coEvery { userRepository.getUserById(1L) } returns Result.success(sampleUserDto)

        viewModel = AuthViewModel(userRepository, userPreferences)
        advanceUntilIdle()

        viewModel.logout()
        advanceUntilIdle()

        coVerify { userPreferences.clearSession() }

        val loginState = viewModel.login.value
        assertFalse(loginState.success)
        assertNull(loginState.userId)

        val userProfile = viewModel.userProfile.value
        assertNull(userProfile)
    }

    @Test
    fun `updateUserProfile actualiza perfil correctamente`() = runTest {
        every { userPreferences.loggedInUserId } returns flowOf(1L)
        coEvery { userRepository.getUserById(1L) } returns Result.success(sampleUserDto)
        coEvery { userRepository.updateUser(any(), any(), any()) } returns Result.success(Unit)

        viewModel = AuthViewModel(userRepository, userPreferences)
        advanceUntilIdle()

        viewModel.updateUserProfile("Nuevo Nombre", "999888777")
        advanceUntilIdle()

        coVerify { userRepository.updateUser(1L, "Nuevo Nombre", "999888777") }
    }

    @Test
    fun `clearLoginResult limpia banderas de resultado`() = runTest {
        every { userPreferences.loggedInUserId } returns flowOf(null)
        viewModel = AuthViewModel(userRepository, userPreferences)

        viewModel.clearLoginResult()

        val state = viewModel.login.value
        assertFalse(state.success)
        assertNull(state.errorMsg)
    }

    @Test
    fun `clearRegisterResult limpia banderas de resultado`() = runTest {
        every { userPreferences.loggedInUserId } returns flowOf(null)
        viewModel = AuthViewModel(userRepository, userPreferences)

        viewModel.clearRegisterResult()

        val state = viewModel.register.value
        assertFalse(state.success)
        assertNull(state.errorMsg)
    }
}