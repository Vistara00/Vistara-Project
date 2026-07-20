package com.example.vistaraapp.api

import com.example.vistaraapp.utils.TokenManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

//  RETROFIT CLIENT

object RetrofitClient {
    private const val BASE_URL = "https://undrafted-erasable-crevice.ngrok-free.dev/api/v1/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("ngrok-skip-browser-warning", "true")
                .build()
            chain.proceed(request)
        }
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // 1. Auth instance built completely standalone
    val instance: ApiAuthService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiAuthService::class.java)
    }

    // 2. Bookings  & SOS
    val bookingInstance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    // 3. Edit profile
    val profileInstance: ProfileApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ProfileApiService::class.java)
    }

    // mpesa
    val mpesaInstance: VistaraApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // Attaches timeouts and ngrok header bypass filters safely
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(VistaraApi::class.java)
    }

    val sessionInstance: SessionApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SessionApi::class.java)
    }


    class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {

        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            val token = tokenManager.getToken()

            val request = chain.request().newBuilder()

            if (token != null) {
                request.addHeader("Authorization", "Bearer $token")
            }

            return chain.proceed(request.build())
        }
    }


}
