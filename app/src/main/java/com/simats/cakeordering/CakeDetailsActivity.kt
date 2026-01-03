package com.simats.cakeordering

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.simats.cakeordering.api.ApiClient
import com.simats.cakeordering.databinding.ActivityCakeDetailsBinding
import com.simats.cakeordering.model.CakeDetailsResponse
import com.simats.cakeordering.model.FavoriteResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CakeDetailsActivity : AppCompatActivity() {

    companion object {
        private const val BASE_URL = "https://zgt68nw9-80.inc1.devtunnels.ms/Custom-Cake-Ordering/"
        private const val TAG = "CakeDetailsActivity"
    }

    private lateinit var binding: ActivityCakeDetailsBinding
    private var cakeId: Int = 0
    private var bakerId: Int = 0
    private var bakerName: String = ""
    private var isFavorite: Boolean = false
    private var userId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCakeDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get user_id from SharedPreferences
        userId = getSharedPreferences("CakeOrderingPrefs", MODE_PRIVATE)
            .getInt("user_id", 0)

        // Get cake_id and baker_id from intent
        cakeId = intent.getIntExtra("cake_id", 0)
        bakerId = intent.getIntExtra("baker_id", 0)
        bakerName = intent.getStringExtra("baker_name") ?: ""

        if (cakeId == 0) {
            Toast.makeText(this, "Invalid cake", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupClickListeners()
        loadCakeDetails()
        checkFavoriteStatus()
    }

    private fun setupClickListeners() {
        // Back button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Favorite button - connected to database
        binding.btnFavorite.setOnClickListener {
            if (userId == 0) {
                Toast.makeText(this, "Please login to add favorites", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            toggleFavorite()
        }

        // Customize button
        binding.btnCustomize.setOnClickListener {
            val intent = Intent(this, CakeCustomizationActivity::class.java)
            intent.putExtra("cake_id", cakeId)
            startActivity(intent)
        }
    }

    private fun checkFavoriteStatus() {
        if (userId == 0) return
        
        ApiClient.api.toggleFavorite(userId, cakeId, "check")
            .enqueue(object : Callback<FavoriteResponse> {
                override fun onResponse(call: Call<FavoriteResponse>, response: Response<FavoriteResponse>) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        isFavorite = response.body()?.isFavorite ?: false
                        updateFavoriteIcon()
                    }
                }
                override fun onFailure(call: Call<FavoriteResponse>, t: Throwable) {
                    Log.e(TAG, "Error checking favorite status", t)
                }
            })
    }

    private fun toggleFavorite() {
        ApiClient.api.toggleFavorite(userId, cakeId, "toggle")
            .enqueue(object : Callback<FavoriteResponse> {
                override fun onResponse(call: Call<FavoriteResponse>, response: Response<FavoriteResponse>) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        isFavorite = response.body()?.isFavorite ?: false
                        updateFavoriteIcon()
                        val msg = response.body()?.message ?: (if (isFavorite) "Added to favorites" else "Removed from favorites")
                        Toast.makeText(this@CakeDetailsActivity, msg, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@CakeDetailsActivity, "Failed to update favorite", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<FavoriteResponse>, t: Throwable) {
                    Toast.makeText(this@CakeDetailsActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun updateFavoriteIcon() {
        val color = if (isFavorite) 0xFFEC4899.toInt() else 0xFFE5E7EB.toInt()
        binding.ivFavorite.setColorFilter(color)
    }


    private fun loadCakeDetails() {
        Log.d(TAG, "Loading cake details for cakeId: $cakeId")
        
        ApiClient.api.getCakeDetails(cakeId)
            .enqueue(object : Callback<CakeDetailsResponse> {
                override fun onResponse(
                    call: Call<CakeDetailsResponse>,
                    response: Response<CakeDetailsResponse>
                ) {
                    Log.d(TAG, "Response code: ${response.code()}")
                    Log.d(TAG, "Response body: ${response.body()}")
                    
                    if (response.isSuccessful) {
                        val body = response.body()
                        Log.d(TAG, "Status: ${body?.status}")
                        Log.d(TAG, "Cake: ${body?.cake}")
                        
                        if (body?.status == "success" && body.cake != null) {
                            displayCakeDetails(body.cake!!)
                        } else {
                            showError("Cake not found (status: ${body?.status})")
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e(TAG, "Error response: $errorBody")
                        showError("Failed to load: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<CakeDetailsResponse>, t: Throwable) {
                    Log.e(TAG, "API error", t)
                    showError("Connection error: ${t.message}")
                }
            })
    }

    private fun displayCakeDetails(cake: com.simats.cakeordering.model.CakeDetails) {
        // Update bakerId from API response
        if (cake.baker_id > 0) {
            bakerId = cake.baker_id
        }
        if (!cake.shop_name.isNullOrEmpty()) {
            bakerName = cake.shop_name
        }

        // Cake name
        binding.tvCakeName.text = cake.cake_name ?: "Delicious Cake"

        // Price
        binding.tvPrice.text = "₹${cake.price.toInt()}"

        // Rating
        binding.tvRating.text = "${cake.rating}(${cake.review_count})"

        // Baker name
        binding.tvBakerName.text = cake.shop_name ?: bakerName.ifEmpty { "Local Bakery" }

        // Description
        binding.tvDescription.text = cake.description 
            ?: "A delicious cake freshly baked with premium ingredients. Perfect for any occasion!"

        // Load cake image
        if (!cake.image.isNullOrEmpty()) {
            val imageUrl = BASE_URL + cake.image
            Glide.with(this)
                .load(imageUrl)
                .centerCrop()
                .placeholder(R.drawable.ic_placeholder_image)
                .error(R.drawable.ic_placeholder_image)
                .into(binding.ivCakeImage)
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        binding.tvCakeName.text = "Error loading"
        binding.tvDescription.text = message
    }
}
