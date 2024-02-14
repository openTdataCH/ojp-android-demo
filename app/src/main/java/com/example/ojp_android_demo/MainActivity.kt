package com.example.ojp_android_demo

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location as GeoLocation
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices


import com.example.ojp_android_demo.ojp.model.request.OJP
import com.example.ojp_android_demo.ojp.model.location.Location
import com.example.ojp_android_demo.ojp.model.location.NearbyLocation
import com.example.ojp_android_demo.ojp.model.request.OJP_LIR_Callback
import com.example.ojp_android_demo.ojp.network.ClientService
import com.example.ojp_android_demo.ojp.network.OJPServiceAPI
import com.example.ojp_android_demo.ojp.utils.parser.parseXmlWithPullParserCallback

class MainActivity : ComponentActivity() {
    private val DEBUG_TAG = "OJP_DEBUG"

    private lateinit var adapter: ArrayAdapter<NearbyLocation>
    private var itemsList = mutableListOf<NearbyLocation>()

    private lateinit var searchInput: EditText
    private lateinit var resultsListView: ListView
    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        searchInput = findViewById(R.id.searchInput)
        resultsListView = findViewById(R.id.resultsListView)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, itemsList)
        resultsListView.adapter = adapter

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        initDeviceGeoLocation()

        initSearchInputListener()
    }

    private fun fetchLocationInformationRequest(ojpRequest: OJP, callback: OJP_LIR_Callback) {
        val client = ClientService.createAPIService().create(OJPServiceAPI::class.java)
        val call = client.fetchOJPResponse(ojpRequest.asRequestBody())
        Log.d(DEBUG_TAG, "====================================")
        Log.d(DEBUG_TAG, ojpRequest.asXML())
        Log.d(DEBUG_TAG, "====================================")

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseXML = response.body()?.string() ?: "<foo/>"
                    parseXmlWithPullParserCallback(responseXML) { locations ->
                        callback.onSuccess(locations)
                    }
                } else {
                    Log.e(DEBUG_TAG, "TODO handle response failure")
                    callback.onFailure(response.message())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e(DEBUG_TAG, "ERROR " +  t.message)
                callback.onFailure(t.message ?: "HTTP error")
            }
        })
    }

    private fun initDeviceGeoLocation() {
        val geolocationButton = findViewById<Button>(R.id.geolocationButton)
        geolocationButton.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                    getCurrentGeoLocation()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            getCurrentGeoLocation()
        } else {
            Log.e(DEBUG_TAG, "location not granted")
        }
    }

    private fun updateLocations(locations: Array<NearbyLocation>) {
        itemsList.clear()
        itemsList.addAll(locations)
        adapter.notifyDataSetChanged()
    }

    private fun getCurrentGeoLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { geoLocation : GeoLocation? ->
            geoLocation?.let {
                val request = OJP.initWithGeoLocationAndBoxSize(geoLocation, 1000.0)
                fetchLocationInformationRequest(request, object: OJP_LIR_Callback {
                    override fun onSuccess(locations: Array<Location>) {
                        val nearbyLocations: MutableList<NearbyLocation> = mutableListOf()
                        for (location in locations) {
                            val nearbyLocation = NearbyLocation.initWithNearbyLocation(geoLocation, location)
                            if (nearbyLocation != null) {
                                nearbyLocations.add(nearbyLocation)
                            }
                        }

                        nearbyLocations.sortBy { it.distance }

                        updateLocations(nearbyLocations.toTypedArray())
                    }

                    override fun onFailure(error: String) {
                        updateLocations(emptyArray<NearbyLocation>())
                    }
                })
            }
        }
    }

    private fun initSearchInputListener() {
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchRunnable?.let { handler.removeCallbacks(it) }

                searchRunnable = Runnable {
                    val request = OJP.initWithName(s.toString())
                    fetchLocationInformationRequest(request, object: OJP_LIR_Callback {
                        override fun onSuccess(locations: Array<Location>) {
                            val nearbyLocations: MutableList<NearbyLocation> = mutableListOf()
                            for (location in locations) {
                                val nearbyLocation = NearbyLocation(location, null)
                                if (nearbyLocation != null) {
                                    nearbyLocations.add(nearbyLocation)
                                }
                            }
                            updateLocations(nearbyLocations.toTypedArray())
                        }

                        override fun onFailure(error: String) {
                            updateLocations(emptyArray<NearbyLocation>())
                        }
                    })
                }.also {
                    handler.postDelayed(it, 300)
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // Log.d(DEBUG_TAG, "afterTextChanged for: $s")
            }
        })
    }
}
