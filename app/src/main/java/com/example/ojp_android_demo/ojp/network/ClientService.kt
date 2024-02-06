package com.example.ojp_android_demo.ojp.network

import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory

object ClientService {
    fun createAPIService(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.opentransportdata.swiss")
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .build()
    }
}
