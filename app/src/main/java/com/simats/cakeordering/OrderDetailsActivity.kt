package com.simats.cakeordering

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.simats.cakeordering.api.ApiClient
import com.simats.cakeordering.model.OrderDetails
import com.simats.cakeordering.model.OrderDetailsResponse
import com.simats.cakeordering.model.OrderItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OrderDetailsActivity : AppCompatActivity() {

    private var orderId: Int = 0
    private var bakerPhone: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_details)

        orderId = intent.getIntExtra("order_id", 0)

        if (orderId == 0) {
            Toast.makeText(this, "Invalid order", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupViews()
        loadOrderDetails()
    }

    private fun setupViews() {
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<ImageView>(R.id.btnCallBaker).setOnClickListener {
            if (!bakerPhone.isNullOrEmpty()) {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$bakerPhone"))
                startActivity(intent)
            } else {
                Toast.makeText(this, "Baker phone not available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadOrderDetails() {
        ApiClient.api.getOrderDetails(orderId)
            .enqueue(object : Callback<OrderDetailsResponse> {
                override fun onResponse(
                    call: Call<OrderDetailsResponse>,
                    response: Response<OrderDetailsResponse>
                ) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        response.body()?.order?.let { displayOrderDetails(it) }
                    } else {
                        Toast.makeText(
                            this@OrderDetailsActivity,
                            "Failed to load order details",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<OrderDetailsResponse>, t: Throwable) {
                    Toast.makeText(
                        this@OrderDetailsActivity,
                        "Connection error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun displayOrderDetails(order: OrderDetails) {
        bakerPhone = order.bakerPhone

        android.util.Log.d("OrderDetails", "Order: ${order.orderIdStr}")
        android.util.Log.d("OrderDetails", "Items count: ${order.items.size}")
        for (item in order.items) {
            android.util.Log.d("OrderDetails", "Item: ${item.cakeName}, Price: ${item.price}")
        }

        // Header
        findViewById<TextView>(R.id.tvOrderId).text = order.orderIdStr
        findViewById<TextView>(R.id.tvOrderDateTime).text = "Ordered on ${order.orderDate}"
        
        // Status
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        when (order.status.lowercase()) {
            "pending" -> {
                tvStatus.text = "Pending"
                tvStatus.setBackgroundResource(R.drawable.bg_status_pending)
            }
            "in_progress", "in progress" -> {
                tvStatus.text = "In Progress"
                tvStatus.setBackgroundResource(R.drawable.bg_status_progress)
            }
            "ready", "delivered" -> {
                tvStatus.text = "Delivered"
                tvStatus.setBackgroundResource(R.drawable.bg_status_delivered)
            }
            "cancelled" -> {
                tvStatus.text = "Cancelled"
                tvStatus.setBackgroundResource(R.drawable.bg_status_cancelled)
            }
        }

        // Baker info
        findViewById<TextView>(R.id.tvBakerName).text = order.bakerName

        // Order items
        val itemsContainer = findViewById<LinearLayout>(R.id.orderItemsContainer)
        itemsContainer.removeAllViews()
        
        if (order.items.isEmpty()) {
            // Show "No items" message
            val noItemsView = TextView(this).apply {
                text = "Order details not available"
                textSize = 14f
                setTextColor(0xFF9CA3AF.toInt())
                setPadding(16, 16, 16, 16)
            }
            itemsContainer.addView(noItemsView)
        } else {
            for (item in order.items) {
                addOrderItemView(itemsContainer, item)
            }
        }

        // Delivery info
        findViewById<TextView>(R.id.tvDeliveryAddress).text = order.deliveryAddress
        findViewById<TextView>(R.id.tvDeliveryDate).text = order.deliveryDate ?: "N/A"
        findViewById<TextView>(R.id.tvDeliveryTime).text = order.deliveryTime ?: "N/A"

        // Payment
        findViewById<TextView>(R.id.tvPaymentMethod).text = order.paymentMethod
        findViewById<TextView>(R.id.tvSubtotal).text = "₹${order.subtotal.toInt()}"
        findViewById<TextView>(R.id.tvDeliveryFee).text = "₹${order.deliveryFee.toInt()}"
        findViewById<TextView>(R.id.tvTotalAmount).text = "₹${order.totalAmount.toInt()}"
    }

    private fun addOrderItemView(container: LinearLayout, item: OrderItem) {
        val itemView = LayoutInflater.from(this)
            .inflate(R.layout.item_order_detail, container, false)

        itemView.findViewById<TextView>(R.id.tvCakeName).text = item.cakeName
        itemView.findViewById<TextView>(R.id.tvQuantity).text = "x${item.quantity}"
        itemView.findViewById<TextView>(R.id.tvPrice).text = "₹${item.price.toInt()}"

        val imgCake = itemView.findViewById<ImageView>(R.id.imgCake)
        if (!item.cakeImage.isNullOrEmpty()) {
        val imageUrl = if (item.cakeImage.startsWith("http")) {
                item.cakeImage
            } else {
                "https://zgt68nw9-80.inc1.devtunnels.ms/Custom-Cake-Ordering/${item.cakeImage}"
            }
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.sample_cake)
                .error(R.drawable.sample_cake)
                .into(imgCake)
        }

        if (!item.customization.isNullOrEmpty()) {
            itemView.findViewById<TextView>(R.id.tvCustomization).apply {
                visibility = View.VISIBLE
                text = item.customization
            }
        }

        container.addView(itemView)
    }
}
