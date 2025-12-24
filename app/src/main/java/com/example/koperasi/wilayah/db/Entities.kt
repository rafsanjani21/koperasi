package com.example.koperasi.wilayah.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "province")
data class ProvinceEntity(
    @PrimaryKey val code: String,
    val name: String,
    val updatedAt: String? // dari meta.updated_at
)

@Entity(tableName = "regency")
data class RegencyEntity(
    @PrimaryKey val code: String,
    val provinceCode: String,
    val name: String,
    val updatedAt: String?
)

@Entity(tableName = "district")
data class DistrictEntity(
    @PrimaryKey val code: String,
    val regencyCode: String,
    val name: String,
    val updatedAt: String?
)

@Entity(tableName = "village")
data class VillageEntity(
    @PrimaryKey val code: String,
    val districtCode: String,
    val name: String,
    val updatedAt: String?
)
