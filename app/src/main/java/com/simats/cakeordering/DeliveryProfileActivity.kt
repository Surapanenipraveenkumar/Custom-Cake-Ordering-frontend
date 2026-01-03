package com.simats.cakeordering

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.cakeordering.api.ApiClient
import com.simats.cakeordering.databinding.ActivityDeliveryProfileBinding
import com.simats.cakeordering.model.DeliveryProfileResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DeliveryProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeliveryProfileBinding
    private var deliveryId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDeliveryProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("DeliveryPrefs", MODE_PRIVATE)
        deliveryId = prefs.getInt("delivery_id", -1)

        if (deliveryId == -1) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.btnBack.setOnClickListener { finish() }

        binding.btnLogout.setOnClickListener {
            prefs.edit().clear().apply()
            finishAffinity()
        }

        binding.btnEditProfile.setOnClickListener {
            startActivity(Intent(this, DeliveryEditProfileActivity::class.java))
        }

        binding.btnContactSupport.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = android.net.Uri.parse("tel:+919876543210")
            startActivity(intent)
        }

        // Set app version
        try {
            val version = packageManager.getPackageInfo(packageName, 0).versionName
            binding.tvAppVersion.text = version
        } catch (e: Exception) {
            binding.tvAppVersion.text = "1.0.0"
        }

        loadProfile()
    }

    private fun loadProfile() {
        ApiClient.api.getDeliveryProfile(deliveryId)
            .enqueue(object : Callback<DeliveryProfileResponse> {
                override fun onResponse(
                    call: Call<DeliveryProfileResponse>,
                    response: Response<DeliveryProfileResponse>
                ) {
                    val body = response.body()
                    if (response.isSuccessful && body?.status == "success") {
                        updateUI(body)
                    } else {
                        Toast.makeText(
                            this@DeliveryProfileActivity,
                            body?.message ?: "Failed to load profile",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<DeliveryProfileResponse>, t: Throwable) {
                    Toast.makeText(
                        this@DeliveryProfileActivity,
                        "Network error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun updateUI(data: DeliveryProfileResponse) {
        binding.tvName.text = data.name ?: "Delivery Partner"
        binding.tvEmail.text = data.email ?: ""
        binding.tvPhone.text = data.phone ?: ""
        binding.tvVehicle.text = data.vehicle ?: "N/A"
        binding.tvRating.text = "${data.rating ?: 0.0} ⭐"

        binding.tvTotalDeliveries.text = "${data.totalDeliveries ?: 0}"
        binding.tvTotalEarnings.text = "₹${data.totalEarnings?.toInt() ?: 0}"
        binding.tvMonthDeliveries.text = "${data.monthDeliveries ?: 0}"
        binding.tvMonthEarnings.text = "₹${data.monthEarnings?.toInt() ?: 0}"
    }
}
