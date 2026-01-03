package com.simats.cakeordering

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.simats.cakeordering.adapter.DeliveryOrderAdapter
import com.simats.cakeordering.api.ApiClient
import com.simats.cakeordering.databinding.ActivityDeliveryOrdersBinding
import com.simats.cakeordering.model.DeliveryOrder
import com.simats.cakeordering.model.DeliveryOrdersResponse
import com.simats.cakeordering.model.BasicResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DeliveryOrdersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeliveryOrdersBinding
    private var deliveryId: Int = -1
    private lateinit var orderAdapter: DeliveryOrderAdapter
    private var currentStatus = "all"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDeliveryOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("DeliveryPrefs", MODE_PRIVATE)
        deliveryId = prefs.getInt("delivery_id", -1)

        if (deliveryId == -1) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupRecyclerView()
        setupTabs()
        setupClickListeners()
        loadOrders("all")
    }

    private fun setupRecyclerView() {
        orderAdapter = DeliveryOrderAdapter(
            onAcceptClick = { order -> updateOrderStatus(order, "accept") },
            onViewClick = { order -> viewOrderDetails(order) }
        )
        binding.rvOrders.layoutManager = LinearLayoutManager(this)
        binding.rvOrders.adapter = orderAdapter
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentStatus = when (tab?.position) {
                    0 -> "all"
                    1 -> "pending"
                    2 -> "picked_up"
                    3 -> "delivered"
                    else -> "all"
                }
                loadOrders(currentStatus)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.swipeRefresh.setOnRefreshListener {
            loadOrders(currentStatus)
        }
    }

    private fun loadOrders(status: String) {
        binding.progressBar.visibility = View.VISIBLE

        ApiClient.api.getDeliveryOrders(deliveryId, status)
            .enqueue(object : Callback<DeliveryOrdersResponse> {
                override fun onResponse(
                    call: Call<DeliveryOrdersResponse>,
                    response: Response<DeliveryOrdersResponse>
                ) {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false

                    val body = response.body()
                    if (response.isSuccessful && body?.status == "success") {
                        val orders = body.orders ?: emptyList()
                        orderAdapter.updateData(orders)
                        binding.emptyState.visibility = if (orders.isEmpty()) View.VISIBLE else View.GONE
                    } else {
                        Toast.makeText(
                            this@DeliveryOrdersActivity,
                            body?.message ?: "Failed to load orders",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<DeliveryOrdersResponse>, t: Throwable) {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
                    Toast.makeText(
                        this@DeliveryOrdersActivity,
                        "Network error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun updateOrderStatus(order: DeliveryOrder, action: String) {
        val body = mapOf(
            "delivery_id" to deliveryId,
            "order_id" to order.orderId,
            "action" to action
        )

        ApiClient.api.updateDeliveryStatus(body)
            .enqueue(object : Callback<BasicResponse> {
                override fun onResponse(
                    call: Call<BasicResponse>,
                    response: Response<BasicResponse>
                ) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        Toast.makeText(
                            this@DeliveryOrdersActivity,
                            response.body()?.message ?: "Success",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadOrders(currentStatus)
                    }
                }

                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                    Toast.makeText(
                        this@DeliveryOrdersActivity,
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
}
