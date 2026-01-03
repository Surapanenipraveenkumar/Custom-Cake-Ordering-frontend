package com.simats.cakeordering

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.cakeordering.api.ApiClient
import com.simats.cakeordering.databinding.ActivityDeliveryOrderDetailBinding
import com.simats.cakeordering.model.BasicResponse
import com.simats.cakeordering.model.OrderDetailsResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DeliveryOrderDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeliveryOrderDetailBinding
    private var orderId: Int = -1
    private var deliveryId: Int = -1
    private var customerPhone: String = ""
    private var bakerPhone: String = ""
    private var bakerAddress: String = ""
    private var customerAddress: String = ""
    private var currentStatus: String = "pending"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDeliveryOrderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        orderId = intent.getIntExtra("order_id", -1)
        val prefs = getSharedPreferences("DeliveryPrefs", MODE_PRIVATE)
        deliveryId = prefs.getInt("delivery_id", -1)

        if (orderId == -1 || deliveryId == -1) {
            Toast.makeText(this, "Invalid order", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupClickListeners()
        loadOrderDetails()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnCallCustomer.setOnClickListener {
            if (customerPhone.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$customerPhone"))
                startActivity(intent)
            }
        }

        binding.btnCallBaker.setOnClickListener {
            if (bakerPhone.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$bakerPhone"))
                startActivity(intent)
            }
        }

        binding.btnNavigateBaker.setOnClickListener {
            if (bakerAddress.isNotEmpty()) {
                val uri = Uri.parse("google.navigation:q=${Uri.encode(bakerAddress)}")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.setPackage("com.google.android.apps.maps")
                startActivity(intent)
            }
        }

        binding.btnNavigateCustomer.setOnClickListener {
            if (customerAddress.isNotEmpty()) {
                val uri = Uri.parse("google.navigation:q=${Uri.encode(customerAddress)}")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.setPackage("com.google.android.apps.maps")
                startActivity(intent)
            }
        }

        binding.btnPickup.setOnClickListener {
            updateStatus("pickup")
        }

        binding.btnDeliver.setOnClickListener {
            updateStatus("deliver")
        }
    }

    private fun loadOrderDetails() {
        binding.progressBar.visibility = View.VISIBLE

        ApiClient.api.getOrderDetails(orderId)
            .enqueue(object : Callback<OrderDetailsResponse> {
                override fun onResponse(
                    call: Call<OrderDetailsResponse>,
                    response: Response<OrderDetailsResponse>
                ) {
                    binding.progressBar.visibility = View.GONE

                    val body = response.body()
                    if (response.isSuccessful && body?.status == "success") {
                        val order = body.order
                        if (order != null) {
                            updateUI(order)
                        } else {
                            Toast.makeText(
                                this@DeliveryOrderDetailActivity,
                                "Order data not found",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@DeliveryOrderDetailActivity,
                            "Failed to load order",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<OrderDetailsResponse>, t: Throwable) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@DeliveryOrderDetailActivity,
                        "Network error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun updateUI(order: com.simats.cakeordering.model.OrderDetails) {
        binding.tvOrderId.text = "Order #${order.orderId}"
        binding.tvCustomerName.text = order.customerName ?: "Customer"
        binding.tvBakerName.text = order.bakerName
        binding.tvBakerAddress.text = order.bakerAddress ?: "Baker Address"
        binding.tvCustomerAddress.text = order.deliveryAddress
        binding.tvAmount.text = "₹${order.totalAmount.toInt()}"

        customerPhone = order.customerPhone ?: ""
        bakerPhone = order.bakerPhone ?: ""
        bakerAddress = order.bakerAddress ?: ""
        customerAddress = order.deliveryAddress
        currentStatus = order.status

        // Update status badge
        binding.tvStatus.text = currentStatus.replaceFirstChar { it.uppercase() }

        // Show/hide action buttons based on status
        when (currentStatus.lowercase()) {
            "confirmed", "assigned", "ready", "ready_for_delivery", "pending" -> {
                binding.btnPickup.visibility = View.VISIBLE
                binding.btnDeliver.visibility = View.GONE
            }
            "picked_up", "out_for_delivery" -> {
                binding.btnPickup.visibility = View.GONE
                binding.btnDeliver.visibility = View.VISIBLE
            }
            "delivered" -> {
                binding.btnPickup.visibility = View.GONE
                binding.btnDeliver.visibility = View.GONE
            }
            else -> {
                // Show pickup button by default for ready orders
                binding.btnPickup.visibility = View.VISIBLE
                binding.btnDeliver.visibility = View.GONE
            }
        }
    }

    private fun updateStatus(action: String) {
        val body = mapOf(
            "delivery_id" to deliveryId,
            "order_id" to orderId,
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
                            this@DeliveryOrderDetailActivity,
                            response.body()?.message ?: "Success",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadOrderDetails()
                    } else {
                        Toast.makeText(
                            this@DeliveryOrderDetailActivity,
                            response.body()?.message ?: "Failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                    Toast.makeText(
                        this@DeliveryOrderDetailActivity,
                        "Network error",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
