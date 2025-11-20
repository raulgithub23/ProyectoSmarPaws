package com.example.smartpaws.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RemoteModule {

    private const val AUTH_BASE_URL = "https://94js12g4-8081.brs.devtunnels.ms/"
    private const val DOCTOR_BASE_URL = "https://94js12g4-8082.brs.devtunnels.ms/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttp = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val authRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl(AUTH_BASE_URL)
        .client(okHttp)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val doctorRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl(DOCTOR_BASE_URL)
        .client(okHttp)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun <T> createAuthService(service: Class<T>): T = authRetrofit.create(service)
    fun <T> createDoctorService(service: Class<T>): T = doctorRetrofit.create(service)
}