package com.simats.cakeordering

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.simats.cakeordering.api.ApiClient
import com.simats.cakeordering.model.RegisterRequest
import com.simats.cakeordering.model.RegisterResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etCPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        btnRegister.setOnClickListener {

            val password = etPassword.text.toString().trim()
            val confirmPassword = etCPassword.text.toString().trim()

            if (password != confirmPassword) {
                Toast.makeText(
                    this,
                    "Passwords do not match",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val request = RegisterRequest(
                etName.text.toString().trim(),
                etEmail.text.toString().trim(),
                password
            )

            ApiClient.api.register(request)
                .enqueue(object : Callback<RegisterResponse> {

                    override fun onResponse(
                        call: Call<RegisterResponse>,
                        response: Response<RegisterResponse>
                    ) {
                        if (response.isSuccessful && response.body() != null) {

                            val res = response.body()!!

                            Toast.makeText(
                                this@RegisterActivity,
                                res.message,
                                Toast.LENGTH_LONG
                            ).show()

                            if (res.status == "success") {
                                startActivity(
                                    Intent(
                                        this@RegisterActivity,
                                        CustomerLoginActivity::class.java
                                    )
                                )
                                finish()
                            }

                        } else {
                            Toast.makeText(
                                this@RegisterActivity,
                                "Registration failed",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Server error: ${t.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
        }
    }
}
