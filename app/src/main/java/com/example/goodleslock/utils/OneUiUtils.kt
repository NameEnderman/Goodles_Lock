package com.example.goodleslock.utils

import android.os.Build
import android.util.Log
import java.lang.reflect.Method

object OneUiUtils {
    private const val TAG = "OneUiUtils"

    fun getOneUiVersionString(): String {
        return try {
            val systemPropertiesClass: Class<*> = Class.forName("android.os.SystemProperties")
            val getMethod: Method = systemPropertiesClass.getMethod("get", String::class.java, String::class.java)
            
            val oneUiVer = getMethod.invoke(null, "ro.build.version.oneui", "") as? String
            if (!oneUiVer.isNullOrBlank()) {
                return formatOneUiVersion(oneUiVer)
            }

            val displayId = Build.DISPLAY
            if (displayId.contains("One UI", ignoreCase = true)) {
                return displayId
            }

            "6.0"
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting One UI version", e)
            "6.0"
        }
    }

    private fun formatOneUiVersion(raw: String): String {
        val intVal = raw.toIntOrNull()
        if (intVal != null) {
            val major = intVal / 10000
            val minor = (intVal % 10000) / 100
            if (major > 0) {
                return if (minor > 0) "$major.$minor" else "$major.0"
            }
        }
        return raw
    }

    fun isSamsungDevice(): Boolean {
        return Build.MANUFACTURER.equals("samsung", ignoreCase = true)
    }
}
