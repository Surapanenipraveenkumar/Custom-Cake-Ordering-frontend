package com.simats.cakeordering

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.simats.cakeordering.adapter.NearbyBakerAdapter
import com.simats.cakeordering.api.ApiClient
import com.simats.cakeordering.model.NearbyBakersResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NearbyBakersActivity : AppCompatActivity() {

    private lateinit var rvBakers: RecyclerView
    private lateinit var loadingLayout: LinearLayout
    private lateinit var emptyLayout: LinearLayout
    private lateinit var txtSubtitle: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    
    private var bakerAdapter: NearbyBakerAdapter? = null
    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0

    companion object {
        private const val TAG = "NearbyBakers"
        private const val LOCATION_PERMISSION_REQUEST = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nearby_bakers)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupViews()
        
        // Check if location was passed from HomeActivity
        val passedLatitude = intent.getDoubleExtra("latitude", 0.0)
        val passedLongitude = intent.getDoubleExtra("longitude", 0.0)
        
        if (passedLatitude != 0.0 && passedLongitude != 0.0) {
            // Use passed location
            currentLatitude = passedLatitude
            currentLongitude = passedLongitude
            Log.d(TAG, "Using passed location: $currentLatitude, $currentLongitude")
            showLoading()
            txtSubtitle.text = "Finding bakers within 10km..."
            loadNearbyBakers()
        } else {
            // Get current GPS location
            checkLocationPermission()
        }
    }

    private fun setupViews() {
        rvBakers = findViewById(R.id.rvBakers)
        loadingLayout = findViewById(R.id.loadingLayout)
        emptyLayout = findViewById(R.id.emptyLayout)
        txtSubtitle = findViewById(R.id.txtSubtitle)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        rvBakers.layoutManager = LinearLayoutManager(this)
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
            == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Toast.makeText(this, "Location permission required to find nearby bakers", Toast.LENGTH_LONG).show()
                showEmpty()
            }
        }
    }

    private fun getCurrentLocation() {
        showLoading()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
            != PackageManager.PERMISSION_GRANTED) {
            showEmpty()
            return
        }

        val cancellationToken = CancellationTokenSource()
        
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationToken.token
        ).addOnSuccessListener { location: Location? ->
            if (location != null) {
                currentLatitude = location.latitude
                currentLongitude = location.longitude
                Log.d(TAG, "Location: $currentLatitude, $currentLongitude")
                txtSubtitle.text = "Finding bakers near your location..."
                loadNearbyBakers()
            } else {
                Log.e(TAG, "Location is null")
                Toast.makeText(this, "Could not get location. Using default.", Toast.LENGTH_SHORT).show()
                // Use default Chennai location for testing
                currentLatitude = 13.0827
                currentLongitude = 80.2707
                loadNearbyBakers()
            }
        }.addOnFailureListener { e ->
            Log.e(TAG, "Error getting location", e)
            Toast.makeText(this, "Error getting location: ${e.message}", Toast.LENGTH_SHORT).show()
            // Use default location
            currentLatitude = 13.0827
            currentLongitude = 80.2707
            loadNearbyBakers()
        }
    }

    private fun loadNearbyBakers() {
        // Search bakers within 10km radius
        ApiClient.api.getNearbyBakers(currentLatitude, currentLongitude, 10.0)
            .enqueue(object : Callback<NearbyBakersResponse> {
                override fun onResponse(
                    call: Call<NearbyBakersResponse>,
                    response: Response<NearbyBakersResponse>
                ) {
                    hideLoading()
                    
                    if (response.isSuccessful && response.body()?.status == "success") {
                        val bakers = response.body()?.bakers ?: emptyList()
                        val count = bakers.size
                        
                        txtSubtitle.text = "Found $count bakers within 10km"
                        
                        if (bakers.isEmpty()) {
                            showEmpty()
                        } else {
                            showBakers(bakers)
                        }
                    } else {
                        Log.e(TAG, "API error: ${response.body()?.status}")
                        showEmpty()
                    }
                }

                override fun onFailure(call: Call<NearbyBakersResponse>, t: Throwable) {
                    hideLoading()
                    Log.e(TAG, "Network error", t)
                    Toast.makeText(this@NearbyBakersActivity, "Connection error", Toast.LENGTH_SHORT).show()
                    showEmpty()
                }
            })
    }

    private fun showLoading() {
        loadingLayout.visibility = View.VISIBLE
        rvBakers.visibility = View.GONE
        emptyLayout.visibility = View.GONE
    }

    private fun hideLoading() {
        loadingLayout.visibility = View.GONE
    }

    private fun showEmpty() {
        emptyLayout.visibility = View.VISIBLE
        rvBakers.visibility = View.GONE
        loadingLayout.visibility = View.GONE
    }

    private fun showBakers(bakers: List<com.simats.cakeordering.model.NearbyBaker>) {
        rvBakers.visibility = View.VISIBLE
        emptyLayout.visibility = View.GONE

        bakerAdapter = NearbyBakerAdapter(bakers) { baker ->
            // Chat button click
            Toast.makeText(this, "Chat with ${baker.shopName}", Toast.LENGTH_SHORT).show()
            // TODO: Open chat with baker
        }
        rvBakers.adapter = bakerAdapter
    }
}
