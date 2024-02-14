package com.example.ojp_android_demo.ojp.utils

import kotlin.math.pow
import kotlin.math.roundToInt

fun Double.roundTo(places: Int): Double {
    val factor = 10.0.pow(places.toDouble())
    return (this * factor).roundToInt() / factor
}