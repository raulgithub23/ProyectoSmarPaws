package com.example.smartpaws.repositories

import com.example.smartpaws.data.remote.pets.PetsApiService
import com.example.smartpaws.data.remote.pets.PetsDto
import com.example.smartpaws.data.repository.PetsRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class PetsRepositoryTest {

    private val samplePet = PetsDto(
        id = 1L,
        userId = 100L,
        name = "Firulais",
        especie = "Perro",
        fechaNacimiento = "2020-01-01",
        peso = 10.5f,
        genero = "Macho",
        color = "Café",
        notas = "Muy buen chico"
    )

    @Test
    fun getAllPets_devuelve_lista_cuando_API_responde_ok() = runBlocking {
        val api = mockk<PetsApiService>()
        val repo = PetsRepository(api)
        val listaMascotas = listOf(samplePet, samplePet.copy(id = 2, name = "Mishi"))

        coEvery { api.getAllPets() } returns Response.success(listaMascotas)

        val result = repo.getAllPets()

        assertTrue("El resultado debería ser exitoso", result.isSuccess)
        assertEquals(2, result.getOrNull()!!.size)
        assertEquals("Firulais", result.getOrNull()!![0].name)
    }

    @Test
    fun getAllPets_devuelve_fallo_cuando_API_falla() = runBlocking {
        val api = mockk<PetsApiService>()
        val repo = PetsRepository(api)

        val errorBody = "Error interno".toResponseBody("text/plain".toMediaTypeOrNull())
        coEvery { api.getAllPets() } returns Response.error(500, errorBody)

        val result = repo.getAllPets()

        assertTrue("El resultado debería ser fallo", result.isFailure)
    }

    @Test
    fun insertPet_devuelve_id_cuando_API_responde_ok() = runBlocking {
        val api = mockk<PetsApiService>()
        val repo = PetsRepository(api)

        coEvery { api.createPet(samplePet) } returns Response.success(samplePet)

        val result = repo.insertPet(samplePet)

        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull())
    }

    @Test
    fun getPetsByUser_devuelve_lista_cuando_API_responde_ok() = runBlocking {
        val api = mockk<PetsApiService>()
        val repo = PetsRepository(api)
        val userId = 100L

        coEvery { api.getPetsByUserId(userId) } returns Response.success(listOf(samplePet))

        val result = repo.getPetsByUser(userId)

        assertTrue(result.isSuccess)
        assertEquals(userId, result.getOrNull()!![0].userId)
    }

    @Test
    fun getPetById_devuelve_pet_cuando_existe() = runBlocking {
        val api = mockk<PetsApiService>()
        val repo = PetsRepository(api)
        val petId = 1L

        coEvery { api.getPetById(petId) } returns Response.success(samplePet)

        val result = repo.getPetById(petId)

        assertTrue(result.isSuccess)
        assertEquals("Firulais", result.getOrNull()?.name)
    }

    @Test
    fun getPetById_devuelve_null_exitoso_cuando_es_404() = runBlocking {
        // es 404, devuelve Success(null)
        val api = mockk<PetsApiService>()
        val repo = PetsRepository(api)
        val petId = 999L

        val errorBody = "".toResponseBody("application/json".toMediaTypeOrNull())
        coEvery { api.getPetById(petId) } returns Response.error(404, errorBody)

        val result = repo.getPetById(petId)

        assertTrue("Debe ser success aunque no encuentre el dato", result.isSuccess)
        assertNull("El cuerpo debe ser nulo si es 404", result.getOrNull())
    }

    @Test
    fun updatePet_devuelve_unit_cuando_API_ok() = runBlocking {
        val api = mockk<PetsApiService>()
        val repo = PetsRepository(api)

        coEvery { api.updatePet(1L, samplePet) } returns Response.success(samplePet)

        val result = repo.updatePet(samplePet)

        assertTrue(result.isSuccess)
    }

    @Test
    fun deletePet_devuelve_unit_cuando_API_ok() = runBlocking {
        val api = mockk<PetsApiService>()
        val repo = PetsRepository(api)

        coEvery { api.deletePet(1L) } returns Response.success(Unit)

        val result = repo.deletePet(samplePet)

        assertTrue(result.isSuccess)
    }

    @Test
    fun observePetsByUser_emite_datos_correctamente() = runBlocking {
        val api = mockk<PetsApiService>()
        val repo = PetsRepository(api)
        val userId = 100L

        coEvery { api.getPetsByUserId(userId) } returns Response.success(listOf(samplePet))

        val flowResult = repo.observePetsByUser(userId).first()

        assertTrue(flowResult.isSuccess)
        assertEquals(1, flowResult.getOrNull()!!.size)
    }
}