package com.example.ojp_android_demo.ojp.model.request

import android.location.Location
import com.example.ojp_android_demo.ojp.model.location.GeoPosition
import com.example.ojp_android_demo.ojp.utils.GeoHelpers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.NamespaceList
import org.simpleframework.xml.Order
import org.simpleframework.xml.Root
import org.simpleframework.xml.core.Persister
import java.io.StringWriter

@Root(name = "OJP", strict = false)
@NamespaceList(
    Namespace(reference = "http://www.vdv.de/ojp"),
    Namespace(reference = "http://www.siri.org.uk/siri", prefix = "siri"),
    Namespace(reference = "http://www.w3.org/2001/XMLSchema-instance", prefix = "xsi"),
    Namespace(reference = "http://www.w3.org/2001/XMLSchema", prefix = "xsd")
)
@Attribute(name = "version")
data class OJP(
    @field:Element(name = "OJPRequest") var ojpRequest: OJPRequest,
    @field:Attribute(name = "version") var version: String = "2.0"
) {
    companion object {
        private fun computeOJPRequest(initialInput: InitialInput): OJP {
            // TODO - remove hardcoding
            val serviceRequest = ServiceRequest(
                requestTimestamp = "2024-01-23T14:05:58.271Z",
                requestorRef = "OJP_JS_SDK_v0.9.24",
                locationInformationRequest = LocationInformationRequest(
                    requestTimestamp = "2024-01-23T14:05:58.271Z",
                    initialInput = initialInput,
                    restrictions = Restrictions(
                        type = "stop",
                        numberOfResults = 100
                    )
                )
            )
            val ojpRequest = OJPRequest(serviceRequest)
            return OJP(ojpRequest)
        }

        fun initWithName(name: String): OJP {
            val initialInput = InitialInput(name = name)
            return computeOJPRequest(initialInput)
        }

        fun initWithGeoLocationAndBoxSize(geoLocation: Location, boxWidth: Double, boxHeight: Double? = null): OJP {
            val initialInput = InitialInput(
                geoRestriction = GeoRestriction(
                    rectangle = Rectangle.initWithGeoLocationAndBoxSize(geoLocation, boxWidth = boxWidth, boxHeight = boxHeight)
                )
            )

            return computeOJPRequest(initialInput)
        }

        fun initWithBBOXCoordinates(boxXMin: Double, boxYMin: Double, boxXMax: Double, boxYMax: Double): OJP {
            val initialInput = InitialInput(
                geoRestriction = GeoRestriction(
                    rectangle = Rectangle(
                        upperLeft = Point(boxXMin, boxYMax),
                        lowerRight = Point(boxXMax, boxYMin)
                    )
                )
            )

            return computeOJPRequest(initialInput)
        }

        fun initWithRectangle(rectangle: Rectangle): OJP {
            val initialInput = InitialInput(
                geoRestriction = GeoRestriction(rectangle = rectangle)
            )

            return computeOJPRequest(initialInput)
        }
    }

    fun asXML(): String {
        val serializer = Persister()
        val result = StringWriter()
        try {
            serializer.write(this, result)
        } catch (e: Exception) {
            // Handle exception
            e.printStackTrace()
        }
        return result.toString()
    }

    fun asRequestBody():RequestBody {
        val xml = asXML()
        val mediaType = "application/xml".toMediaType()
        return xml.toRequestBody(mediaType)
    }
}

@Root(name = "OJPRequest", strict = false)
data class OJPRequest(
    @field:Element(name = "ServiceRequest") var serviceRequest: ServiceRequest
)

@Root(name = "ServiceRequest", strict = false)
@Namespace(reference = "http://www.siri.org.uk/siri")
@Order(elements = ["RequestTimestamp", "RequestorRef", "OJPLocationInformationRequest"])
data class ServiceRequest(
    @field:Element(name = "RequestTimestamp")
    @field:Namespace(reference = "http://www.siri.org.uk/siri")
    var requestTimestamp: String,

    @field:Element(name = "RequestorRef")
    @field:Namespace(reference = "http://www.siri.org.uk/siri")
    var requestorRef: String,

    @field:Element(name = "OJPLocationInformationRequest")
    var locationInformationRequest: LocationInformationRequest
)

@Root(name = "LocationInformationRequest", strict = false)
@Order(elements = ["RequestTimestamp", "InitialInput", "Restrictions"])
data class LocationInformationRequest(
    @field:Element(name = "RequestTimestamp")
    @field:Namespace(reference = "http://www.siri.org.uk/siri")
    var requestTimestamp: String,

    @field:Element(name = "InitialInput")
    var initialInput: InitialInput,

    @field:Element(name = "Restrictions")
    var restrictions: Restrictions
)

@Root(name = "InitialInput", strict = false)
data class InitialInput(
    @field:Element(name = "GeoRestriction", required = false)
    var geoRestriction: GeoRestriction? = null,

    @field:Element(name = "Name", required = false)
    var name: String? = null
)

@Root(name = "GeoRestriction", strict = false)
data class GeoRestriction(
    @field:Element(name = "Rectangle")
    var rectangle: Rectangle
)

@Root(name = "Rectangle", strict = false)
@Order(elements = ["UpperLeft", "LowerRight"])
data class Rectangle(
    @field:Element(name = "UpperLeft")
    var upperLeft: Point,

    @field:Element(name = "LowerRight")
    var lowerRight: Point
) {
    companion object {
        fun initWithBBOXCoordinates(boxXMin: Double, boxYMin: Double, boxXMax: Double, boxYMax: Double): Rectangle {
            val rectangle = Rectangle(
                upperLeft = Point(boxXMin, boxYMax),
                lowerRight = Point(boxXMax, boxYMin)
            )

            return rectangle
        }

        fun initWithGeoLocationAndBoxSize(geoLocation: Location, boxWidth: Double, boxHeight: Double? = null): Rectangle {
            return initWithGeoPositionAndBoxSize(GeoPosition.initWithGeoLocation(geoLocation), boxWidth, boxHeight)
        }

        fun initWithGeoPositionAndBoxSize(geoPosition: GeoPosition, boxWidth: Double, boxHeightParam: Double? = null): Rectangle {
            val boxHeight = boxHeightParam ?: boxWidth

            val geoPositionShiftLongitude = GeoPosition(geoPosition.longitude + 1, geoPosition.latitude)
            val geoPositionShiftLatitude = GeoPosition(geoPosition.longitude, geoPosition.latitude + 1)
            val longitudeDegreeLength = geoPositionShiftLongitude.distanceFromAnotherGeoPosition(geoPosition)
            val latitudeDegreeLength = geoPositionShiftLatitude.distanceFromAnotherGeoPosition(geoPosition)

            val deltaLongitude = boxWidth / longitudeDegreeLength
            val deltaLatitude = boxWidth / latitudeDegreeLength

            val boxXMin = GeoHelpers.roundCoordinate(geoPosition.longitude - deltaLongitude / 2)
            val boxYMin = GeoHelpers.roundCoordinate(geoPosition.latitude - deltaLatitude / 2)
            val boxXMax = GeoHelpers.roundCoordinate(geoPosition.longitude + deltaLongitude / 2)
            val boxYMax = GeoHelpers.roundCoordinate(geoPosition.latitude + deltaLatitude / 2)

            val rectangle = Rectangle.initWithBBOXCoordinates(boxXMin = boxXMin, boxYMin = boxYMin, boxXMax = boxXMax, boxYMax = boxYMax)
            return rectangle
        }
    }
}

@Root(strict = false)
@Order(elements = ["Longitude", "Latitude"])
data class Point(
    @field:Element(name = "Longitude")
    @field:Namespace(reference = "http://www.siri.org.uk/siri")
    var longitude: Double,

    @field:Element(name = "Latitude")
    @field:Namespace(reference = "http://www.siri.org.uk/siri")
    var latitude: Double
)

@Root(name = "Restrictions", strict = false)
@Order(elements = ["Type", "NumberOfResults"])
data class Restrictions(
    @field:Element(name = "Type")
    var type: String,

    @field:Element(name = "NumberOfResults")
    var numberOfResults: Int
)
