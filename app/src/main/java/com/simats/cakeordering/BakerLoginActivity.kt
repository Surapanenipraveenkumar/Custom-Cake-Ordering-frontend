package com.simats.cakeordering

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.cakeordering.api.ApiClient
import com.simats.cakeordering.databinding.ActivityBakerLoginBinding
import com.simats.cakeordering.model.LoginRequest
import com.simats.cakeordering.model.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BakerLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBakerLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBakerLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Enter email & password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginBaker(email, password)
        }

        // Navigate to registration
        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, BakerRegisterActivity::class.java))
        }
    }

    private fun loginBaker(email: String, password: String) {

        ApiClient.api.bakerLogin(LoginRequest(email, password))
            .enqueue(object : Callback<LoginResponse> {

                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {

                    val body = response.body()

                    if (response.isSuccessful &&
                        body?.status == "success" &&
                        body.bakerId != null
                    ) {

                        Toast.makeText(
                            this@BakerLoginActivity,
                            "Login Successful",
                            Toast.LENGTH_SHORT
                        ).show()

                        // ✅ Save baker_id to SharedPreferences
                        val prefs = getSharedPreferences("CakeOrderingPrefs", MODE_PRIVATE)
                        prefs.edit()
                            .putInt("baker_id", body.bakerId)
                            .putString("user_type", "baker")
                            .apply()

                        // 🔥 Save FCM token to server for push notifications
                        NotificationHelper.saveTokenToServerAfterLogin(
                            this@BakerLoginActivity,
                            "baker",
                            body.bakerId
                        )

                        // ✅ OPEN DASHBOARD & CLEAR BACK STACK
                        val intent = Intent(
                            this@BakerLoginActivity,
                            BakerDashboardActivity::class.java
                        )

                        intent.putExtra("baker_id", body.bakerId)

                        // 🔥 THIS IS THE KEY FIX
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK

                        startActivity(intent)

                    } else {
                        Toast.makeText(
                            this@BakerLoginActivity,
                            body?.message ?: "Login failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(
                        this@BakerLoginActivity,
                        t.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
}
