package com.example.ojp_android_demo.ojp.network

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface OJPServiceAPI {
    @Headers(
        // registered by ojp_android_demo@m23.ch
        "Authorization: eyJvcmciOiI2NDA2NTFhNTIyZmEwNTAwMDEyOWJiZTEiLCJpZCI6ImE2NmQyNWRkNzcyNDRmMTA4ZmMwODljYzczZmZlODBhIiwiaCI6Im11cm11cjEyOCJ9",
        "Content-Type: text/xml",
    )
    @POST("ojp20")
    fun fetchOJPResponse(@Body requestBody: RequestBody): Call<ResponseBody>
}
