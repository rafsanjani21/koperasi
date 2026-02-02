package com.example.koperasi.utils

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object LocationHelper {

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): String =
        suspendCancellableCoroutine { cont ->

            val client = LocationServices.getFusedLocationProviderClient(context)

            val request = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                1000
            ).setMaxUpdates(1).build()

            val callback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val loc = result.lastLocation
                    if (loc != null) {
                        cont.resume("${loc.latitude},${loc.longitude}")
                    } else {
                        cont.resume("UNKNOWN_LOCATION")
                    }
                    client.removeLocationUpdates(this)
                }
            }

            client.requestLocationUpdates(
                request,
                callback,
                context.mainLooper
            )
        }
}
