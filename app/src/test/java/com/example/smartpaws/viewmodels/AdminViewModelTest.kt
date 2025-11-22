package com.example.smartpaws.viewmodel

import com.example.smartpaws.data.remote.dto.DoctorDto
import com.example.smartpaws.data.remote.dto.ScheduleDto
import com.example.smartpaws.data.remote.dto.UserDto
import com.example.smartpaws.data.repository.DoctorRepository
import com.example.smartpaws.data.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
class AdminViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var userRepository: UserRepository
    private lateinit var doctorRepository: DoctorRepository
    private lateinit var viewModel: AdminViewModel

    private val sampleUser = UserDto(
        id = 1L,
        rol = "USER",
        name = "Juan Pérez",
        email = "juan@example.com",
        phone = "123456789",
        profileImagePath = null
    )

    private val sampleScheduleDto = ScheduleDto(
        id = 1L,
        dayOfWeek = "LUNES",
        startTime = "09:00",
        endTime = "17:00"
    )

    private val sampleDoctorDto = DoctorDto(
        id = 1L,
        name = "Dr. María López",
        specialty = "Veterinaria",
        email = "maria@vet.com",
        phone = "987654321",
        schedules = listOf(sampleScheduleDto)
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        userRepository = mockk(relaxed = true)
        doctorRepository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadAllData carga usuarios y doctores exitosamente`() = runTest {
        val users = listOf(sampleUser)
        val doctors = listOf(sampleDoctorDto)

        coEvery { userRepository.getAllUsers() } returns Result.success(users)
        coEvery { doctorRepository.getAllDoctorsWithSchedules() } returns doctors

        viewModel = AdminViewModel(userRepository, doctorRepository)

        val state = viewModel.uiState.value

        assertEquals(1, state.users.size)
        assertEquals(1, state.doctors.size)
        assertFalse(state.isLoading)
        assertNull(state.errorMsg)
    }

    @Test
    fun `loadAllData maneja error correctamente`() = runTest {
        coEvery { userRepository.getAllUsers() } returns Result.failure(Exception("Error de red"))
        coEvery { doctorRepository.getAllDoctorsWithSchedules() } returns emptyList()

        viewModel = AdminViewModel(userRepository, doctorRepository)

        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertNotNull(state.errorMsg)
        assertTrue(state.errorMsg!!.contains("Error al cargar datos"))
    }

    @Test
    fun `createDoctor crea usuario y perfil de doctor exitosamente`() = runTest {
        coEvery { userRepository.getAllUsers() } returns Result.success(emptyList())
        coEvery { doctorRepository.getAllDoctorsWithSchedules() } returns emptyList()
        coEvery { userRepository.register(any(), any(), any(), any()) } returns Result.success(2L)
        coEvery { userRepository.updateUserRole(2L, "DOCTOR") } returns Result.success(Unit)
        coEvery { doctorRepository.createDoctorWithSchedules(any(), any(), any(), any(), any()) } returns Result.success(1L)

        viewModel = AdminViewModel(userRepository, doctorRepository)

        viewModel.createDoctor(
            name = "Dr. Pedro",
            email = "pedro@vet.com",
            phone = "111222333",
            pass = "Pass123$",
            specialty = "Cirugía"
        )

        coVerify { userRepository.register("Dr. Pedro", "pedro@vet.com", "111222333", "Pass123$") }
        coVerify { userRepository.updateUserRole(2L, "DOCTOR") }
        coVerify { doctorRepository.createDoctorWithSchedules(any(), any(), any(), any(), any()) }

        val state = viewModel.uiState.value
        assertEquals("Doctor (Usuario y Perfil) creado correctamente", state.successMsg)
    }

    @Test
    fun `createDoctor maneja error al registrar usuario`() = runTest {
        coEvery { userRepository.getAllUsers() } returns Result.success(emptyList())
        coEvery { doctorRepository.getAllDoctorsWithSchedules() } returns emptyList()
        coEvery { userRepository.register(any(), any(), any(), any()) } returns Result.failure(Exception("Email duplicado"))

        viewModel = AdminViewModel(userRepository, doctorRepository)

        viewModel.createDoctor(
            name = "Dr. Pedro",
            email = "pedro@vet.com",
            phone = "111222333",
            pass = "Pass123$",
            specialty = "Cirugía"
        )

        val state = viewModel.uiState.value
        assertNotNull(state.errorMsg)
        assertTrue(state.errorMsg!!.contains("Error al registrar usuario") || state.errorMsg!!.contains("Email duplicado"))
    }

    @Test
    fun `updateDoctorSchedules actualiza horarios exitosamente`() = runTest {
        coEvery { userRepository.getAllUsers() } returns Result.success(emptyList())
        coEvery { doctorRepository.getAllDoctorsWithSchedules() } returns emptyList()
        coEvery { doctorRepository.updateSchedules(any(), any()) } returns Result.success(Unit)

        viewModel = AdminViewModel(userRepository, doctorRepository)

        val newSchedules = listOf(
            ScheduleDto(null, "MARTES", "10:00", "18:00")
        )

        viewModel.updateDoctorSchedules(1L, newSchedules)

        coVerify { doctorRepository.updateSchedules(1L, newSchedules) }

        val state = viewModel.uiState.value
        assertEquals("Horario actualizado correctamente", state.successMsg)
    }

    @Test
    fun `deleteUser elimina usuario exitosamente`() = runTest {
        coEvery { userRepository.getAllUsers() } returns Result.success(emptyList())
        coEvery { doctorRepository.getAllDoctorsWithSchedules() } returns emptyList()
        coEvery { userRepository.deleteUser(1L) } returns Result.success(Unit)

        viewModel = AdminViewModel(userRepository, doctorRepository)

        viewModel.deleteUser(1L)

        coVerify { userRepository.deleteUser(1L) }

        val state = viewModel.uiState.value
        assertEquals("Usuario eliminado", state.successMsg)
    }

    @Test
    fun `deleteDoctorProfile elimina perfil de doctor exitosamente`() = runTest {
        coEvery { userRepository.getAllUsers() } returns Result.success(emptyList())
        coEvery { doctorRepository.getAllDoctorsWithSchedules() } returns emptyList()
        coEvery { doctorRepository.deleteDoctor(1L) } returns Result.success(Unit)

        viewModel = AdminViewModel(userRepository, doctorRepository)

        viewModel.deleteDoctorProfile(1L)

        coVerify { doctorRepository.deleteDoctor(1L) }

        val state = viewModel.uiState.value
        assertEquals("Perfil de doctor eliminado", state.successMsg)
    }

    @Test
    fun `onSearchQueryChange filtra usuarios correctamente`() = runTest {
        val users = listOf(sampleUser, sampleUser.copy(id = 2L, name = "Pedro González"))

        coEvery { userRepository.getAllUsers() } returns Result.success(users)
        coEvery { doctorRepository.getAllDoctorsWithSchedules() } returns emptyList()
        coEvery { userRepository.searchUsers("Pedro") } returns Result.success(listOf(users[1]))

        viewModel = AdminViewModel(userRepository, doctorRepository)

        viewModel.onSearchQueryChange("Pedro")

        coVerify { userRepository.searchUsers("Pedro") }

        val state = viewModel.uiState.value
        assertEquals("Pedro", state.searchQuery)
    }

    @Test
    fun `filterByRole filtra por rol correctamente`() = runTest {
        val doctors = listOf(sampleUser.copy(rol = "DOCTOR"))

        coEvery { userRepository.getAllUsers() } returns Result.success(emptyList())
        coEvery { doctorRepository.getAllDoctorsWithSchedules() } returns emptyList()
        coEvery { userRepository.getUsersByRole("DOCTOR") } returns Result.success(doctors)

        viewModel = AdminViewModel(userRepository, doctorRepository)

        viewModel.filterByRole("DOCTOR")

        coVerify { userRepository.getUsersByRole("DOCTOR") }

        val state = viewModel.uiState.value
        assertEquals("DOCTOR", state.selectedRole)
    }

    @Test
    fun `calculateStats calcula estadísticas correctamente`() = runTest {
        val users = listOf(
            sampleUser.copy(id = 1L, rol = "USER"),
            sampleUser.copy(id = 2L, rol = "ADMIN"),
            sampleUser.copy(id = 3L, rol = "USER")
        )
        val doctors = listOf(sampleDoctorDto)

        coEvery { userRepository.getAllUsers() } returns Result.success(users)
        coEvery { doctorRepository.getAllDoctorsWithSchedules() } returns doctors

        viewModel = AdminViewModel(userRepository, doctorRepository)

        val state = viewModel.uiState.value

        assertEquals(3, state.stats.totalUsers)
        assertEquals(1, state.stats.adminCount)
        assertEquals(2, state.stats.userCount)
        assertEquals(1, state.stats.doctorCount)
    }

    @Test
    fun `clearMessages limpia mensajes de error y éxito`() = runTest {
        coEvery { userRepository.getAllUsers() } returns Result.success(emptyList())
        coEvery { doctorRepository.getAllDoctorsWithSchedules() } returns emptyList()

        viewModel = AdminViewModel(userRepository, doctorRepository)

        viewModel.clearMessages()

        val state = viewModel.uiState.value
        assertNull(state.errorMsg)
        assertNull(state.successMsg)
    }
}