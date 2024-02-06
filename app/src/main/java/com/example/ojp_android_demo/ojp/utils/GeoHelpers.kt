package com.example.ojp_android_demo.ojp.utils

import java.math.BigDecimal
import java.math.RoundingMode

object GeoHelpers {
    fun Distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // Radius of the earth in kilometers
        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)
        val a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c * 1000
    }

    fun roundCoordinate(value: Double, scale: Int = 6): Double {
        return BigDecimal(value).setScale(scale, RoundingMode.HALF_EVEN).toDouble()
    }
}