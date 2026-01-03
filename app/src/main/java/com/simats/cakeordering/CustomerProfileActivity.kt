package com.simats.cakeordering

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simats.cakeordering.adapter.ProfileOrderAdapter
import com.simats.cakeordering.api.ApiClient
import com.simats.cakeordering.model.CustomerProfileResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CustomerProfileActivity : AppCompatActivity() {

    private lateinit var orderAdapter: ProfileOrderAdapter
    private var userId: Int = 0
    
    // Store current profile data for Edit Profile
    private var currentName: String = ""
    private var currentEmail: String = ""
    private var currentPhone: String = ""
    private var currentAddress: String = ""

    // Activity Result API launcher for edit profile
    private val editProfileLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // Reload profile after edit
            loadProfile()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_profile)

        // Get user ID from SharedPreferences
        val prefs = getSharedPreferences("CakeOrderingPrefs", Context.MODE_PRIVATE)
        userId = prefs.getInt("user_id", 0)

        // Debug: Show which user_id is being used
        android.util.Log.d("CustomerProfile", "User ID from SharedPreferences: $userId")

        if (userId == 0) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            navigateToLogin()
            return
        }

        // Show loading toast
        Toast.makeText(this, "Loading profile for user #$userId...", Toast.LENGTH_SHORT).show()

        setupViews()
        loadProfile()
    }

    private fun setupViews() {
        // Back button
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Orders RecyclerView
        val rvOrders = findViewById<RecyclerView>(R.id.rvRecentOrders)
        orderAdapter = ProfileOrderAdapter(emptyList())
        rvOrders.layoutManager = LinearLayoutManager(this)
        rvOrders.adapter = orderAdapter

        // View All Orders
        findViewById<TextView>(R.id.tvViewAllOrders).setOnClickListener {
            startActivity(Intent(this, CustomerOrdersActivity::class.java))
        }

        // Edit Profile button - using Activity Result API
        findViewById<Button>(R.id.btnEditProfile).setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            intent.putExtra("name", currentName)
            intent.putExtra("email", currentEmail)
            intent.putExtra("phone", currentPhone)
            intent.putExtra("address", currentAddress)
            editProfileLauncher.launch(intent)
        }

        // Favorites button
        findViewById<Button>(R.id.btnFavorites).setOnClickListener {
            startActivity(Intent(this, FavoritesActivity::class.java))
        }

        // Logout button
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun loadProfile() {
        android.util.Log.d("CustomerProfile", "Loading profile for userId: $userId")
        
        ApiClient.api.getCustomerProfile(userId)
            .enqueue(object : Callback<CustomerProfileResponse> {
                override fun onResponse(
                    call: Call<CustomerProfileResponse>,
                    response: Response<CustomerProfileResponse>
                ) {
                    android.util.Log.d("CustomerProfile", "Response code: ${response.code()}")
                    android.util.Log.d("CustomerProfile", "Response body: ${response.body()}")
                    
                    if (response.isSuccessful && response.body()?.status == "success") {
                        val data = response.body()!!
                        android.util.Log.d("CustomerProfile", "Profile loaded: ${data.profile?.name}")
                        displayProfile(data)
                    } else {
                        val errorBody = response.errorBody()?.string()
                        android.util.Log.e("CustomerProfile", "Error: $errorBody")
                        Toast.makeText(
                            this@CustomerProfileActivity,
                            "Failed to load profile: ${response.body()?.status ?: "Error ${response.code()}"}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<CustomerProfileResponse>, t: Throwable) {
                    android.util.Log.e("CustomerProfile", "Connection error", t)
                    Toast.makeText(
                        this@CustomerProfileActivity,
                        "Connection error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun displayProfile(data: CustomerProfileResponse) {
        // Store current profile data for Edit Profile
        currentName = data.profile?.name ?: ""
        currentEmail = data.profile?.email ?: ""
        currentPhone = data.profile?.phone ?: ""
        currentAddress = data.profile?.address ?: ""
        
        // Header info
        findViewById<TextView>(R.id.tvUserName).text = data.profile?.name ?: "User"
        findViewById<TextView>(R.id.tvUserEmail).text = data.profile?.email ?: ""
        findViewById<TextView>(R.id.tvMemberSince).text = "⭐ Member since ${data.memberSince ?: "Recently"}"

        // Stats
        findViewById<TextView>(R.id.tvTotalOrders).text = "${data.stats?.totalOrders ?: 0}"
        findViewById<TextView>(R.id.tvPendingOrders).text = "${data.stats?.pendingOrders ?: 0}"
        findViewById<TextView>(R.id.tvDeliveredOrders).text = "${data.stats?.deliveredOrders ?: 0}"

        // Personal info
        findViewById<TextView>(R.id.tvInfoName).text = data.profile?.name ?: "Not provided"
        findViewById<TextView>(R.id.tvInfoEmail).text = data.profile?.email ?: "Not provided"
        findViewById<TextView>(R.id.tvInfoPhone).text = 
            if (data.profile?.phone.isNullOrEmpty()) "Not provided" else data.profile?.phone
        findViewById<TextView>(R.id.tvInfoAddress).text = 
            if (data.profile?.address.isNullOrEmpty()) "Not provided" else data.profile?.address

        // Recent orders - show only 2 orders on profile
        val allOrders = data.recentOrders ?: emptyList()
        val limitedOrders = allOrders.take(2) // Show only first 2 orders
        
        if (allOrders.isEmpty()) {
            findViewById<TextView>(R.id.tvNoOrders).visibility = View.VISIBLE
            findViewById<RecyclerView>(R.id.rvRecentOrders).visibility = View.GONE
            findViewById<TextView>(R.id.tvViewAllOrders).visibility = View.GONE
        } else {
            findViewById<TextView>(R.id.tvNoOrders).visibility = View.GONE
            findViewById<RecyclerView>(R.id.rvRecentOrders).visibility = View.VISIBLE
            orderAdapter.updateData(limitedOrders)
            
            // Show "View All" only if there are more than 2 orders
            val viewAllText = findViewById<TextView>(R.id.tvViewAllOrders)
            if (allOrders.size > 2) {
                viewAllText.visibility = View.VISIBLE
            } else {
                viewAllText.visibility = View.GONE
            }
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        // Clear SharedPreferences
        val prefs = getSharedPreferences("CakeOrderingPrefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        navigateToLogin()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginTypeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
