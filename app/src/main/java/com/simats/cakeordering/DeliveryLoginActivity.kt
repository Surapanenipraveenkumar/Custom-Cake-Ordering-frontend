package com.simats.cakeordering

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.cakeordering.api.ApiClient
import com.simats.cakeordering.databinding.ActivityDeliveryLoginBinding
import com.simats.cakeordering.model.LoginRequest
import com.simats.cakeordering.model.DeliveryLoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DeliveryLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeliveryLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDeliveryLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Enter email & password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginDelivery(email, password)
        }

        // Navigate to registration
        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, DeliveryRegisterActivity::class.java))
        }
    }

    private fun loginDelivery(email: String, password: String) {
        binding.btnLogin.isEnabled = false

        ApiClient.api.deliveryLogin(LoginRequest(email, password))
            .enqueue(object : Callback<DeliveryLoginResponse> {

                override fun onResponse(
                    call: Call<DeliveryLoginResponse>,
                    response: Response<DeliveryLoginResponse>
                ) {
                    binding.btnLogin.isEnabled = true
                    val body = response.body()

                    if (response.isSuccessful &&
                        body?.status == "success" &&
                        body.deliveryId != null
                    ) {

                        // Save to SharedPreferences
                        val prefs = getSharedPreferences("CakeOrderingPrefs", MODE_PRIVATE)
                        prefs.edit()
                            .putInt("delivery_id", body.deliveryId)
                            .putString("delivery_name", body.name)
                            .putString("user_type", "delivery")
                            .apply()

                        // 🔥 Save FCM token to server for push notifications
                        NotificationHelper.saveTokenToServerAfterLogin(
                            this@DeliveryLoginActivity,
                            "delivery",
                            body.deliveryId
                        )

                        Toast.makeText(
                            this@DeliveryLoginActivity,
                            "Login Successful",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Open Delivery Dashboard
                        val intent = Intent(
                            this@DeliveryLoginActivity,
                            DeliveryDashboardActivity::class.java
                        )
                        intent.putExtra("delivery_id", body.deliveryId)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)

                    } else {
                        Toast.makeText(
                            this@DeliveryLoginActivity,
                            body?.message ?: "Login failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<DeliveryLoginResponse>, t: Throwable) {
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(
                        this@DeliveryLoginActivity,
                        t.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
}
