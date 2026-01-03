package com.simats.cakeordering

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.simats.cakeordering.adapter.CakeAdapter
import com.simats.cakeordering.api.ApiClient
import com.simats.cakeordering.databinding.ActivityHomeBinding
import com.simats.cakeordering.model.Cake
import com.simats.cakeordering.model.ViewCakesResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var cakeAdapter: CakeAdapter
    private var allCakes: List<Cake> = emptyList()
    
    // Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var customerLatitude: Double = 0.0
    private var customerLongitude: Double = 0.0

    // Navigation items for managing selection state
    private data class NavItem(
        val indicator: FrameLayout,
        val icon: ImageView,
        val label: TextView
    )

    private lateinit var navItems: Map<String, NavItem>
    private var currentNav: String = "home"

    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        // Load saved location from SharedPreferences
        loadSavedLocation()

        setupNavigation()
        setupRecyclerView()
        loadCakes()

        // Search on keyboard action
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                filterCakes(binding.etSearch.text.toString())
                true
            } else false
        }

        // Real-time search as user types
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterCakes(s?.toString() ?: "")
            }
        })

        // Location click - get current GPS location
        binding.tvLocation.setOnClickListener {
            getCustomerLocation()
        }

        // Nearby Bakers button - pass saved location
        binding.btnNearbyBakers.setOnClickListener {
            val intent = Intent(this, NearbyBakersActivity::class.java)
            intent.putExtra("latitude", customerLatitude)
            intent.putExtra("longitude", customerLongitude)
            startActivity(intent)
        }

        // AI Cake Generator button
        binding.btnAiCake.setOnClickListener {
            startActivity(Intent(this, AiCakeGeneratorActivity::class.java))
        }

        // Notification icon - open notifications
        binding.ivNotification.setOnClickListener {
            startActivity(Intent(this, CustomerNotificationsActivity::class.java))
        }
    }

    private fun loadSavedLocation() {
        val prefs = getSharedPreferences("CakeOrderingPrefs", Context.MODE_PRIVATE)
        customerLatitude = prefs.getFloat("customer_latitude", 0f).toDouble()
        customerLongitude = prefs.getFloat("customer_longitude", 0f).toDouble()
        val savedAddress = prefs.getString("customer_location_name", null)
        
        if (savedAddress != null) {
            binding.tvLocation.text = "📍 $savedAddress"
        }
    }

    private fun getCustomerLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
            return
        }

        binding.tvLocation.text = "📍 Getting location..."
        
        val cancellationToken = CancellationTokenSource()
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationToken.token)
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    customerLatitude = location.latitude
                    customerLongitude = location.longitude
                    
                    // Save to SharedPreferences
                    val prefs = getSharedPreferences("CakeOrderingPrefs", Context.MODE_PRIVATE)
                    prefs.edit()
                        .putFloat("customer_latitude", customerLatitude.toFloat())
                        .putFloat("customer_longitude", customerLongitude.toFloat())
                        .apply()
                    
                    // Get address from coordinates
                    getAddressFromLocation(customerLatitude, customerLongitude)
                    
                    Toast.makeText(this, "Location saved! Now click 'Nearby Bakers' to find bakers", Toast.LENGTH_LONG).show()
                } else {
                    binding.tvLocation.text = "📍 Select Location"
                    Toast.makeText(this, "Could not get location", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                binding.tvLocation.text = "📍 Select Location"
                Toast.makeText(this, "Error getting location", Toast.LENGTH_SHORT).show()
            }
    }

    @Suppress("DEPRECATION")
    private fun getAddressFromLocation(latitude: Double, longitude: Double) {
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val locationName = when {
                    !address.locality.isNullOrEmpty() -> "${address.locality}, ${address.adminArea ?: ""}"
                    !address.subAdminArea.isNullOrEmpty() -> address.subAdminArea
                    else -> "Lat: ${String.format("%.2f", latitude)}"
                }
                binding.tvLocation.text = "📍 $locationName"
                
                // Save location name
                val prefs = getSharedPreferences("CakeOrderingPrefs", Context.MODE_PRIVATE)
                prefs.edit().putString("customer_location_name", locationName).apply()
            } else {
                binding.tvLocation.text = "📍 Location Set"
            }
        } catch (e: Exception) {
            binding.tvLocation.text = "📍 Location Set"
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCustomerLocation()
            } else {
                Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        cakeAdapter = CakeAdapter(emptyList())
        binding.recyclerCakes.layoutManager = LinearLayoutManager(this)
        binding.recyclerCakes.adapter = cakeAdapter
    }

    private fun loadCakes() {
        // Empty body to fetch ALL available cakes from ALL bakers
        val body = emptyMap<String, String>()

        ApiClient.api.viewCakes(body)
            .enqueue(object : Callback<ViewCakesResponse> {

                override fun onResponse(
                    call: Call<ViewCakesResponse>,
                    response: Response<ViewCakesResponse>
                ) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        allCakes = response.body()!!.data
                        cakeAdapter.updateData(allCakes)
                        updateCakeCount(allCakes.size)
                    } else {
                        Toast.makeText(this@HomeActivity, "No cakes found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ViewCakesResponse>, t: Throwable) {
                    Toast.makeText(this@HomeActivity, "Server error", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun filterCakes(query: String) {
        if (query.isEmpty()) {
            cakeAdapter.updateData(allCakes)
            updateCakeCount(allCakes.size)
        } else {
            val filtered = allCakes.filter { cake ->
                (cake.name?.contains(query, ignoreCase = true) == true) ||
                (cake.baker?.contains(query, ignoreCase = true) == true)
            }
            cakeAdapter.updateData(filtered)
            updateCakeCount(filtered.size)
        }
    }

    private fun updateCakeCount(count: Int) {
        binding.tvAvailableCakes.text = "($count)"
    }

    private fun setupNavigation() {
        // Initialize navigation items map
        navItems = mapOf(
            "home" to NavItem(
                binding.navHomeIndicator,
                binding.navHomeIcon,
                binding.navHomeLabel
            ),
            "messages" to NavItem(
                binding.navMessagesIndicator,
                binding.navMessagesIcon,
                binding.navMessagesLabel
            ),
            "cart" to NavItem(
                binding.navCartIndicator,
                binding.navCartIcon,
                binding.navCartLabel
            ),
            "profile" to NavItem(
                binding.navProfileIndicator,
                binding.navProfileIcon,
                binding.navProfileLabel
            )
        )

        // Set up click listeners
        binding.navHome.setOnClickListener {
            selectNav("home")
            // Already on home, no action needed
        }

        binding.navMessages.setOnClickListener {
            selectNav("messages")
            startActivity(Intent(this, CustomerMessagesActivity::class.java))
        }

        binding.navCart.setOnClickListener {
            selectNav("cart")
            startActivity(Intent(this, CartActivity::class.java))
        }

        binding.navProfile.setOnClickListener {
            selectNav("profile")
            startActivity(Intent(this, CustomerProfileActivity::class.java))
        }
        
        // Initialize home as selected
        initializeHomeSelected()
    }
    
    override fun onResume() {
        super.onResume()
        // Reset to home selected when coming back to this screen
        currentNav = ""  // Reset so selectNav will apply styling
        selectNav("home")
    }
    
    private fun initializeHomeSelected() {
        val accentColor = 0xFFEC4899.toInt() // Pink accent
        val unselectedColor = 0xFF9CA3AF.toInt() // Gray
        
        // Set all items to unselected first
        navItems.forEach { (_, item) ->
            item.icon.setColorFilter(unselectedColor)
            item.label.setTextColor(unselectedColor)
            item.label.setTypeface(null, android.graphics.Typeface.NORMAL)
        }
        
        // Set home as selected
        navItems["home"]?.let { item ->
            item.icon.setColorFilter(accentColor)
            item.label.setTextColor(accentColor)
            item.label.setTypeface(null, android.graphics.Typeface.BOLD)
        }
    }

    private fun selectNav(navKey: String) {
        if (currentNav == navKey) return

        val accentColor = 0xFFEC4899.toInt() // Pink accent
        val unselectedColor = 0xFF9CA3AF.toInt() // Gray

        // Deselect ALL items first
        navItems.forEach { (_, item) ->
            item.indicator.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            item.icon.setColorFilter(unselectedColor)
            item.label.setTextColor(unselectedColor)
            item.label.setTypeface(null, android.graphics.Typeface.NORMAL)
        }

        // Select the new item
        navItems[navKey]?.let { item ->
            item.icon.setColorFilter(accentColor)
            item.label.setTextColor(accentColor)
            item.label.setTypeface(null, android.graphics.Typeface.BOLD)
        }

        currentNav = navKey
    }
}
