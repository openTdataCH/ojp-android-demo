package com.example.ojp_android_demo.ojp.model.location

import com.example.ojp_android_demo.ojp.utils.TreeNode

open class Location (
    var geoPosition: GeoPosition?,
    var stopPlace: StopPlace?
) {
    companion object {
        fun Empty(): Location {
            val location = Location(null, null)
            return  location
        }

        fun initWithPlaceResultTreeNode(placeResultTreeNode: TreeNode): Location {
            val location = Location.Empty()

            val placeTreeNode = placeResultTreeNode.findChildNamed("Place")
            if (placeTreeNode != null) {
                location.stopPlace = StopPlace.initWithLocationTreeNode(placeTreeNode)
                location.geoPosition = GeoPosition.initWithLocationTreeNode(placeTreeNode)
            }

            return location
        }
    }
}
