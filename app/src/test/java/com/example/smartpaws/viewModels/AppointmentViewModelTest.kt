package com.example.smartpaws.viewModels


import android.os.Build
import androidx.annotation.RequiresApi
import com.example.smartpaws.data.remote.appointments.AppointmentResponseDto
import com.example.smartpaws.data.remote.dto.DoctorDto
import com.example.smartpaws.data.remote.dto.ScheduleDto
import com.example.smartpaws.data.remote.pets.PetsDto
import com.example.smartpaws.data.repository.AppointmentRepository
import com.example.smartpaws.data.repository.DoctorRepository
import com.example.smartpaws.ui.mascota.PetsUiState
import com.example.smartpaws.ui.mascota.PetsViewModel
import com.example.smartpaws.viewmodel.AppointmentViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class AppointmentViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // --- MOCKS ---
    private val appointmentRepository = mockk<AppointmentRepository>(relaxed = true)
    private val doctorRepository = mockk<DoctorRepository>(relaxed = true)
    private val petsViewModel = mockk<PetsViewModel>(relaxed = true)

    //para simular que PetsViewModel tiene datos
    private val fakePetsUiState = MutableStateFlow(PetsUiState())

    private lateinit var viewModel: AppointmentViewModel

    // --- DATOS DE PRUEBA ---
    private val userId = 100L
    private val samplePet = PetsDto(1L, userId, "Firulais", "Perro", "2022-01-01", 10f, "M", "Café", "")

    private val sampleSchedule = ScheduleDto(
        id = 1,
        dayOfWeek = "Lunes",
        startTime = "09:00",
        endTime = "10:00"
    )

    private val sampleDoctor = DoctorDto(
        id = 50L,
        name = "Dr. House",
        specialty = "Diagnóstico",
        email = "house@test.com",
        phone = "123456",
        schedules = listOf(sampleSchedule)
    )

    private val nextMondayDate = LocalDate(2026, 1, 5)

    @Before
    fun setUp() {
        every { petsViewModel.uiState } returns fakePetsUiState

        // el repo de doctores devuelve nuestro doctor de prueba
        coEvery { doctorRepository.getAllDoctorsWithSchedules() } returns listOf(sampleDoctor)

        // Simulamos que no hay citas agendadas previas
        coEvery { appointmentRepository.getUpcomingAppointmentsByUser(userId) } returns Result.success(emptyList())

        // Instanciamos el ViewModel bajo prueba
        viewModel = AppointmentViewModel(
            appointmentRepository,
            doctorRepository,
            petsViewModel,
            userId
        )
    }

    @Test
    fun `init carga doctores y observa mascotas correctamente`() = runTest {
        fakePetsUiState.emit(PetsUiState(petsList = listOf(samplePet)))
        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertEquals(1, state.doctors.size)
        assertEquals("Dr. House", state.doctors[0].name)

        assertEquals(1, state.userPets.size)
        assertEquals("Firulais", state.userPets[0].name)
    }

    @Test
    fun `selectDoctor y selectDate genera slots de tiempo disponibles`() = runTest {
        viewModel.selectDoctor(sampleDoctor)

        coEvery {
            appointmentRepository.getAppointmentsByDoctorAndDate(sampleDoctor.id, nextMondayDate.toString())
        } returns Result.success(emptyList())

        viewModel.selectDate(nextMondayDate)
        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertEquals(2, state.availableTimes.size)
        assertTrue(state.availableTimes.contains("09:00"))
        assertTrue(state.availableTimes.contains("09:30"))

        assertEquals(nextMondayDate, state.selectedDate)
    }

    @Test
    fun `selectDate filtra horarios ya ocupados`() = runTest {
        viewModel.selectDoctor(sampleDoctor)

        val citaOcupada = AppointmentResponseDto(
            id = 99, userId = 1, petId = 1, doctorId = 50,
            date = nextMondayDate.toString(), time = "09:00", notes = "")

        coEvery {
            appointmentRepository.getAppointmentsByDoctorAndDate(sampleDoctor.id, nextMondayDate.toString())
        } returns Result.success(listOf(citaOcupada))

        viewModel.selectDate(nextMondayDate)
        advanceUntilIdle()

        val state = viewModel.uiState.value

        // Solo debería quedar "09:30" disponible
        assertEquals(1, state.availableTimes.size)
        assertEquals("09:30", state.availableTimes[0])
    }

    @Test
    fun `scheduleAppointment falla si faltan datos`() = runTest {
        // No seleccionamos nada
        viewModel.scheduleAppointment()
        assertEquals("Selecciona una mascota", viewModel.uiState.value.errorMsg)

        viewModel.selectPet(samplePet)
        viewModel.scheduleAppointment()
        assertEquals("Completa todos los campos", viewModel.uiState.value.errorMsg)
    }

    @Test
    fun `scheduleAppointment exitoso`() = runTest {
        prepararEstadoValido()

        // Mock: Disponibilidad OK
        coEvery {
            appointmentRepository.getAppointmentsByDoctorAndDate(sampleDoctor.id, nextMondayDate.toString())
        } returns Result.success(emptyList())

        // Mock: Creación OK devolviendo ID 777
        coEvery {
            appointmentRepository.createAppointment(any(), any(), any(), any(), any(), any())
        } returns Result.success(777L)

        viewModel.scheduleAppointment("Nota test")
        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertTrue(state.success)
        assertEquals(777L, state.createdAppointmentId)
        assertFalse(state.isSubmitting)

        // Verificar que se llamó al repositorio con los datos correctos
        coVerify {
            appointmentRepository.createAppointment(
                userId = userId,
                petId = samplePet.id,
                doctorId = sampleDoctor.id,
                date = nextMondayDate.toString(),
                time = "09:00",
                notes = "Nota test"
            )
        }
    }

    @Test
    fun `scheduleAppointment detecta Race Condition (alguien ocupo el turno antes)`() = runTest {
        prepararEstadoValido()

        // Mock: Cuando vamos a guardar, revisamos de nuevo y... ¡Sorpresa! Alguien ocupó "09:00"
        val citaIntrusa = AppointmentResponseDto(
            id = 88, userId = 2, petId = 2, doctorId = 50,
            date = nextMondayDate.toString(), time = "09:00",
            notes = "Nota test"
        )

        coEvery {
            appointmentRepository.getAppointmentsByDoctorAndDate(sampleDoctor.id, nextMondayDate.toString())
        } returns Result.success(listOf(citaIntrusa))

        viewModel.scheduleAppointment()
        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertEquals("Lo sentimos, este horario acaba de ser ocupado.", state.errorMsg)
        assertFalse(state.success)

        coVerify(exactly = 0) { appointmentRepository.createAppointment(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `deleteAppointment elimina cita localmente al tener exito`() = runTest {
        val citaExistente = AppointmentResponseDto(
            id = 10L, userId = userId, petId = 1, doctorId = 50,
            date = "2025-12-01", time = "10:00",
            notes = "Test de Nota"
        )

        coEvery { appointmentRepository.getUpcomingAppointmentsByUser(userId) } returns Result.success(listOf(citaExistente))

        viewModel = AppointmentViewModel(appointmentRepository, doctorRepository, petsViewModel, userId)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.scheduledAppointments.size)

        coEvery { appointmentRepository.deleteAppointmentById(10L) } returns Result.success(Unit)

        viewModel.deleteAppointment(10L)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.scheduledAppointments.isEmpty())
    }

    private fun prepararEstadoValido() {
        viewModel.selectPet(samplePet)
        viewModel.selectDoctor(sampleDoctor)
        viewModel.selectDate(nextMondayDate)
        viewModel.selectTime("09:00")
    }
}