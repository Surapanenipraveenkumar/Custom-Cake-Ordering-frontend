package com.simats.cakeordering

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.simats.cakeordering.api.ApiClient
import com.simats.cakeordering.model.LoginRequest
import com.simats.cakeordering.model.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CustomerLoginActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_login)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)

        btnLogin.setOnClickListener {

            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = LoginRequest(email, password)

            ApiClient.api.login(request)
                .enqueue(object : Callback<LoginResponse> {

                    override fun onResponse(
                        call: Call<LoginResponse>,
                        response: Response<LoginResponse>
                    ) {
                        if (response.isSuccessful &&
                            response.body()?.status == "success"
                        ) {
                            val body = response.body()!!

                            // ✅ Save user_id to SharedPreferences
                            val prefs = getSharedPreferences("CakeOrderingPrefs", Context.MODE_PRIVATE)
                            prefs.edit().apply {
                                putInt("user_id", body.userId ?: 0)
                                putString("user_name", body.name ?: "")
                                putString("user_type", "customer")
                                apply()
                            }

                            // 🔥 Save FCM token to server for push notifications
                            NotificationHelper.saveTokenToServerAfterLogin(
                                this@CustomerLoginActivity,
                                "customer",
                                body.userId ?: 0
                            )

                            Toast.makeText(
                                this@CustomerLoginActivity,
                                "Login successful",
                                Toast.LENGTH_SHORT
                            ).show()

                            startActivity(
                                Intent(
                                    this@CustomerLoginActivity,
                                    HomeActivity::class.java
                                )
                            )
                            finish()

                        } else {
                            Toast.makeText(
                                this@CustomerLoginActivity,
                                response.body()?.message ?: "Invalid login",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        Toast.makeText(
                            this@CustomerLoginActivity,
                            "Server error: ${t.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
        }

        tvRegister.setOnClickListener {
            startActivity(
                Intent(this, RegisterActivity::class.java)
            )
        }
    }
}
