package com.example.smartpaws.repositories

import com.example.smartpaws.data.remote.DoctorApiService
import com.example.smartpaws.data.remote.dto.CreateDoctorRequest
import com.example.smartpaws.data.remote.dto.DoctorDto
import com.example.smartpaws.data.remote.dto.ScheduleDto
import com.example.smartpaws.data.remote.dto.UpdateSchedulesRequest
import com.example.smartpaws.data.repository.DoctorRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DoctorRepositoryTest {

    private val sampleScheduleDto = ScheduleDto(
        id = 1L,
        dayOfWeek = "LUNES",
        startTime = "09:00",
        endTime = "17:00"
    )

    private val sampleDoctorDto = DoctorDto(
        id = 1L,
        name = "Dr. Juan Pérez",
        specialty = "Veterinaria General",
        email = "juan.perez@vet.com",
        phone = "123456789",
        schedules = listOf(sampleScheduleDto)
    )

    @Test
    fun getAllDoctorsWithSchedules_devuelve_lista_exitosamente() = runBlocking {
        val api = mockk<DoctorApiService>()
        val repo = DoctorRepository(api)
        val doctorList = listOf(sampleDoctorDto)

        coEvery { api.getAllDoctors() } returns doctorList

        val result = repo.getAllDoctorsWithSchedules()

        assertEquals(1, result.size)
        assertEquals("Dr. Juan Pérez", result[0].name)
        assertEquals(1, result[0].schedules.size)
    }

    @Test
    fun getAllDoctorsWithSchedules_devuelve_lista_vacia_en_error() = runBlocking {
        val api = mockk<DoctorApiService>()
        val repo = DoctorRepository(api)

        coEvery { api.getAllDoctors() } throws Exception("Error de red")

        val result = repo.getAllDoctorsWithSchedules()

        assertEquals(0, result.size)
    }

    @Test
    fun getDoctorWithSchedules_devuelve_doctor_exitosamente() = runBlocking {
        val api = mockk<DoctorApiService>()
        val repo = DoctorRepository(api)

        coEvery { api.getDoctorById(1L) } returns sampleDoctorDto

        val result = repo.getDoctorWithSchedules(1L)

        assertTrue(result.isSuccess)
        assertEquals("Dr. Juan Pérez", result.getOrNull()?.name)
        assertEquals("juan.perez@vet.com", result.getOrNull()?.email)
    }

    @Test
    fun getDoctorWithSchedules_devuelve_error_cuando_falla() = runBlocking {
        val api = mockk<DoctorApiService>()
        val repo = DoctorRepository(api)

        coEvery { api.getDoctorById(999L) } throws Exception("Doctor no encontrado")

        val result = repo.getDoctorWithSchedules(999L)

        assertTrue(result.isFailure)
    }

    @Test
    fun getDoctorByEmail_devuelve_doctor_exitosamente() = runBlocking {
        val api = mockk<DoctorApiService>()
        val repo = DoctorRepository(api)

        coEvery { api.getDoctorByEmail("juan.perez@vet.com") } returns sampleDoctorDto

        val result = repo.getDoctorByEmail("juan.perez@vet.com")

        assertTrue(result.isSuccess)
        assertEquals("juan.perez@vet.com", result.getOrNull()?.email)
        assertEquals("Dr. Juan Pérez", result.getOrNull()?.name)
    }

    @Test
    fun getDoctorByEmail_devuelve_error_cuando_no_existe() = runBlocking {
        val api = mockk<DoctorApiService>()
        val repo = DoctorRepository(api)

        coEvery { api.getDoctorByEmail("noexiste@vet.com") } throws Exception("Doctor no encontrado")

        val result = repo.getDoctorByEmail("noexiste@vet.com")

        assertTrue(result.isFailure)
    }

    @Test
    fun createDoctorWithSchedules_crea_doctor_exitosamente() = runBlocking {
        val api = mockk<DoctorApiService>()
        val repo = DoctorRepository(api)

        val schedules = listOf(
            ScheduleDto(null, "LUNES", "09:00", "17:00")
        )

        coEvery { api.createDoctor(any()) } returns sampleDoctorDto

        val result = repo.createDoctorWithSchedules(
            name = "Dr. Juan Pérez",
            specialty = "Veterinaria General",
            email = "juan.perez@vet.com",
            phone = "123456789",
            schedules = schedules
        )

        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull())
        coVerify { api.createDoctor(any<CreateDoctorRequest>()) }
    }

    @Test
    fun createDoctorWithSchedules_maneja_error_correctamente() = runBlocking {
        val api = mockk<DoctorApiService>()
        val repo = DoctorRepository(api)

        val schedules = listOf(
            ScheduleDto(null, "LUNES", "09:00", "17:00")
        )

        coEvery { api.createDoctor(any()) } throws Exception("Email duplicado")

        val result = repo.createDoctorWithSchedules(
            name = "Dr. Juan Pérez",
            specialty = "Veterinaria General",
            email = "juan.perez@vet.com",
            phone = "123456789",
            schedules = schedules
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun hasDoctors_devuelve_true_cuando_hay_doctores() = runBlocking {
        val api = mockk<DoctorApiService>()
        val repo = DoctorRepository(api)

        coEvery { api.countDoctors() } returns 5L

        val result = repo.hasDoctors()

        assertTrue(result)
    }

    @Test
    fun hasDoctors_devuelve_false_cuando_no_hay_doctores() = runBlocking {
        val api = mockk<DoctorApiService>()
        val repo = DoctorRepository(api)

        coEvery { api.countDoctors() } returns 0L

        val result = repo.hasDoctors()

        assertFalse(result)
    }

    @Test
    fun hasDoctors_devuelve_false_en_caso_de_error() = runBlocking {
        val api = mockk<DoctorApiService>()
        val repo = DoctorRepository(api)

        coEvery { api.countDoctors() } throws Exception("Error de conexión")

        val result = repo.hasDoctors()

        assertFalse(result)
    }

    @Test
    fun updateSchedules_actualiza_exitosamente() = runBlocking {
        val api = mockk<DoctorApiService>()
        val repo = DoctorRepository(api)

        val newSchedules = listOf(
            ScheduleDto(null, "MARTES", "10:00", "18:00")
        )

        coEvery { api.updateSchedules(1L, any()) } returns sampleDoctorDto

        val result = repo.updateSchedules(1L, newSchedules)

        assertTrue(result.isSuccess)
        coVerify { api.updateSchedules(eq(1L), any<UpdateSchedulesRequest>()) }
    }

    @Test
    fun updateSchedules_maneja_error_correctamente() = runBlocking {
        val api = mockk<DoctorApiService>()
        val repo = DoctorRepository(api)

        val newSchedules = listOf(
            ScheduleDto(null, "MARTES", "10:00", "18:00")
        )

        coEvery { api.updateSchedules(1L, any()) } throws Exception("Doctor no encontrado")

        val result = repo.updateSchedules(1L, newSchedules)

        assertTrue(result.isFailure)
    }

    @Test
    fun deleteDoctor_elimina_exitosamente() = runBlocking {
        val api = mockk<DoctorApiService>()
        val repo = DoctorRepository(api)

        coEvery { api.deleteDoctor(1L) } returns Unit

        val result = repo.deleteDoctor(1L)

        assertTrue(result.isSuccess)
        coVerify { api.deleteDoctor(1L) }
    }

    @Test
    fun deleteDoctor_maneja_error_correctamente() = runBlocking {
        val api = mockk<DoctorApiService>()
        val repo = DoctorRepository(api)

        coEvery { api.deleteDoctor(999L) } throws Exception("Doctor no encontrado")

        val result = repo.deleteDoctor(999L)

        assertTrue(result.isFailure)
    }

    @Test
    fun createDoctorWithSchedules_sin_horarios() = runBlocking {
        val api = mockk<DoctorApiService>()
        val repo = DoctorRepository(api)

        coEvery { api.createDoctor(any()) } returns sampleDoctorDto

        val result = repo.createDoctorWithSchedules(
            name = "Dr. Juan Pérez",
            specialty = "Veterinaria General",
            email = "juan.perez@vet.com",
            phone = "123456789",
            schedules = emptyList()
        )

        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull())
    }

    @Test
    fun createDoctorWithSchedules_sin_telefono() = runBlocking {
        val api = mockk<DoctorApiService>()
        val repo = DoctorRepository(api)

        val schedules = listOf(
            ScheduleDto(null, "LUNES", "09:00", "17:00")
        )

        coEvery { api.createDoctor(any()) } returns sampleDoctorDto

        val result = repo.createDoctorWithSchedules(
            name = "Dr. Juan Pérez",
            specialty = "Veterinaria General",
            email = "juan.perez@vet.com",
            phone = null,
            schedules = schedules
        )

        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull())
    }
}