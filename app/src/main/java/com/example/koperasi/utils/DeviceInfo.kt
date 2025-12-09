package com.example.koperasi.utils

object DeviceInfo {
    fun getDeviceInfo(): Map<String, String> {
        return mapOf(
            "os_version" to android.os.Build.VERSION.RELEASE,
            "api_level" to android.os.Build.VERSION.SDK_INT.toString(),
            "device_model" to android.os.Build.MODEL,
            "device_brand" to android.os.Build.BRAND,
            "manufacturer" to android.os.Build.MANUFACTURER
        )
    }
}