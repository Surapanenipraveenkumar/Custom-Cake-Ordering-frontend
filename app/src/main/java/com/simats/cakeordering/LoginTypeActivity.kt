package com.simats.cakeordering

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simats.cakeordering.databinding.ActivityLoginTypeBinding

class LoginTypeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginTypeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginTypeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // CUSTOMER LOGIN
        binding.btnCustomer.setOnClickListener {
            startActivity(
                Intent(this, CustomerLoginActivity::class.java)
            )
        }

        // BAKER LOGIN
        binding.btnBaker.setOnClickListener {
            startActivity(
                Intent(this, BakerLoginActivity::class.java)
            )
        }

        // DELIVERY LOGIN
        binding.btnDelivery.setOnClickListener {
            startActivity(
                Intent(this, DeliveryLoginActivity::class.java)
            )
        }
    }
}
