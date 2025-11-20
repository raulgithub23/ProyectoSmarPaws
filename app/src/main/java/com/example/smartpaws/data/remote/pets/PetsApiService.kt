package com.example.smartpaws.data.remote.pets

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface PetsApiService {

    // Endpoint: @GetMapping()
    @GET("api/pets")
    suspend fun getAllPets(): Response<List<PetsDto>>

    // Endpoint: @PostMapping()
    @POST("api/pets")
    suspend fun createPet(@Body pet: PetsDto): Response<PetsDto>

    // Endpoint: @GetMapping("/{id}")
    @GET("api/pets/{id}")
    suspend fun getPetById(@Path("id") id: Long): Response<PetsDto>

    // Endpoint: @PutMapping("/{id}")
    @PUT("api/pets/{id}")
    suspend fun updatePet(@Path("id") id: Long, @Body pet: PetsDto): Response<PetsDto>

    // Endpoint: @DeleteMapping("/{id}")
    @DELETE("api/pets/{id}")
    suspend fun deletePet(@Path("id") id: Long): Response<Unit>

    // Endpoint: @GetMapping("/usuario/{userId}")
    @GET("api/pets/usuario/{userId}")
    suspend fun getPetsByUserId(@Path("userId") userId: Long): Response<List<PetsDto>>

    // Endpoint: @GetMapping("/buscar/nombre")
    @GET("api/pets/buscar/nombre")
    suspend fun searchPetsByName(@Query("nombre") name: String): Response<List<PetsDto>>
}