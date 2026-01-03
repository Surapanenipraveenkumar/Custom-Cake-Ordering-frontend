package com.simats.cakeordering

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.simats.cakeordering.databinding.ActivityOrderConfirmationBinding

class OrderConfirmationActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "OrderConfirmation"
        private const val BASE_URL = "https://zgt68nw9-80.inc1.devtunnels.ms/Custom-Cake-Ordering/"
    }

    private lateinit var binding: ActivityOrderConfirmationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderConfirmationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get data from intent
        val orderId = intent.getStringExtra("order_id") ?: ""
        val subtotal = intent.getIntExtra("subtotal", 0)
        val deliveryFee = intent.getIntExtra("delivery_fee", 0)
        val totalAmount = intent.getIntExtra("total_amount", 0)
        val itemName = intent.getStringExtra("item_name") ?: "Cake Order"
        val itemImage = intent.getStringExtra("item_image") ?: ""
        val deliveryAddress = intent.getStringExtra("delivery_address") ?: "Pickup"
        val deliveryDate = intent.getStringExtra("delivery_date") ?: ""
        val deliveryTime = intent.getStringExtra("delivery_time") ?: ""

        // Calculate tax
        val tax = ((subtotal + deliveryFee) * 0.001).toInt() // 0.1% tax
        val total = if (totalAmount > 0) totalAmount else (subtotal + deliveryFee + tax)

        // Display data
        displayOrderDetails(orderId, itemName, itemImage, subtotal, deliveryFee, tax, total, 
            deliveryAddress, deliveryDate, deliveryTime)

        setupClickListeners()
    }

    private fun displayOrderDetails(
        orderId: String,
        itemName: String,
        itemImage: String,
        subtotal: Int,
        deliveryFee: Int,
        tax: Int,
        total: Int,
        address: String,
        date: String,
        time: String
    ) {
        binding.tvOrderId.text = orderId
        binding.tvItemName.text = itemName
        binding.tvItemPrice.text = "₹$subtotal"
        binding.tvSubtotal.text = "₹$subtotal"
        binding.tvDeliveryFee.text = "₹$deliveryFee"
        binding.tvTax.text = "₹$tax"
        binding.tvTotal.text = "₹$total"
        binding.tvDeliveryAddress.text = address
        binding.tvDeliveryDate.text = date
        binding.tvDeliveryTime.text = time

        // Load cake image
        loadCakeImage(itemImage)
    }

    private fun loadCakeImage(imageUrl: String) {
        Log.d(TAG, "Loading cake image: $imageUrl")
        
        if (imageUrl.isNotEmpty()) {
            // Image path from database is like: uploads/cakes/cake_123.jpg
            // Build the full URL using the correct base URL
            val fullUrl = when {
                imageUrl.startsWith("http") -> imageUrl
                imageUrl.startsWith("/") -> BASE_URL + imageUrl.substring(1)
                else -> BASE_URL + imageUrl
            }
            
            Log.d(TAG, "Full image URL: $fullUrl")
            
            Glide.with(this)
                .load(fullUrl)
                .placeholder(R.drawable.ic_placeholder_image)
                .error(R.drawable.ic_placeholder_image)
                .centerCrop()
                .into(binding.ivCakeImage)
        } else {
            // No image URL, show placeholder
            binding.ivCakeImage.setImageResource(R.drawable.ic_placeholder_image)
        }
    }

    private fun setupClickListeners() {
        // Go to Home button
        binding.btnGoHome.setOnClickListener {
            navigateToHome()
        }

        // Track Order button
        binding.btnTrackOrder.setOnClickListener {
            // TODO: Navigate to Order Tracking screen
            navigateToHome()
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        navigateToHome()
    }
}
