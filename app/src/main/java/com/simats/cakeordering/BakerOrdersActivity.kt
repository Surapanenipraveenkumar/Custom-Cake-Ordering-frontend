package com.simats.cakeordering

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simats.cakeordering.adapter.BakerOrderAdapter
import com.simats.cakeordering.api.ApiClient
import com.simats.cakeordering.model.BakerOrder
import com.simats.cakeordering.model.BakerOrdersResponse
import com.simats.cakeordering.model.BasicResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BakerOrdersActivity : AppCompatActivity() {

    private lateinit var rvOrders: RecyclerView
    private lateinit var txtEmpty: TextView
    private lateinit var orderAdapter: BakerOrderAdapter

    private var bakerId: Int = 0
    private var allOrders: List<BakerOrder> = listOf()
    private var currentFilter: String? = null

    // Filter buttons
    private lateinit var btnAllOrders: Button
    private lateinit var btnPending: Button
    private lateinit var btnInProgress: Button
    private lateinit var btnReady: Button
    private lateinit var btnDelivered: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_baker_orders)

        bakerId = intent.getIntExtra("baker_id", 0)
        if (bakerId == 0) {
            Toast.makeText(this, "Invalid baker", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize views
        rvOrders = findViewById(R.id.rvOrders)
        txtEmpty = findViewById(R.id.txtEmpty)
        btnAllOrders = findViewById(R.id.btnAllOrders)
        btnPending = findViewById(R.id.btnPending)
        btnInProgress = findViewById(R.id.btnInProgress)
        btnReady = findViewById(R.id.btnReady)
        btnDelivered = findViewById(R.id.btnDelivered)

        // Back button
        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Setup RecyclerView
        orderAdapter = BakerOrderAdapter(mutableListOf()) { order ->
            handleOrderAction(order)
        }
        rvOrders.layoutManager = LinearLayoutManager(this)
        rvOrders.adapter = orderAdapter

        // Setup filter buttons
        setupFilterButtons()

        // Load orders
        loadOrders()
    }

    private fun setupFilterButtons() {
        val buttons = listOf(btnAllOrders, btnPending, btnInProgress, btnReady, btnDelivered)

        fun selectButton(selected: Button) {
            buttons.forEach { btn ->
                if (btn == selected) {
                    btn.setBackgroundColor(0xFF3B82F6.toInt())
                    btn.setTextColor(0xFFFFFFFF.toInt())
                } else {
                    btn.setBackgroundColor(0xFFE5E7EB.toInt())
                    btn.setTextColor(0xFF374151.toInt())
                }
            }
        }

        btnAllOrders.setOnClickListener {
            selectButton(btnAllOrders)
            currentFilter = null
            applyFilter()
        }

        btnPending.setOnClickListener {
            selectButton(btnPending)
            currentFilter = "pending"
            applyFilter()
        }

        btnInProgress.setOnClickListener {
            selectButton(btnInProgress)
            currentFilter = "in_progress"
            applyFilter()
        }

        btnReady.setOnClickListener {
            selectButton(btnReady)
            currentFilter = "ready"
            applyFilter()
        }

        btnDelivered.setOnClickListener {
            selectButton(btnDelivered)
            currentFilter = "delivered"
            applyFilter()
        }
    }

    private fun applyFilter() {
        val filtered = if (currentFilter == null) {
            allOrders
        } else {
            allOrders.filter { it.status.lowercase().replace(" ", "_") == currentFilter }
        }

        orderAdapter.updateData(filtered)

        if (filtered.isEmpty()) {
            txtEmpty.visibility = View.VISIBLE
            rvOrders.visibility = View.GONE
        } else {
            txtEmpty.visibility = View.GONE
            rvOrders.visibility = View.VISIBLE
        }
    }

    private fun loadOrders() {
        ApiClient.api.getBakerOrders(bakerId)
            .enqueue(object : Callback<BakerOrdersResponse> {
                override fun onResponse(
                    call: Call<BakerOrdersResponse>,
                    response: Response<BakerOrdersResponse>
                ) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        allOrders = response.body()!!.orders
                        applyFilter()
                    } else {
                        Toast.makeText(
                            this@BakerOrdersActivity,
                            "Failed to load orders",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<BakerOrdersResponse>, t: Throwable) {
                    Toast.makeText(
                        this@BakerOrdersActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun handleOrderAction(order: BakerOrder) {
        val currentStatus = order.status.lowercase().replace(" ", "_")
        
        // If order is ready, call setForDelivery to make it available for delivery partners
        if (currentStatus == "ready") {
            setOrderForDelivery(order)
            return
        }
        
        val newStatus = when (currentStatus) {
            "pending" -> "in_progress"
            "in_progress" -> "ready"
            else -> return
        }

        ApiClient.api.updateOrderStatus(order.order_id, newStatus)
            .enqueue(object : Callback<BasicResponse> {
                override fun onResponse(
                    call: Call<BasicResponse>,
                    response: Response<BasicResponse>
                ) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        Toast.makeText(
                            this@BakerOrdersActivity,
                            "Order status updated",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadOrders() // Refresh the list
                    } else {
                        Toast.makeText(
                            this@BakerOrdersActivity,
                            "Failed to update status",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                    Toast.makeText(
                        this@BakerOrdersActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
    
    private fun setOrderForDelivery(order: BakerOrder) {
        val body = mapOf(
            "order_id" to order.order_id,
            "baker_id" to bakerId
        )
        
        ApiClient.api.setForDelivery(body)
            .enqueue(object : Callback<BasicResponse> {
                override fun onResponse(
                    call: Call<BasicResponse>,
                    response: Response<BasicResponse>
                ) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        Toast.makeText(
                            this@BakerOrdersActivity,
                            "✓ Order is now available for delivery partners!",
                            Toast.LENGTH_LONG
                        ).show()
                        loadOrders() // Refresh the list
                    } else {
                        Toast.makeText(
                            this@BakerOrdersActivity,
                            response.body()?.message ?: "Failed to set for delivery",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                    Toast.makeText(
                        this@BakerOrdersActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
}
