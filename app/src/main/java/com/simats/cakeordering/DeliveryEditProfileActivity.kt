package com.simats.cakeordering

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.cakeordering.api.ApiClient
import com.simats.cakeordering.databinding.ActivityDeliveryEditProfileBinding
import com.simats.cakeordering.model.DeliveryProfileResponse
import com.simats.cakeordering.model.BasicResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DeliveryEditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeliveryEditProfileBinding
    private var deliveryId: Int = -1
    private val vehicleTypes = listOf("Motorcycle", "Scooter", "Bicycle", "Car")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDeliveryEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("DeliveryPrefs", MODE_PRIVATE)
        deliveryId = prefs.getInt("delivery_id", -1)

        if (deliveryId == -1) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupVehicleSpinner()
        setupClickListeners()
        loadProfile()
    }

    private fun setupVehicleSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, vehicleTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerVehicle.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnSave.setOnClickListener {
            saveProfile()
        }
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
                        binding.etName.setText(body.name ?: "")
                        binding.etPhone.setText(body.phone ?: "")
                        binding.etServiceArea.setText(body.serviceArea ?: "")
                        binding.etVehicleNumber.setText(body.vehicleNumber ?: "")
                        
                        // Set vehicle spinner
                        val vehicleIndex = vehicleTypes.indexOf(body.vehicle ?: "Motorcycle")
                        if (vehicleIndex >= 0) {
                            binding.spinnerVehicle.setSelection(vehicleIndex)
                        }
                    }
                }

                override fun onFailure(call: Call<DeliveryProfileResponse>, t: Throwable) {
                    Toast.makeText(
                        this@DeliveryEditProfileActivity,
                        "Failed to load profile",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun saveProfile() {
        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val vehicle = binding.spinnerVehicle.selectedItem.toString()
        val vehicleNumber = binding.etVehicleNumber.text.toString().trim()
        val serviceArea = binding.etServiceArea.text.toString().trim()

        if (name.isEmpty()) {
            binding.etName.error = "Name is required"
            return
        }

        if (phone.isEmpty()) {
            binding.etPhone.error = "Phone is required"
            return
        }

        val body = mapOf(
            "delivery_id" to deliveryId,
            "name" to name,
            "phone" to phone,
            "vehicle" to vehicle,
            "vehicle_number" to vehicleNumber,
            "service_area" to serviceArea
        )

        binding.btnSave.isEnabled = false
        binding.btnSave.text = "Saving..."

        ApiClient.api.updateDeliveryProfile(body)
            .enqueue(object : Callback<BasicResponse> {
                override fun onResponse(
                    call: Call<BasicResponse>,
                    response: Response<BasicResponse>
                ) {
                    binding.btnSave.isEnabled = true
                    binding.btnSave.text = "💾 Save Changes"

                    if (response.isSuccessful && response.body()?.status == "success") {
                        Toast.makeText(
                            this@DeliveryEditProfileActivity,
                            "Profile updated successfully!",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@DeliveryEditProfileActivity,
                            response.body()?.message ?: "Failed to update",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                    binding.btnSave.isEnabled = true
                    binding.btnSave.text = "💾 Save Changes"
                    Toast.makeText(
                        this@DeliveryEditProfileActivity,
                        "Network error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
