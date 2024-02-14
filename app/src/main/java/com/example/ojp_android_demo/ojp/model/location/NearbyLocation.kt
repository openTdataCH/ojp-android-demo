package com.example.ojp_android_demo.ojp.model.location

import com.example.ojp_android_demo.ojp.utils.roundTo
import kotlin.math.roundToInt

class NearbyLocation(public val location: Location, var distance: Double?) {
    override fun toString(): String {
        var debugParts = location.stopPlace?.stopPlaceName + " (" + (location.stopPlace?.stopPlaceRef ?: "n/a") + ")"
        if (distance is Double) {
            val doubleDistance: Double = distance as Double
            debugParts += " - " + doubleDistance.roundToInt() + " m"
        }
        return debugParts
    }

    companion object {
        fun initWithNearbyLocation(geoLocation: android.location.Location, location: Location): NearbyLocation? {
            val geoPosition = location.geoPosition ?: return  null

            val distance = geoPosition.distanceFromGeoLocation(geoLocation)
            return NearbyLocation(location, distance)
        }
    }
}