package com.simats.cakeordering

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.simats.cakeordering.adapter.BakerCakeAdapter
import com.simats.cakeordering.api.ApiClient
import com.simats.cakeordering.databinding.ActivityBakerDashboardBinding
import com.simats.cakeordering.model.BakerCakesResponse
import com.simats.cakeordering.model.BakerDashboardResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BakerDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBakerDashboardBinding
    private lateinit var cakeAdapter: BakerCakeAdapter
    private var bakerId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBakerDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Get baker ID
        bakerId = intent.getIntExtra("baker_id", 0)
        if (bakerId == 0) {
            Toast.makeText(this, "Invalid baker", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // ✅ RecyclerView (2 columns)
        cakeAdapter = BakerCakeAdapter(mutableListOf(), bakerId)
        binding.rvMyCakes.layoutManager = GridLayoutManager(this, 2)
        binding.rvMyCakes.adapter = cakeAdapter

        // ✅ Add Cake button
        binding.btnAddCake.setOnClickListener {
            startActivity(
                Intent(this, AddCakeActivity::class.java)
                    .putExtra("baker_id", bakerId)
            )
        }

        // ✅ Monthly card click - Navigate to Analytics
        binding.cardMonthly.setOnClickListener {
            startActivity(
                Intent(this, BakerAnalyticsActivity::class.java)
                    .putExtra("baker_id", bakerId)
            )
        }

        // ✅ Bottom Navigation
        setupBottomNavigation()

        // ✅ Notification button - Navigate to notifications
        binding.btnNotification.setOnClickListener {
            startActivity(
                Intent(this, BakerNotificationsActivity::class.java)
                    .putExtra("baker_id", bakerId)
            )
        }

        // ✅ Load data from database
        loadDashboardStats()
        loadBakerCakes()
    }

    override fun onResume() {
        super.onResume()
        loadDashboardStats()
        loadBakerCakes()
    }

    // ---------------- LOAD DASHBOARD STATS ----------------

    private fun loadDashboardStats() {
        ApiClient.api.getBakerDashboard(bakerId)
            .enqueue(object : Callback<BakerDashboardResponse> {

                override fun onResponse(
                    call: Call<BakerDashboardResponse>,
                    response: Response<BakerDashboardResponse>
                ) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        val data = response.body()!!
                        
                        // Update UI with real-time data from database
                        binding.txtIncome.text = "₹${data.monthlyIncome.toInt()}"
                        binding.txtTotalOrders.text = data.totalOrders.toString()
                        binding.txtPending.text = data.pendingOrders.toString()
                    }
                }

                override fun onFailure(call: Call<BakerDashboardResponse>, t: Throwable) {
                    // Silently fail - stats will show default values
                }
            })
    }

    // ---------------- LOAD BAKER CAKES ----------------

    private fun loadBakerCakes() {
        ApiClient.api.getBakerCakes(bakerId)
            .enqueue(object : Callback<BakerCakesResponse> {

                override fun onResponse(
                    call: Call<BakerCakesResponse>,
                    response: Response<BakerCakesResponse>
                ) {
                    if (response.isSuccessful &&
                        response.body()?.status == "success"
                    ) {
                        cakeAdapter.updateData(response.body()!!.cakes)
                    }
                }

                override fun onFailure(call: Call<BakerCakesResponse>, t: Throwable) {
                    Toast.makeText(
                        this@BakerDashboardActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    // ---------------- BOTTOM NAVIGATION ----------------

    private fun setupBottomNavigation() {

        binding.bottomNavigation.selectedItemId = R.id.nav_home

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_home -> true

                R.id.nav_orders -> {
                    // Navigate to Baker Orders
                    startActivity(
                        Intent(this, BakerOrdersActivity::class.java)
                            .putExtra("baker_id", bakerId)
                    )
                    true
                }

                R.id.nav_messages -> {
                    // Navigate to Messages
                    startActivity(
                        Intent(this, BakerMessagesActivity::class.java)
                            .putExtra("baker_id", bakerId)
                    )
                    true
                }

                R.id.nav_profile -> {
                    startActivity(
                        Intent(this, BakerProfileActivity::class.java)
                            .putExtra("baker_id", bakerId)
                    )
                    true
                }

                else -> false
            }
        }
    }
}
