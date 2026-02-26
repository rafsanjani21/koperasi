package com.example.koperasi.data.remote

import android.content.Context
import com.example.koperasi.TokenManager
import com.example.koperasi.repository.PaymentRepository
import com.example.koperasi.repository.PaymentRepositoryImpl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val BASE_URL = "http://192.168.52.29:8080/"

    private lateinit var tokenManager: TokenManager

    fun init(context: Context) {
        tokenManager = TokenManager(context)
    }

    // ================= LOGGING =================
    private val loggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    // ================= REFRESH API (NO AUTHENTICATOR) =================
    private val refreshApi: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    // ================= OKHTTP =================
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .authenticator(
                TokenAuthenticator(
                    tokenManager,
                    refreshApi
                )
            )
            .build()
    }

    // ================= RETROFIT WITH AUTH =================
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // ================= MAIN API =================
    val api: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    // ================= PAYMENT API =================
    private val paymentApi: PaymentApi by lazy {
        retrofit.create(PaymentApi::class.java)
    }

    // ================= REPOSITORY =================
    val paymentRepository: PaymentRepository by lazy {
        PaymentRepositoryImpl(paymentApi)
    }
}