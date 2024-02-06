package com.example.ojp_android_demo.ojp.model.location

import android.location.Location
import com.example.ojp_android_demo.ojp.utils.GeoHelpers
import com.example.ojp_android_demo.ojp.utils.TreeNode

class GeoPosition(
    val longitude: Double,
    val latitude: Double
) {
    companion object {
        fun initWithLocationTreeNode(locationTreeNode: TreeNode): GeoPosition? {
            val longitudeS = locationTreeNode.findTextFromChildNamed("GeoPosition/siri:Longitude")
            val latitudeS = locationTreeNode.findTextFromChildNamed("GeoPosition/siri:Latitude")

            if (longitudeS == null || latitudeS == null) {
                return  null
            }

            val geoPosition = GeoPosition(longitudeS.toDouble(), latitudeS.toDouble())

            return geoPosition
        }

        fun initWithGeoLocation(geoLocation: Location): GeoPosition {
            return GeoPosition(geoLocation.longitude, geoLocation.latitude)
        }
    }

    fun distanceFromAnotherGeoPosition(anotherGeoPosition: GeoPosition): Double {
        val distance = GeoHelpers.Distance(longitude, latitude, anotherGeoPosition.longitude, anotherGeoPosition.latitude)
        return distance
    }

    fun distanceFromGeoLocation(geoLocation: Location): Double {
        val distance = GeoHelpers.Distance(longitude, latitude, geoLocation.longitude, geoLocation.latitude)
        return distance
    }
}