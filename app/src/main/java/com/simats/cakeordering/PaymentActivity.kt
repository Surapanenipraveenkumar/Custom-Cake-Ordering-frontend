package com.simats.cakeordering

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.simats.cakeordering.api.ApiClient
import com.simats.cakeordering.databinding.ActivityPaymentBinding
import com.simats.cakeordering.model.PlaceOrderResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PaymentActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "PaymentActivity"
    }

    private lateinit var binding: ActivityPaymentBinding
    private var selectedPaymentMethod = "" // "upi" or "cash"
    private var subtotal = 0
    private var deliveryFee = 0
    private var total = 0
    private var userId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get user ID
        val prefs = getSharedPreferences("CakeOrderingPrefs", Context.MODE_PRIVATE)
        userId = prefs.getInt("user_id", 0)

        // Get data from intent
        subtotal = intent.getIntExtra("subtotal", 0)
        deliveryFee = intent.getIntExtra("delivery_fee", 0)
        total = subtotal + deliveryFee

        displayPrices()
        setupClickListeners()
    }

    private fun displayPrices() {
        binding.tvSubtotal.text = "₹$subtotal"
        binding.tvDeliveryFee.text = "₹$deliveryFee"
        binding.tvTotal.text = "₹$total"
    }

    private fun setupClickListeners() {
        // Back button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // UPI option
        binding.optionUPI.setOnClickListener {
            selectPaymentMethod("upi")
        }

        // Cash option
        binding.optionCash.setOnClickListener {
            selectPaymentMethod("cash")
        }

        // Place Order button
        binding.btnPlaceOrder.setOnClickListener {
            placeOrder()
        }
    }

    private fun selectPaymentMethod(method: String) {
        selectedPaymentMethod = method

        if (method == "upi") {
            // Select UPI
            binding.optionUPI.setBackgroundResource(R.drawable.bg_payment_option_selected)
            binding.ivUpiCheck.setImageResource(R.drawable.ic_radio_checked)

            // Deselect Cash
            binding.optionCash.setBackgroundResource(R.drawable.bg_payment_option)
            binding.ivCashCheck.setImageResource(R.drawable.ic_radio_unchecked)
        } else {
            // Select Cash
            binding.optionCash.setBackgroundResource(R.drawable.bg_payment_option_selected)
            binding.ivCashCheck.setImageResource(R.drawable.ic_radio_checked)

            // Deselect UPI
            binding.optionUPI.setBackgroundResource(R.drawable.bg_payment_option)
            binding.ivUpiCheck.setImageResource(R.drawable.ic_radio_unchecked)
        }
    }

    private fun placeOrder() {
        if (selectedPaymentMethod.isEmpty()) {
            Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedPaymentMethod == "upi") {
            // Show Razorpay dummy payment dialog
            showRazorpayDialog()
        } else {
            // Cash on Delivery - proceed directly
            processPaymentAndPlaceOrder("Cash on Delivery")
        }
    }

    private fun showRazorpayDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_razorpay_payment)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)

        // Set amount
        val tvAmount = dialog.findViewById<TextView>(R.id.tvPaymentAmount)
        tvAmount.text = "₹$total"

        // Close button
        val btnClose = dialog.findViewById<ImageView>(R.id.btnClosePayment)
        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        // Processing layout
        val processingLayout = dialog.findViewById<LinearLayout>(R.id.processingLayout)
        val tvProcessingStatus = dialog.findViewById<TextView>(R.id.tvProcessingStatus)
        val btnPay = dialog.findViewById<Button>(R.id.btnPay)

        // UPI Apps click listeners
        val btnGooglePay = dialog.findViewById<LinearLayout>(R.id.btnGooglePay)
        val btnPhonePe = dialog.findViewById<LinearLayout>(R.id.btnPhonePe)
        val btnPaytm = dialog.findViewById<LinearLayout>(R.id.btnPaytm)

        val startPayment: (String) -> Unit = { appName ->
            btnPay.visibility = View.GONE
            processingLayout.visibility = View.VISIBLE
            tvProcessingStatus.text = "Opening $appName..."

            Handler(Looper.getMainLooper()).postDelayed({
                tvProcessingStatus.text = "Waiting for payment..."
            }, 1000)

            Handler(Looper.getMainLooper()).postDelayed({
                tvProcessingStatus.text = "Verifying payment..."
            }, 2500)

            Handler(Looper.getMainLooper()).postDelayed({
                dialog.dismiss()
                processPaymentAndPlaceOrder("UPI ($appName)")
            }, 4000)
        }

        btnGooglePay.setOnClickListener { startPayment("Google Pay") }
        btnPhonePe.setOnClickListener { startPayment("PhonePe") }
        btnPaytm.setOnClickListener { startPayment("Paytm") }

        // Pay button click
        btnPay.setOnClickListener {
            val etUpiId = dialog.findViewById<EditText>(R.id.etUpiId)
            val upiId = etUpiId.text.toString().trim()

            if (upiId.isEmpty() || !upiId.contains("@")) {
                Toast.makeText(this, "Please enter a valid UPI ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnPay.visibility = View.GONE
            processingLayout.visibility = View.VISIBLE
            tvProcessingStatus.text = "Sending payment request..."

            // Simulate payment processing
            Handler(Looper.getMainLooper()).postDelayed({
                tvProcessingStatus.text = "Waiting for approval..."
            }, 1500)

            Handler(Looper.getMainLooper()).postDelayed({
                tvProcessingStatus.text = "Payment confirmed!"
            }, 3000)

            Handler(Looper.getMainLooper()).postDelayed({
                dialog.dismiss()
                processPaymentAndPlaceOrder("UPI")
            }, 4000)
        }

        dialog.show()
    }

    private fun processPaymentAndPlaceOrder(paymentType: String) {
        if (userId == 0) {
            Toast.makeText(this, "Please login to place order", Toast.LENGTH_SHORT).show()
            return
        }

        val deliveryAddress = intent.getStringExtra("delivery_address") ?: "Pickup"
        val deliveryDate = intent.getStringExtra("delivery_date") ?: ""
        val deliveryTime = intent.getStringExtra("delivery_time") ?: ""

        Log.d(TAG, "Placing order for user: $userId")

        val body = mapOf(
            "user_id" to userId,
            "delivery_address" to deliveryAddress,
            "delivery_date" to deliveryDate,
            "delivery_time" to deliveryTime,
            "payment_method" to paymentType,
            "delivery_fee" to deliveryFee
        )

        // Show loading
        Toast.makeText(this, "Placing order...", Toast.LENGTH_SHORT).show()

        ApiClient.api.placeOrder(body)
            .enqueue(object : Callback<PlaceOrderResponse> {
                override fun onResponse(
                    call: Call<PlaceOrderResponse>,
                    response: Response<PlaceOrderResponse>
                ) {
                    Log.d(TAG, "Place order response code: ${response.code()}")
                    Log.d(TAG, "Place order response body: ${response.body()}")
                    
                    if (response.isSuccessful && response.body()?.status == "success") {
                        val orderResponse = response.body()!!
                        Log.d(TAG, "Order placed successfully: ${orderResponse.orderIdStr}")

                        // Navigate to Order Confirmation with all data
                        navigateToConfirmation(orderResponse, paymentType, deliveryAddress, deliveryDate, deliveryTime)
                    } else {
                        val errorMsg = response.body()?.message ?: "Failed to place order (code: ${response.code()})"
                        Log.e(TAG, "Order placement failed: $errorMsg")
                        Toast.makeText(
                            this@PaymentActivity,
                            errorMsg,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<PlaceOrderResponse>, t: Throwable) {
                    Log.e(TAG, "Error placing order: ${t.message}", t)
                    Toast.makeText(this@PaymentActivity, "Connection error", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun navigateToConfirmation(
        orderResponse: PlaceOrderResponse,
        paymentType: String,
        address: String,
        date: String,
        time: String
    ) {
        val firstItem = orderResponse.items?.firstOrNull()
        
        // Debug log the image URL
        Log.d(TAG, "Order items: ${orderResponse.items}")
        Log.d(TAG, "First item: $firstItem")
        Log.d(TAG, "First item image: ${firstItem?.image}")
        
        val intent = Intent(this, OrderConfirmationActivity::class.java).apply {
            putExtra("order_id", orderResponse.orderIdStr ?: "")
            putExtra("subtotal", orderResponse.subtotal?.toInt() ?: subtotal)
            putExtra("delivery_fee", orderResponse.deliveryFee ?: deliveryFee)
            putExtra("total_amount", orderResponse.totalAmount?.toInt() ?: total)
            putExtra("item_name", firstItem?.cakeName ?: "Cake Order")
            putExtra("item_image", firstItem?.image ?: "")
            putExtra("delivery_address", address)
            putExtra("delivery_date", date)
            putExtra("delivery_time", time)
            putExtra("payment_method", paymentType)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
    }
}
