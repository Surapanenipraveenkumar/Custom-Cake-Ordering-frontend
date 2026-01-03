package com.simats.cakeordering

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.cakeordering.api.ApiClient
import com.simats.cakeordering.model.GenericResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OrderTrackingActivity : AppCompatActivity() {

    private var orderId: Int = 0
    private var bakerPhone: String? = null
    private var currentStatus: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_tracking)

        // Get data from Intent
        orderId = intent.getIntExtra("order_id", 0)
        val orderIdStr = intent.getStringExtra("order_id_str") ?: "ORD${String.format("%08d", orderId)}"
        currentStatus = intent.getStringExtra("status") ?: "pending"
        val totalAmount = intent.getDoubleExtra("total_amount", 0.0)
        val orderDate = intent.getStringExtra("order_date") ?: ""
        val orderTime = intent.getStringExtra("order_time") ?: ""
        val deliveryAddress = intent.getStringExtra("delivery_address") ?: "N/A"
        val bakerName = intent.getStringExtra("baker_name") ?: ""

        if (orderId == 0) {
            Toast.makeText(this, "Invalid order", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupViews()
        displayOrderData(orderIdStr, currentStatus, totalAmount, orderDate, orderTime, deliveryAddress)
    }

    private fun setupViews() {
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnContactBaker).setOnClickListener {
            if (!bakerPhone.isNullOrEmpty()) {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$bakerPhone"))
                startActivity(intent)
            } else {
                Toast.makeText(this, "Baker phone not available", Toast.LENGTH_SHORT).show()
            }
        }

        // Cancel Order button
        val btnCancelOrder = findViewById<Button>(R.id.btnCancelOrder)
        btnCancelOrder.setOnClickListener {
            showCancelConfirmationDialog()
        }
    }

    private fun showCancelConfirmationDialog() {
        // Check if order can be cancelled
        val status = currentStatus.lowercase()
        if (status == "delivered") {
            Toast.makeText(this, "Cannot cancel a delivered order", Toast.LENGTH_SHORT).show()
            return
        }
        if (status == "cancelled") {
            Toast.makeText(this, "Order is already cancelled", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Cancel Order")
            .setMessage("Are you sure you want to cancel this order? This action cannot be undone.")
            .setPositiveButton("Yes, Cancel") { _, _ ->
                cancelOrder()
            }
            .setNegativeButton("No, Keep Order", null)
            .show()
    }

    private fun cancelOrder() {
        Toast.makeText(this, "Cancelling order...", Toast.LENGTH_SHORT).show()

        ApiClient.api.cancelOrder(orderId)
            .enqueue(object : Callback<GenericResponse> {
                override fun onResponse(
                    call: Call<GenericResponse>,
                    response: Response<GenericResponse>
                ) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        Toast.makeText(
                            this@OrderTrackingActivity,
                            "Order cancelled successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        
                        // Update UI to show cancelled status
                        currentStatus = "cancelled"
                        updateTrackingStatus("cancelled", "", "")
                        
                        // Hide cancel button
                        findViewById<Button>(R.id.btnCancelOrder).visibility = View.GONE
                        
                        // Go back after a short delay
                        findViewById<Button>(R.id.btnCancelOrder).postDelayed({
                            finish()
                        }, 1500)
                    } else {
                        val errorMsg = response.body()?.message ?: "Failed to cancel order"
                        Toast.makeText(
                            this@OrderTrackingActivity,
                            errorMsg,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                    Toast.makeText(
                        this@OrderTrackingActivity,
                        "Connection error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun displayOrderData(
        orderIdStr: String,
        status: String,
        totalAmount: Double,
        orderDate: String,
        orderTime: String,
        deliveryAddress: String
    ) {
        // Display Order ID prominently
        findViewById<TextView>(R.id.tvOrderId).text = orderIdStr

        // Display delivery info
        findViewById<TextView>(R.id.tvDeliveryAddress).text = 
            if (deliveryAddress.isNotEmpty() && deliveryAddress != "N/A") deliveryAddress else "Not specified"
        
        // Parse date for delivery info
        findViewById<TextView>(R.id.tvDeliveryDate).text = if (orderDate.isNotEmpty()) orderDate else "N/A"
        findViewById<TextView>(R.id.tvDeliveryTime).text = if (orderTime.isNotEmpty()) orderTime else "N/A"
        
        // Total amount
        findViewById<TextView>(R.id.tvTotalAmount).text = "₹${totalAmount.toInt()}"

        // Update tracking status
        updateTrackingStatus(status, orderDate, orderTime)
        
        // Hide cancel button if already delivered or cancelled
        val statusLower = status.lowercase()
        if (statusLower == "delivered" || statusLower == "cancelled") {
            findViewById<Button>(R.id.btnCancelOrder).visibility = View.GONE
        }
    }

    private fun updateTrackingStatus(status: String, orderDate: String, orderTime: String) {
        val tvCurrentStatus = findViewById<TextView>(R.id.tvCurrentStatus)

        // Step indicators
        val dot1 = findViewById<View>(R.id.dot1)
        val dot2 = findViewById<View>(R.id.dot2)
        val dot3 = findViewById<View>(R.id.dot3)
        val dot4 = findViewById<View>(R.id.dot4)
        val dot5 = findViewById<View>(R.id.dot5)

        val line1 = findViewById<View>(R.id.line1)
        val line2 = findViewById<View>(R.id.line2)
        val line3 = findViewById<View>(R.id.line3)
        val line4 = findViewById<View>(R.id.line4)

        val tvStep1Title = findViewById<TextView>(R.id.tvStep1Title)
        val tvStep2Title = findViewById<TextView>(R.id.tvStep2Title)
        val tvStep3Title = findViewById<TextView>(R.id.tvStep3Title)
        val tvStep4Title = findViewById<TextView>(R.id.tvStep4Title)
        val tvStep5Title = findViewById<TextView>(R.id.tvStep5Title)

        val tvStep1Time = findViewById<TextView>(R.id.tvStep1Time)

        // Reset all to pending
        listOf(dot1, dot2, dot3, dot4, dot5).forEach {
            it.setBackgroundResource(R.drawable.bg_step_pending)
        }
        listOf(line1, line2, line3, line4).forEach {
            it.setBackgroundColor(0xFFE5E7EB.toInt())
        }
        listOf(tvStep1Title, tvStep2Title, tvStep3Title, tvStep4Title, tvStep5Title).forEach {
            it.setTextColor(0xFF6B7280.toInt())
        }

        // Show order date/time for step 1
        if (orderDate.isNotEmpty() || orderTime.isNotEmpty()) {
            tvStep1Time.text = "$orderDate, $orderTime"
        }

        // Determine current step based on status
        val currentStep = when (status.lowercase()) {
            "pending" -> 1
            "confirmed" -> 2
            "in_progress", "in progress", "preparing" -> 3
            "out_for_delivery", "out for delivery", "picked_up" -> 4
            "ready", "delivered" -> 5
            "cancelled" -> 0
            else -> 1
        }

        // Update status text
        when (currentStep) {
            0 -> {
                tvCurrentStatus.text = "Cancelled"
                tvCurrentStatus.setTextColor(0xFFEF4444.toInt())
            }
            1 -> {
                tvCurrentStatus.text = "Order Placed"
                tvCurrentStatus.setTextColor(0xFFF59E0B.toInt())
            }
            2 -> {
                tvCurrentStatus.text = "Order Confirmed"
                tvCurrentStatus.setTextColor(0xFF3B82F6.toInt())
            }
            3 -> {
                tvCurrentStatus.text = "Preparing Your Cake"
                tvCurrentStatus.setTextColor(0xFF8B5CF6.toInt())
            }
            4 -> {
                tvCurrentStatus.text = "Out for Delivery"
                tvCurrentStatus.setTextColor(0xFF3B82F6.toInt())
            }
            5 -> {
                tvCurrentStatus.text = "Delivered"
                tvCurrentStatus.setTextColor(0xFF10B981.toInt())
            }
        }

        // Mark completed steps
        if (currentStep >= 1) {
            dot1.setBackgroundResource(R.drawable.bg_step_completed)
            tvStep1Title.setTextColor(0xFF111827.toInt())
            tvStep1Title.text = "Order Placed ✓"
            if (currentStep > 1) line1.setBackgroundColor(0xFF10B981.toInt())
        }

        if (currentStep >= 2) {
            dot2.setBackgroundResource(R.drawable.bg_step_completed)
            tvStep2Title.setTextColor(0xFF111827.toInt())
            tvStep2Title.text = "Order Confirmed ✓"
            if (currentStep > 2) line2.setBackgroundColor(0xFF10B981.toInt())
        }

        if (currentStep >= 3) {
            dot3.setBackgroundResource(R.drawable.bg_step_completed)
            tvStep3Title.setTextColor(0xFF111827.toInt())
            tvStep3Title.text = "Preparing Your Cake ✓"
            if (currentStep > 3) line3.setBackgroundColor(0xFF10B981.toInt())
        }

        if (currentStep >= 4) {
            dot4.setBackgroundResource(R.drawable.bg_step_completed)
            tvStep4Title.setTextColor(0xFF111827.toInt())
            tvStep4Title.text = "Out for Delivery ✓"
            if (currentStep > 4) line4.setBackgroundColor(0xFF10B981.toInt())
        }

        if (currentStep >= 5) {
            dot5.setBackgroundResource(R.drawable.bg_step_completed)
            tvStep5Title.setTextColor(0xFF111827.toInt())
            tvStep5Title.text = "Delivered ✓"
        }

        // Mark current step as active (blue)
        if (currentStep > 0 && currentStep < 5) {
            val currentDot = when (currentStep) {
                1 -> dot1
                2 -> dot2
                3 -> dot3
                4 -> dot4
                else -> null
            }
            currentDot?.setBackgroundResource(R.drawable.bg_step_active)
        }
    }
}
