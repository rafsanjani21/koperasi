package com.example.koperasi.wilayah

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object WilayahRetrofit {
    fun createApi(): WilayahApi {
        return Retrofit.Builder()
            .baseUrl("https://wilayah.id/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WilayahApi::class.java)
    }
}
