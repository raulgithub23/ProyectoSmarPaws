package com.example.smartpaws.viewModels

import com.example.smartpaws.data.remote.pets.PetsDto
import com.example.smartpaws.data.repository.PetsRepository
import com.example.smartpaws.ui.mascota.PetsEvent
import com.example.smartpaws.ui.mascota.PetsViewModel
import com.example.smartpaws.viewmodel.AuthViewModel
import com.example.smartpaws.viewmodel.LoginUiState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class PetsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // Mocks
    private val repository = mockk<PetsRepository>(relaxed = true)
    private val authViewModel = mockk<AuthViewModel>(relaxed = true)

    // Flujo de control para simular Login/Logout
    private val fakeLoginFlow = MutableStateFlow(LoginUiState())

    private lateinit var viewModel: PetsViewModel

    // Datos de prueba
    private val userId = 10L
    private val samplePet = PetsDto(
        id = 1,
        userId = userId,
        name = "Firulais",
        especie = "Perro",
        fechaNacimiento = "2022-01-01",
        peso = 10f,
        genero = "M",
        color = "Café",
        notas = "Le gusta ladrar"
    )

    @Before
    fun setUp() {
        every { authViewModel.login } returns fakeLoginFlow

        viewModel = PetsViewModel(repository, authViewModel)
    }

    @Test
    fun `init NO carga mascotas si usuario no esta logueado`() = runTest {
        fakeLoginFlow.emit(LoginUiState(success = false, userId = null))
        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertTrue(state.petsList.isEmpty())
        coVerify(exactly = 0) { repository.observePetsByUser(any()) }
    }

    @Test
    fun `init comienza a observar mascotas cuando usuario hace Login`() = runTest {
        coEvery { repository.observePetsByUser(userId) } returns flowOf(Result.success(listOf(samplePet)))

        fakeLoginFlow.emit(LoginUiState(success = true, userId = userId))
        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertEquals(1, state.petsList.size)
        assertEquals("Firulais", state.petsList[0].name)

        coVerify { repository.observePetsByUser(userId) }
    }

    @Test
    fun `logout limpia la lista de mascotas`() = runTest {
        coEvery { repository.observePetsByUser(userId) } returns flowOf(Result.success(listOf(samplePet)))
        fakeLoginFlow.emit(LoginUiState(success = true, userId = userId))
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.petsList.size)

        fakeLoginFlow.emit(LoginUiState(success = false, userId = null))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.petsList.isEmpty())
    }

    @Test
    fun `LoadUserPets carga lista exitosamente`() = runTest {
        coEvery { repository.getPetsByUser(userId) } returns Result.success(listOf(samplePet))

        viewModel.onEvent(PetsEvent.LoadUserPets(userId))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.petsList.size)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `LoadUserPets maneja error`() = runTest {
        val errorMsg = "Error de red"
        coEvery { repository.getPetsByUser(userId) } returns Result.failure(Exception(errorMsg))

        viewModel.onEvent(PetsEvent.LoadUserPets(userId))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(errorMsg, state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun `AddNewPet inserta y luego recarga la lista`() = runTest {
        coEvery { repository.insertPet(samplePet) } returns Result.success(samplePet.id!!)

        coEvery { repository.getPetsByUser(userId) } returns Result.success(listOf(samplePet))

        viewModel.onEvent(PetsEvent.AddNewPet(samplePet))
        advanceUntilIdle()

        coVerify { repository.insertPet(samplePet) }
        coVerify { repository.getPetsByUser(userId) }

        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `AddNewPet muestra error si falla insercion`() = runTest {
        coEvery { repository.insertPet(any()) } returns Result.failure(Exception("Error al guardar"))

        viewModel.onEvent(PetsEvent.AddNewPet(samplePet))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Error al guardar", state.error)

        coVerify(exactly = 0) { repository.getPetsByUser(any()) }
    }

    @Test
    fun `EditPetInformation actualiza y recarga`() = runTest {
        val petEditada = samplePet.copy(name = "Rex")

        coEvery { repository.updatePet(petEditada) } returns Result.success(Unit)
        coEvery { repository.getPetsByUser(userId) } returns Result.success(listOf(petEditada))

        viewModel.onEvent(PetsEvent.EditPetInformation(petEditada))
        advanceUntilIdle()

        coVerify { repository.updatePet(petEditada) }
        coVerify { repository.getPetsByUser(userId) }

        assertEquals("Rex", viewModel.uiState.value.petsList[0].name)
    }

    @Test
    fun `RemovePetFromUser elimina y recarga`() = runTest {
        coEvery { repository.deletePet(samplePet) } returns Result.success(Unit)
        coEvery { repository.getPetsByUser(userId) } returns Result.success(emptyList())

        viewModel.onEvent(PetsEvent.RemovePetFromUser(samplePet))
        advanceUntilIdle()

        coVerify { repository.deletePet(samplePet) }
        assertTrue(viewModel.uiState.value.petsList.isEmpty())
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}