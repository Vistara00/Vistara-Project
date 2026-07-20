package com.example.vistaraapp.api

import com.example.vistaraapp.entities_dataclass.WeatherResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("v1/forecast")
    suspend fun getWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current_weather") currentWeather: Boolean = true,
        @Query("hourly") hourly: String = "temperature_2m,precipitation",
        @Query("daily") daily: String = "temperature_2m_max,temperature_2m_min",
        @Query("timezone") timezone: String = "Africa/Nairobi"
    ): WeatherResponse
}

object WeatherApiClient {
    private const val BASE_URL = "https://api.open-meteo.com/"

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val weatherService: WeatherApiService = retrofit.create(WeatherApiService::class.java)
}
