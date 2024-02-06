package com.example.ojp_android_demo.ojp.model.location

import com.example.ojp_android_demo.ojp.model.types.StopPlaceType
import com.example.ojp_android_demo.ojp.utils.TreeNode

class StopPlace(
    val stopPlaceRef: String,
    val stopPlaceName: String?,
    val topographicPlaceRef: String?,
    val stopType: StopPlaceType = StopPlaceType.StopPlace
) {
    companion object {
        fun initWithLocationTreeNode(locationTreeNode: TreeNode): StopPlace? {
            var stopType = StopPlaceType.StopPlace

            var stopPlaceRef = locationTreeNode.findTextFromChildNamed("StopPlace/StopPlaceRef")
            var stopPlaceName = locationTreeNode.findTextFromChildNamed("StopPlace/StopPlaceName/Text")
            var topographicPlaceRef = locationTreeNode.findTextFromChildNamed("StopPlace/TopographicPlaceRef")

            if (stopPlaceRef == null) {
                stopType = StopPlaceType.StopPoint
                stopPlaceRef = locationTreeNode.findTextFromChildNamed("StopPoint/StopPointRef")
                stopPlaceName = locationTreeNode.findTextFromChildNamed("StopPoint/StopPointName/Text")
                topographicPlaceRef = locationTreeNode.findTextFromChildNamed("StopPoint/TopographicPlaceRef")
            }

            if (stopPlaceRef == null) {
                stopType = StopPlaceType.StopPoint
                stopPlaceRef = locationTreeNode.findTextFromChildNamed("siri:StopPointRef")
            }

            if (stopPlaceRef == null) {
                return null
            }

            return StopPlace(stopPlaceRef, stopPlaceName, topographicPlaceRef, stopType)
        }
    }
}