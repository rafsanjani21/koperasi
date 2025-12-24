package com.example.koperasi.wilayah.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ProvinceEntity::class, RegencyEntity::class, DistrictEntity::class, VillageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class WilayahDb : RoomDatabase() {
    abstract fun wilayahDao(): WilayahDao

    companion object {
        @Volatile private var INSTANCE: WilayahDb? = null

        fun get(context: Context): WilayahDb {
            return INSTANCE ?: synchronized(this) {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    WilayahDb::class.java,
                    "wilayah_cache.db"
                ).build()
                INSTANCE = db
                db
            }
        }
    }
}
