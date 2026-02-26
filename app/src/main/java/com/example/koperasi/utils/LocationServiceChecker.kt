package com.example.koperasi.utils

import android.content.Context
import android.location.LocationManager

fun Context.isLocationEnabled(): Boolean {
    val locationManager =
        getSystemService(Context.LOCATION_SERVICE) as LocationManager

    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}