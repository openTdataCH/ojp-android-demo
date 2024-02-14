package com.example.ojp_android_demo.ojp.utils.parser

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader

import com.example.ojp_android_demo.ojp.model.location.Location
import com.example.ojp_android_demo.ojp.utils.TreeNode

typealias XmlElementCallback = (Array<Location>) -> Unit

fun parseXmlWithPullParserCallback(xmlString: String, callback: XmlElementCallback) {
    val factory = XmlPullParserFactory.newInstance()
    factory.isNamespaceAware = true
    val xpp = factory.newPullParser()

    xpp.setInput(StringReader(xmlString))

    var eventType = xpp.eventType
    val rootNode = TreeNode("root", null, mutableMapOf(), mutableListOf(), null)
    var currentNode = rootNode
    val stack = mutableListOf<TreeNode>()

    // parser specific
    val locations: MutableList<Location> = mutableListOf()

    while (eventType != XmlPullParser.END_DOCUMENT) {
        when (eventType) {
            XmlPullParser.START_TAG -> {
                val nodeName = buildString {
                    if (xpp.prefix != null) {
                        append(xpp.prefix + ":")
                    }
                    append(xpp.name)
                }

                val attributes = mutableMapOf<String, String>()
                for (i in 0 until xpp.attributeCount) {
                    attributes[xpp.getAttributeName(i)] = xpp.getAttributeValue(i)
                }
                val newNode = TreeNode(nodeName, currentNode.name, attributes, mutableListOf(), null)
                currentNode.children.add(newNode)
                stack.add(newNode)
                currentNode = newNode
            }
            XmlPullParser.END_TAG -> {
                // set the text to null if needed, otherwise will contain \n chars
                if (currentNode.children.size > 0){
                    currentNode.text = null
                }

                // remove currentNode from stack
                stack.removeAt(stack.size - 1)

                if (xpp.name == "PlaceResult") {
                    val location = Location.initWithPlaceResultTreeNode(currentNode)
                    if (location.stopPlace === null || location.geoPosition == null) {
                        println("whoops, cant init location stopPlace")
                    } else {
                        locations.add(location)
                    }
                }

                // this happen on XmlPullParser.END_DOCUMENT
                if (stack.isNotEmpty()) {
                    currentNode = stack.last()
                }
            }
            XmlPullParser.TEXT -> {
                currentNode.text = xpp.text
            }
        }
        eventType = xpp.next()
    }

    callback(locations.toTypedArray())
}