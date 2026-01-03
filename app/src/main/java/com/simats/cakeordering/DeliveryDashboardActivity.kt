package com.simats.cakeordering

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.simats.cakeordering.adapter.DeliveryOrderAdapter
import com.simats.cakeordering.api.ApiClient
import com.simats.cakeordering.databinding.ActivityDeliveryDashboardBinding
import com.simats.cakeordering.model.DeliveryDashboardResponse
import com.simats.cakeordering.model.DeliveryOrder
import com.simats.cakeordering.model.BasicResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DeliveryDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeliveryDashboardBinding
    private var deliveryId: Int = -1
    private var isOnline: Boolean = true
    private lateinit var orderAdapter: DeliveryOrderAdapter
    private var showingAvailable = true
    private var isInitialLoad = true  // Prevent switch from triggering on initial load

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDeliveryDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get delivery_id from intent or SharedPreferences
        deliveryId = intent.getIntExtra("delivery_id", -1)
        if (deliveryId == -1) {
            val prefs = getSharedPreferences("DeliveryPrefs", MODE_PRIVATE)
            deliveryId = prefs.getInt("delivery_id", -1)
        }

        if (deliveryId == -1) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, DeliveryLoginActivity::class.java))
            finish()
            return
        }

        setupRecyclerView()
        setupClickListeners()
        setupBottomNav()
        loadDashboard()
    }

    private fun setupRecyclerView() {
        orderAdapter = DeliveryOrderAdapter(
            onAcceptClick = { order -> acceptOrder(order) },
            onViewClick = { order -> viewOrderDetails(order) }
        )
        binding.rvOrders.layoutManager = LinearLayoutManager(this)
        binding.rvOrders.adapter = orderAdapter
    }

    private fun setupClickListeners() {
        // Tab switching
        binding.tabAvailable.setOnClickListener {
            showingAvailable = true
            updateTabUI()
            loadDashboard()
        }

        binding.tabActive.setOnClickListener {
            showingAvailable = false
            updateTabUI()
            loadDashboard()
        }

        // Notification button
        binding.btnNotification.setOnClickListener {
            Toast.makeText(this, "Delivery notifications coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Online/Offline toggle switch
        binding.switchOnline.setOnCheckedChangeListener { _, isChecked ->
            if (!isInitialLoad) {
                toggleOnlineStatus(isChecked)
            }
        }
    }

    private fun toggleOnlineStatus(goOnline: Boolean) {
        val onlineValue = if (goOnline) 1 else 0
        
        ApiClient.api.toggleDeliveryOnline(deliveryId, onlineValue)
            .enqueue(object : Callback<BasicResponse> {
                override fun onResponse(
                    call: Call<BasicResponse>,
                    response: Response<BasicResponse>
                ) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        isOnline = goOnline
                        updateOnlineUI()
                        val msg = if (goOnline) "You are now Online - Ready to receive orders!" else "You are now Offline"
                        Toast.makeText(this@DeliveryDashboardActivity, msg, Toast.LENGTH_SHORT).show()
                        
                        // Reload dashboard to show/hide orders based on status
                        if (goOnline) loadDashboard()
                    } else {
                        // Revert switch on failure
                        binding.switchOnline.isChecked = !goOnline
                        Toast.makeText(this@DeliveryDashboardActivity, "Failed to update status", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                    // Revert switch on failure
                    binding.switchOnline.isChecked = !goOnline
                    Toast.makeText(this@DeliveryDashboardActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun updateOnlineUI() {
        if (isOnline) {
            binding.tvOnlineStatus.text = "Active Now"
            binding.statusDot.setBackgroundResource(R.drawable.bg_dot_green)
        } else {
            binding.tvOnlineStatus.text = "Offline"
            binding.statusDot.setBackgroundResource(R.drawable.bg_dot_red)
        }
    }

    private fun updateTabUI() {
        if (showingAvailable) {
            binding.tabAvailable.setBackgroundResource(R.drawable.bg_tab_selected)
            binding.tabAvailable.setTextColor(resources.getColor(android.R.color.white, null))
            binding.tabActive.setBackgroundResource(0)
            binding.tabActive.setTextColor(resources.getColor(R.color.gray_600, null))
        } else {
            binding.tabActive.setBackgroundResource(R.drawable.bg_tab_selected)
            binding.tabActive.setTextColor(resources.getColor(android.R.color.white, null))
            binding.tabAvailable.setBackgroundResource(0)
            binding.tabAvailable.setTextColor(resources.getColor(R.color.gray_600, null))
        }
    }

    private fun setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_orders -> {
                    // Already on orders
                    true
                }
                R.id.nav_history -> {
                    startActivity(Intent(this, DeliveryOrdersActivity::class.java))
                    true
                }
                R.id.nav_earnings -> {
                    // Show earnings - could create a new activity
                    Toast.makeText(this, "Earnings feature coming soon", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, DeliveryProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun loadDashboard() {
        ApiClient.api.getDeliveryDashboard(deliveryId)
            .enqueue(object : Callback<DeliveryDashboardResponse> {
                override fun onResponse(
                    call: Call<DeliveryDashboardResponse>,
                    response: Response<DeliveryDashboardResponse>
                ) {
                    val body = response.body()
                    if (response.isSuccessful && body?.status == "success") {
                        updateUI(body)
                    } else {
                        Toast.makeText(
                            this@DeliveryDashboardActivity,
                            body?.message ?: "Failed to load dashboard",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<DeliveryDashboardResponse>, t: Throwable) {
                    Toast.makeText(
                        this@DeliveryDashboardActivity,
                        "Network error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun updateUI(data: DeliveryDashboardResponse) {
        // Update stats
        val earnings = data.todayEarnings ?: 0.0
        binding.tvEarnings.text = if (earnings >= 1000) "₹${(earnings/1000).toInt()}k" else "₹${earnings.toInt()}"
        binding.tvTotalOrders.text = "${data.totalDeliveries ?: 0}"
        binding.tvRating.text = "4.9" // Placeholder - you can add rating to API
        
        isOnline = (data.isOnline ?: 0) == 1
        binding.switchOnline.isChecked = isOnline
        isInitialLoad = false  // Allow future toggles to trigger API
        updateOnlineUI()

        val orders = data.pendingOrders ?: emptyList()
        
        // Filter based on tab
        val filteredOrders = if (showingAvailable) {
            orders.filter { it.deliveryStatus == "pending" || it.isAssigned != true }
        } else {
            orders.filter { it.isAssigned == true && it.deliveryStatus != "delivered" }
        }
        
        orderAdapter.updateData(filteredOrders)
        
        // Update tab counts
        val availableCount = orders.count { it.deliveryStatus == "pending" || it.isAssigned != true }
        val activeCount = orders.count { it.isAssigned == true && it.deliveryStatus != "delivered" }
        binding.tabAvailable.text = "Available ($availableCount)"
        binding.tabActive.text = "Active ($activeCount)"
        
        binding.emptyState.visibility = if (filteredOrders.isEmpty()) View.VISIBLE else View.GONE
        binding.rvOrders.visibility = if (filteredOrders.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun acceptOrder(order: DeliveryOrder) {
        val body = mapOf(
            "delivery_id" to deliveryId,
            "order_id" to order.orderId,
            "action" to "accept"
        )

        ApiClient.api.updateDeliveryStatus(body)
            .enqueue(object : Callback<BasicResponse> {
                override fun onResponse(
                    call: Call<BasicResponse>,
                    response: Response<BasicResponse>
                ) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        Toast.makeText(
                            this@DeliveryDashboardActivity,
                            "Order accepted!",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadDashboard()
                    } else {
                        Toast.makeText(
                            this@DeliveryDashboardActivity,
                            response.body()?.message ?: "Failed to accept order",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                    Toast.makeText(
                        this@DeliveryDashboardActivity,
                        "Network error",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun viewOrderDetails(order: DeliveryOrder) {
        val intent = Intent(this, DeliveryOrderDetailActivity::class.java)
        intent.putExtra("order_id", order.orderId)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        loadDashboard()
    }
}
