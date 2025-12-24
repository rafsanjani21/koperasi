package com.example.koperasi.wilayah.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WilayahDao {

    // Provinces
    @Query("SELECT * FROM province ORDER BY name ASC")
    suspend fun getProvinces(): List<ProvinceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProvinces(items: List<ProvinceEntity>)

    // Regencies
    @Query("SELECT * FROM regency WHERE provinceCode = :provinceCode ORDER BY name ASC")
    suspend fun getRegencies(provinceCode: String): List<RegencyEntity>

    // ✅ NEW: ambil semua kab/kota (buat tempat lahir global)
    @Query("SELECT * FROM regency ORDER BY name ASC")
    suspend fun getAllRegencies(): List<RegencyEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRegencies(items: List<RegencyEntity>)

    // Districts
    @Query("SELECT * FROM district WHERE regencyCode = :regencyCode ORDER BY name ASC")
    suspend fun getDistricts(regencyCode: String): List<DistrictEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDistricts(items: List<DistrictEntity>)

    // Villages
    @Query("SELECT * FROM village WHERE districtCode = :districtCode ORDER BY name ASC")
    suspend fun getVillages(districtCode: String): List<VillageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertVillages(items: List<VillageEntity>)
}
