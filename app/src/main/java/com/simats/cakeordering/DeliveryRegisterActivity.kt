package com.simats.cakeordering

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.cakeordering.api.ApiClient
import com.simats.cakeordering.databinding.ActivityDeliveryRegisterBinding
import com.simats.cakeordering.model.BasicResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DeliveryRegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeliveryRegisterBinding
    private var selectedVehicle = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDeliveryRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupVehicleSpinner()

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val vehicle = selectedVehicle
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty() || vehicle.isEmpty() || 
                email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerDelivery(name, phone, vehicle, email, password, confirmPassword)
        }

        binding.tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun setupVehicleSpinner() {
        val vehicles = listOf("Select Vehicle Type", "Bicycle", "Scooter", "Motorcycle", "Car")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, vehicles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerVehicle.adapter = adapter

        binding.spinnerVehicle.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedVehicle = if (position > 0) vehicles[position] else ""
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedVehicle = ""
            }
        }
    }

    private fun registerDelivery(
        name: String, 
        phone: String, 
        vehicle: String, 
        email: String, 
        password: String,
        confirmPassword: String
    ) {
        binding.btnRegister.isEnabled = false
        binding.btnRegister.text = "Creating Account..."

        val body = mapOf(
            "name" to name,
            "phone" to phone,
            "vehicle" to vehicle,
            "email" to email,
            "password" to password,
            "cpassword" to confirmPassword
        )

        ApiClient.api.deliveryRegister(body)
            .enqueue(object : Callback<BasicResponse> {
                override fun onResponse(
                    call: Call<BasicResponse>,
                    response: Response<BasicResponse>
                ) {
                    binding.btnRegister.isEnabled = true
                    binding.btnRegister.text = "Create Account"
                    val responseBody = response.body()

                    if (response.isSuccessful && responseBody?.status == "success") {
                        Toast.makeText(
                            this@DeliveryRegisterActivity,
                            "Registration successful! Please login.",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@DeliveryRegisterActivity,
                            responseBody?.message ?: "Registration failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                    binding.btnRegister.isEnabled = true
                    binding.btnRegister.text = "Create Account"
                    Toast.makeText(
                        this@DeliveryRegisterActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
