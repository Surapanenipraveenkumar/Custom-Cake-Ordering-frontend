package com.simats.cakeordering

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simats.cakeordering.adapter.CustomerOrdersAdapter
import com.simats.cakeordering.api.ApiClient
import com.simats.cakeordering.model.CustomerOrder
import com.simats.cakeordering.model.CustomerOrdersResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.content.Intent

class CustomerOrdersActivity : AppCompatActivity() {

    private lateinit var ordersAdapter: CustomerOrdersAdapter
    private var allOrders: List<CustomerOrder> = emptyList()
    private var userId: Int = 0

    private lateinit var btnAllOrders: Button
    private lateinit var btnInProgress: Button
    private lateinit var btnCompleted: Button
    private lateinit var btnCancelled: Button

    private var currentFilter = "all"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_orders)

        val prefs = getSharedPreferences("CakeOrderingPrefs", Context.MODE_PRIVATE)
        userId = prefs.getInt("user_id", 0)

        setupViews()
        loadOrders()
    }

    private fun setupViews() {
        // Back button
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Filter buttons
        btnAllOrders = findViewById(R.id.btnAllOrders)
        btnInProgress = findViewById(R.id.btnInProgress)
        btnCompleted = findViewById(R.id.btnCompleted)
        btnCancelled = findViewById(R.id.btnCancelled)

        btnAllOrders.setOnClickListener { filterOrders("all") }
        btnInProgress.setOnClickListener { filterOrders("in_progress") }
        btnCompleted.setOnClickListener { filterOrders("completed") }
        btnCancelled.setOnClickListener { filterOrders("cancelled") }

        // Orders RecyclerView
        val rvOrders = findViewById<RecyclerView>(R.id.rvOrders)
        ordersAdapter = CustomerOrdersAdapter(emptyList()) { order ->
            // Navigate to Order Tracking with all data
            val intent = Intent(this, OrderTrackingActivity::class.java)
            intent.putExtra("order_id", order.orderId)
            intent.putExtra("order_id_str", order.orderIdStr)
            intent.putExtra("status", order.status)
            intent.putExtra("total_amount", order.totalAmount)
            intent.putExtra("order_date", order.orderDate)
            intent.putExtra("order_time", order.orderTime ?: "")
            intent.putExtra("delivery_address", order.deliveryAddress ?: "")
            intent.putExtra("baker_name", order.bakerName)
            startActivity(intent)
        }
        rvOrders.layoutManager = LinearLayoutManager(this)
        rvOrders.adapter = ordersAdapter
    }

    private fun loadOrders() {
        android.util.Log.d("CustomerOrders", "Loading orders for user_id: $userId")
        Toast.makeText(this, "Loading orders...", Toast.LENGTH_SHORT).show()
        
        ApiClient.api.getCustomerOrders(userId)
            .enqueue(object : Callback<CustomerOrdersResponse> {
                override fun onResponse(
                    call: Call<CustomerOrdersResponse>,
                    response: Response<CustomerOrdersResponse>
                ) {
                    android.util.Log.d("CustomerOrders", "Response code: ${response.code()}")
                    android.util.Log.d("CustomerOrders", "Response body: ${response.body()}")
                    
                    if (response.isSuccessful && response.body()?.status == "success") {
                        val data = response.body()!!
                        allOrders = data.orders
                        
                        android.util.Log.d("CustomerOrders", "Loaded ${data.orders.size} orders")
                        
                        findViewById<TextView>(R.id.tvTotalOrders).text = 
                            "${data.totalOrders} total orders"
                        
                        updateOrdersList()
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "Unknown error"
                        android.util.Log.e("CustomerOrders", "Error: $errorBody")
                        Toast.makeText(
                            this@CustomerOrdersActivity,
                            "Failed: ${response.body()?.status ?: errorBody}",
                            Toast.LENGTH_LONG
                        ).show()
                        showEmpty()
                    }
                }

                override fun onFailure(call: Call<CustomerOrdersResponse>, t: Throwable) {
                    android.util.Log.e("CustomerOrders", "Connection error", t)
                    Toast.makeText(
                        this@CustomerOrdersActivity,
                        "Connection error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    showEmpty()
                }
            })
    }

    private fun filterOrders(filter: String) {
        currentFilter = filter
        updateButtonStyles()
        updateOrdersList()
    }

    private fun updateButtonStyles() {
        val selectedBg = 0xFFEC4899.toInt()
        val unselectedBg = 0xFFE5E7EB.toInt()
        val selectedText = android.graphics.Color.WHITE
        val unselectedText = 0xFF374151.toInt()

        // Reset all buttons
        listOf(btnAllOrders, btnInProgress, btnCompleted, btnCancelled).forEach { btn ->
            btn.setTextColor(unselectedText)
            btn.backgroundTintList = android.content.res.ColorStateList.valueOf(unselectedBg)
        }

        // Highlight selected button
        val selectedBtn = when (currentFilter) {
            "all" -> btnAllOrders
            "in_progress" -> btnInProgress
            "completed" -> btnCompleted
            "cancelled" -> btnCancelled
            else -> btnAllOrders
        }
        selectedBtn.setTextColor(selectedText)
        selectedBtn.backgroundTintList = android.content.res.ColorStateList.valueOf(selectedBg)
    }

    private fun updateOrdersList() {
        val filtered = when (currentFilter) {
            "all" -> allOrders
            "in_progress" -> allOrders.filter { 
                it.status.lowercase() in listOf("pending", "in_progress", "in progress") 
            }
            "completed" -> allOrders.filter { 
                it.status.lowercase() in listOf("ready", "delivered") 
            }
            "cancelled" -> allOrders.filter { 
                it.status.lowercase() == "cancelled" 
            }
            else -> allOrders
        }

        if (filtered.isEmpty()) {
            showEmpty()
        } else {
            hideEmpty()
            ordersAdapter.updateData(filtered)
        }
    }

    private fun showEmpty() {
        findViewById<RecyclerView>(R.id.rvOrders).visibility = View.GONE
        findViewById<LinearLayout>(R.id.emptyState).visibility = View.VISIBLE
    }

    private fun hideEmpty() {
        findViewById<RecyclerView>(R.id.rvOrders).visibility = View.VISIBLE
        findViewById<LinearLayout>(R.id.emptyState).visibility = View.GONE
    }
}
