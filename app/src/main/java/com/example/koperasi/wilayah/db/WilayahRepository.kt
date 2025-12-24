package com.example.koperasi.wilayah

import com.example.koperasi.wilayah.db.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class WilayahOption(val code: String, val name: String)

class WilayahRepository(
    private val api: WilayahApi,
    private val dao: WilayahDao
) {
    suspend fun getProvinces(): List<WilayahOption> = withContext(Dispatchers.IO) {
        val cached = dao.getProvinces()
        if (cached.isNotEmpty()) return@withContext cached.map { WilayahOption(it.code, it.name) }

        val res = api.provinces()
        val updatedAt = res.meta?.updated_at
        val entities = res.data.map { ProvinceEntity(it.code, it.name, updatedAt) }
        dao.upsertProvinces(entities)
        entities.map { WilayahOption(it.code, it.name) }
    }

    suspend fun getRegencies(provinceCode: String): List<WilayahOption> = withContext(Dispatchers.IO) {
        val cached = dao.getRegencies(provinceCode)
        if (cached.isNotEmpty()) return@withContext cached.map { WilayahOption(it.code, it.name) }

        val res = api.regencies(provinceCode)
        val updatedAt = res.meta?.updated_at
        val entities = res.data.map { RegencyEntity(it.code, provinceCode, it.name, updatedAt) }
        dao.upsertRegencies(entities)
        entities.map { WilayahOption(it.code, it.name) }
    }

    suspend fun getDistricts(regencyCode: String): List<WilayahOption> = withContext(Dispatchers.IO) {
        val cached = dao.getDistricts(regencyCode)
        if (cached.isNotEmpty()) return@withContext cached.map { WilayahOption(it.code, it.name) }

        val res = api.districts(regencyCode)
        val updatedAt = res.meta?.updated_at
        val entities = res.data.map { DistrictEntity(it.code, regencyCode, it.name, updatedAt) }
        dao.upsertDistricts(entities)
        entities.map { WilayahOption(it.code, it.name) }
    }

    suspend fun getVillages(districtCode: String): List<WilayahOption> = withContext(Dispatchers.IO) {
        val cached = dao.getVillages(districtCode)
        if (cached.isNotEmpty()) return@withContext cached.map { WilayahOption(it.code, it.name) }

        val res = api.villages(districtCode)
        val updatedAt = res.meta?.updated_at
        val entities = res.data.map { VillageEntity(it.code, districtCode, it.name, updatedAt) }
        dao.upsertVillages(entities)
        entities.map { WilayahOption(it.code, it.name) }
    }

    /**
     * ✅ NEW: daftar Kab/Kota seluruh Indonesia untuk "Tempat Lahir"
     * Cache-first:
     * - kalau tabel regency sudah terisi -> pakai itu
     * - kalau kosong -> fetch semua provinsi, lalu fetch regencies per provinsi, simpan ke Room
     */
    suspend fun getAllRegenciesForBirthPlace(): List<WilayahOption> = withContext(Dispatchers.IO) {
        val cachedAll = dao.getAllRegencies()
        if (cachedAll.isNotEmpty()) {
            return@withContext cachedAll.map { WilayahOption(it.code, it.name) }
        }

        val prov = api.provinces()
        val provUpdated = prov.meta?.updated_at

        val all = mutableListOf<RegencyEntity>()
        for (p in prov.data) {
            val reg = api.regencies(p.code)
            val regUpdated = reg.meta?.updated_at ?: provUpdated
            all += reg.data.map { RegencyEntity(it.code, p.code, it.name, regUpdated) }
        }

        dao.upsertRegencies(all)
        all.map { WilayahOption(it.code, it.name) }.sortedBy { it.name }
    }
}
