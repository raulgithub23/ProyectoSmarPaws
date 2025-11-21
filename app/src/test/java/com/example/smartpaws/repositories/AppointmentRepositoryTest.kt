package com.example.smartpaws.repositories

import com.example.smartpaws.data.remote.appointments.AppointmentApiService
import com.example.smartpaws.data.remote.appointments.AppointmentRequestDto
import com.example.smartpaws.data.remote.appointments.AppointmentResponseDto
import com.example.smartpaws.data.repository.AppointmentRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class AppointmentRepositoryTest {

    // Datos de prueba
    private val sampleAppointment = AppointmentResponseDto(
        id = 10L,
        userId = 100L,
        petId = 200L,
        doctorId = 300L,
        date = "2024-12-25",
        time = "10:30",
        notes = "Revisión general"
    )

    @Test
    fun getAppointmentsByUser_devuelve_lista_exitosamente() = runBlocking {
        // Mocks
        val api = mockk<AppointmentApiService>()
        val repo = AppointmentRepository(api)
        val listaCitas = listOf(sampleAppointment)

        // comportamiento
        coEvery { api.getAppointmentsByUser(100L) } returns Response.success(listaCitas)

        //Ejecucion
        val result = repo.getAppointmentsByUser(100L)

        // erificar
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
        assertEquals(10L, result.getOrNull()!![0].id)
    }

    @Test
    fun getAppointmentsByUser_devuelve_fallo_en_error_api() = runBlocking {
        val api = mockk<AppointmentApiService>()
        val repo = AppointmentRepository(api)
        val errorBody = "Error".toResponseBody("text/plain".toMediaTypeOrNull())

        coEvery { api.getAppointmentsByUser(any()) } returns Response.error(500, errorBody)

        val result = repo.getAppointmentsByUser(100L)

        assertTrue(result.isFailure)
    }

    @Test
    fun observeAppointmentsByUser_emite_datos_correctamente() = runBlocking {
        val api = mockk<AppointmentApiService>()
        val repo = AppointmentRepository(api)
        val listaCitas = listOf(sampleAppointment)

        coEvery { api.getAppointmentsByUser(100L) } returns Response.success(listaCitas)

        val result = repo.observeAppointmentsByUser(100L).first()

        assertTrue(result.isSuccess)
        assertEquals(10L, result.getOrNull()!![0].id)
    }

    @Test
    fun getUpcomingAppointmentsByUser_devuelve_lista_exitosamente() = runBlocking {
        val api = mockk<AppointmentApiService>()
        val repo = AppointmentRepository(api)

        coEvery { api.getUpcomingAppointmentsByUser(100L) } returns Response.success(listOf(sampleAppointment))

        val result = repo.getUpcomingAppointmentsByUser(100L)

        assertTrue(result.isSuccess)
        assertEquals("2024-12-25", result.getOrNull()!![0].date)
    }

    @Test
    fun getAppointmentsByDoctorAndDate_devuelve_lista_exitosamente() = runBlocking {
        val api = mockk<AppointmentApiService>()
        val repo = AppointmentRepository(api)
        val date = "2024-12-25"

        coEvery { api.getAppointmentsByDoctorAndDate(300L, date) } returns Response.success(listOf(sampleAppointment))

        val result = repo.getAppointmentsByDoctorAndDate(300L, date)

        assertTrue(result.isSuccess)
        assertEquals(300L, result.getOrNull()!![0].doctorId)
    }

    @Test
    fun getAppointmentDetail_devuelve_detalle_exitosamente() = runBlocking {
        val api = mockk<AppointmentApiService>()
        val repo = AppointmentRepository(api)

        coEvery { api.getAppointmentById(10L) } returns Response.success(sampleAppointment)

        val result = repo.getAppointmentDetail(10L)

        assertTrue(result.isSuccess)
        assertEquals("Revisión general", result.getOrNull()?.notes)
    }

    @Test
    fun getAppointmentDetail_devuelve_error_custom_si_es_404() = runBlocking {
        val api = mockk<AppointmentApiService>()
        val repo = AppointmentRepository(api)
        val errorBody = "".toResponseBody("application/json".toMediaTypeOrNull())

        coEvery { api.getAppointmentById(99L) } returns Response.error(404, errorBody)

        val result = repo.getAppointmentDetail(99L)

        assertTrue(result.isFailure)
        assertEquals("Cita no encontrada", result.exceptionOrNull()?.message)
    }

    @Test
    fun createAppointment_crea_dto_y_devuelve_id() = runBlocking {
        val api = mockk<AppointmentApiService>()
        val repo = AppointmentRepository(api)

        coEvery { api.createAppointment(any()) } returns Response.success(123L)

        val result = repo.createAppointment(
            userId = 100L,
            petId = 200L,
            doctorId = 300L,
            date = "2025-01-01",
            time = "09:00",
            notes = "Nueva cita"
        )

        assertTrue(result.isSuccess)
        assertEquals(123L, result.getOrNull())

        coVerify { api.createAppointment(any<AppointmentRequestDto>()) }
    }

    @Test
    fun deleteAppointmentById_devuelve_unit_exitosamente() = runBlocking {
        val api = mockk<AppointmentApiService>()
        val repo = AppointmentRepository(api)

        coEvery { api.deleteAppointment(10L) } returns Response.success(Unit)

        val result = repo.deleteAppointmentById(10L)

        assertTrue(result.isSuccess)
    }

    @Test
    fun getAppointmentsForDoctor_devuelve_lista_exitosamente() = runBlocking {
        val api = mockk<AppointmentApiService>()
        val repo = AppointmentRepository(api)

        coEvery { api.getAppointmentsByDoctor(300L) } returns Response.success(listOf(sampleAppointment))

        val result = repo.getAppointmentsForDoctor(300L)

        assertTrue(result.isSuccess)
        assertEquals(300L, result.getOrNull()!![0].doctorId)
    }
}