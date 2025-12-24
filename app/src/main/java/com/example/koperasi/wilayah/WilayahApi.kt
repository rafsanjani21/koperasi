package com.example.koperasi.wilayah

import retrofit2.http.GET
import retrofit2.http.Path

data class ApiMeta(val updated_at: String?)
data class ApiItem(val code: String, val name: String)
data class ApiResponse(val data: List<ApiItem>, val meta: ApiMeta?)

interface WilayahApi {
    @GET("api/provinces.json")
    suspend fun provinces(): ApiResponse

    @GET("api/regencies/{provinceCode}.json")
    suspend fun regencies(@Path("provinceCode") provinceCode: String): ApiResponse

    @GET("api/districts/{regencyCode}.json")
    suspend fun districts(@Path("regencyCode") regencyCode: String): ApiResponse

    @GET("api/villages/{districtCode}.json")
    suspend fun villages(@Path("districtCode") districtCode: String): ApiResponse
}
