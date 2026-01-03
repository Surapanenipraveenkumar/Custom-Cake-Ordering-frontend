package com.simats.cakeordering

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.chip.Chip
import com.simats.cakeordering.api.ApiClient
import com.simats.cakeordering.databinding.ActivityCakeCustomizationBinding
import com.simats.cakeordering.model.BasicResponse
import com.simats.cakeordering.model.CakeCustomizationResponse
import com.simats.cakeordering.model.CakeDetails
import com.simats.cakeordering.model.CakeDetailsResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CakeCustomizationActivity : AppCompatActivity() {

    companion object {
        private const val BASE_URL = "https://zgt68nw9-80.inc1.devtunnels.ms/Custom-Cake-Ordering/"
        private const val TAG = "CakeCustomization"
    }

    private lateinit var binding: ActivityCakeCustomizationBinding
    private var cakeId = 0
    private var isFavorite = false
    private var isCustomizationVisible = true

    // Selected customization options
    private var selectedWeight: String? = null
    private var selectedShape: String? = null
    private var selectedColor: String? = null
    private var selectedFlavor: String? = null
    private var selectedToppings = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCakeCustomizationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cakeId = intent.getIntExtra("cake_id", 0)

        if (cakeId == 0) {
            Toast.makeText(this, "Invalid cake", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupClickListeners()
        loadCakeDetails()
        loadCustomizationOptions()
    }

    private fun setupClickListeners() {
        // Back button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Favorite button
        binding.btnFavorite.setOnClickListener {
            isFavorite = !isFavorite
            updateFavoriteIcon()
            val msg = if (isFavorite) "Added to favorites" else "Removed from favorites"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        // Toggle customization visibility
        binding.btnToggleCustomization.setOnClickListener {
            isCustomizationVisible = !isCustomizationVisible
            if (isCustomizationVisible) {
                binding.customizationContainer.visibility = View.VISIBLE
                binding.btnToggleCustomization.text = "Hide Customization"
            } else {
                binding.customizationContainer.visibility = View.GONE
                binding.btnToggleCustomization.text = "Show Customization"
            }
        }

        // Add to cart
        binding.btnAddToCart.setOnClickListener {
            addToCart()
        }
    }

    private fun updateFavoriteIcon() {
        val color = if (isFavorite) 0xFFEC4899.toInt() else 0xFFE5E7EB.toInt()
        binding.ivFavorite.setColorFilter(color)
    }

    // ---------- LOAD CAKE DETAILS ----------
    private fun loadCakeDetails() {
        binding.progressBar.visibility = View.VISIBLE

        ApiClient.api.getCakeDetails(cakeId)
            .enqueue(object : Callback<CakeDetailsResponse> {
                override fun onResponse(
                    call: Call<CakeDetailsResponse>,
                    response: Response<CakeDetailsResponse>
                ) {
                    binding.progressBar.visibility = View.GONE

                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.status == "success" && body.cake != null) {
                            displayCakeDetails(body.cake!!)
                        } else {
                            showError("Cake not found")
                        }
                    } else {
                        showError("Failed to load cake details")
                    }
                }

                override fun onFailure(call: Call<CakeDetailsResponse>, t: Throwable) {
                    binding.progressBar.visibility = View.GONE
                    showError("Connection error: ${t.message}")
                }
            })
    }

    private fun displayCakeDetails(cake: CakeDetails) {
        // Cake name
        binding.tvCakeName.text = cake.cake_name ?: "Delicious Cake"

        // Price
        binding.tvPrice.text = "₹${cake.price.toInt()}"

        // Rating
        binding.tvRating.text = "${cake.rating} (${cake.review_count})"

        // Baker name
        binding.tvBakerName.text = cake.shop_name ?: "Local Bakery"

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

    // ---------- LOAD CUSTOMIZATION OPTIONS ----------
    private fun loadCustomizationOptions() {
        ApiClient.api.getCakeCustomizationOptions(cakeId)
            .enqueue(object : Callback<CakeCustomizationResponse> {

                override fun onResponse(
                    call: Call<CakeCustomizationResponse>,
                    response: Response<CakeCustomizationResponse>
                ) {
                    val body = response.body()
                    if (response.isSuccessful && body?.status == "success") {
                        val options = body.customizationOptions
                        // Add default weight options
                        val defaultWeights = listOf("0.5 Kg", "1 Kg", "1.5 Kg", "2 Kg", "3 Kg")
                        addSingleChoiceChips(binding.cgWeight, defaultWeights) { selectedWeight = it }
                        addSingleChoiceChips(binding.cgShape, options.shapes) { selectedShape = it }
                        addSingleChoiceChips(binding.cgColor, options.colours) { selectedColor = it }
                        addSingleChoiceChips(binding.cgFlavor, options.flavours) { selectedFlavor = it }
                        addMultiChoiceChips(binding.cgToppings, options.toppings)
                    } else {
                        // Show default options if none available from API
                        Log.w(TAG, "Customization options not available for this cake, using defaults")
                        val defaultWeights = listOf("0.5 Kg", "1 Kg", "1.5 Kg", "2 Kg", "3 Kg")
                        addSingleChoiceChips(binding.cgWeight, defaultWeights) { selectedWeight = it }
                    }
                }

                override fun onFailure(
                    call: Call<CakeCustomizationResponse>,
                    t: Throwable
                ) {
                    Log.e(TAG, "Failed to load customization options", t)
                }
            })
    }

    // ---------- CHIP HELPERS ----------
    private fun addSingleChoiceChips(
        container: FlexboxLayout,
        list: List<String>,
        onSelect: (String) -> Unit
    ) {
        container.removeAllViews()
        var selectedChip: Chip? = null

        list.forEachIndexed { index, text ->
            val chip = createChip(text)
            chip.setOnClickListener {
                // Deselect previous
                selectedChip?.isChecked = false
                // Select this one
                chip.isChecked = true
                selectedChip = chip
                onSelect(text)
            }
            // Auto-select first option
            if (index == 0) {
                chip.isChecked = true
                selectedChip = chip
                onSelect(text)
            }
            container.addView(chip)
        }
    }

    private fun addMultiChoiceChips(container: FlexboxLayout, list: List<String>) {
        container.removeAllViews()
        selectedToppings.clear()

        list.forEachIndexed { index, text ->
            val chip = createChip(text)
            chip.setOnClickListener {
                chip.isChecked = !chip.isChecked
                if (chip.isChecked) {
                    selectedToppings.add(text)
                } else {
                    selectedToppings.remove(text)
                }
            }
            // Auto-select first two options for demo
            if (index < 2) {
                chip.isChecked = true
                selectedToppings.add(text)
            }
            container.addView(chip)
        }
    }

    private fun createChip(text: String): Chip {
        return Chip(this).apply {
            this.text = text
            isCheckable = true
            isClickable = true
            chipBackgroundColor = ContextCompat.getColorStateList(
                this@CakeCustomizationActivity,
                R.color.chip_selector
            )
            chipStrokeWidth = 2f
            chipStrokeColor = ContextCompat.getColorStateList(
                this@CakeCustomizationActivity,
                R.color.chip_stroke_selector
            )
            setTextColor(ContextCompat.getColorStateList(
                this@CakeCustomizationActivity,
                R.color.chip_text_selector
            ))
            shapeAppearanceModel = shapeAppearanceModel.toBuilder()
                .setAllCornerSizes(24f)
                .build()
            chipMinHeight = 40f
            val params = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 16, 12)
            layoutParams = params
        }
    }

    // ---------- ADD TO CART ----------
    private fun addToCart() {
        val prefs = getSharedPreferences("CakeOrderingPrefs", Context.MODE_PRIVATE)
        val userId = prefs.getInt("user_id", 0)

        if (userId == 0) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            return
        }

        val body = mapOf(
            "user_id" to userId,
            "cake_id" to cakeId,
            "quantity" to 1,
            "weight" to (selectedWeight ?: "1 Kg"),
            "shape" to (selectedShape ?: ""),
            "color" to (selectedColor ?: ""),
            "flavor" to (selectedFlavor ?: ""),
            "toppings" to selectedToppings.joinToString(",")
        )

        ApiClient.api.addToCart(body)
            .enqueue(object : Callback<BasicResponse> {

                override fun onResponse(
                    call: Call<BasicResponse>,
                    response: Response<BasicResponse>
                ) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        Toast.makeText(
                            this@CakeCustomizationActivity,
                            "Added to cart!",
                            Toast.LENGTH_SHORT
                        ).show()
                        startActivity(
                            Intent(
                                this@CakeCustomizationActivity,
                                CartActivity::class.java
                            )
                        )
                    } else {
                        Toast.makeText(
                            this@CakeCustomizationActivity,
                            response.body()?.message ?: "Add to cart failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(
                    call: Call<BasicResponse>,
                    t: Throwable
                ) {
                    Toast.makeText(
                        this@CakeCustomizationActivity,
                        t.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
}
