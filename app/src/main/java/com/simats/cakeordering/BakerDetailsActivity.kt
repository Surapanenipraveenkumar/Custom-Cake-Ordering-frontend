package com.simats.cakeordering

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.flexbox.FlexboxLayout
import com.simats.cakeordering.api.ApiClient
import com.simats.cakeordering.model.BakerProfileResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BakerDetailsActivity : AppCompatActivity() {

    private var bakerId: Int = 0
    private var shopName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_baker_details)

        // Get baker ID from intent
        bakerId = intent.getIntExtra("baker_id", 0)
        shopName = intent.getStringExtra("shop_name") ?: ""

        // Back button
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Chat button - Customer chats with Baker
        findViewById<Button>(R.id.btnChat).setOnClickListener {
            // Get user_id from SharedPreferences (same as customer login)
            val prefs = getSharedPreferences("CakeOrderingPrefs", MODE_PRIVATE)
            val userId = prefs.getInt("user_id", 0)
            
            if (userId > 0) {
                val intent = Intent(this, CustomerChatActivity::class.java)
                intent.putExtra("baker_id", bakerId)
                intent.putExtra("user_id", userId)
                intent.putExtra("baker_name", shopName)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Please login to chat", Toast.LENGTH_SHORT).show()
            }
        }

        // Load baker details
        if (bakerId > 0) {
            loadBakerDetails()
        } else {
            Toast.makeText(this, "Invalid baker", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadBakerDetails() {
        ApiClient.api.getBakerProfile(bakerId)
            .enqueue(object : Callback<BakerProfileResponse> {
                override fun onResponse(
                    call: Call<BakerProfileResponse>,
                    response: Response<BakerProfileResponse>
                ) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        response.body()?.baker?.let { baker ->
                            displayBakerDetails(baker)
                        }
                    } else {
                        Toast.makeText(this@BakerDetailsActivity, "Failed to load baker details", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<BakerProfileResponse>, t: Throwable) {
                    Toast.makeText(this@BakerDetailsActivity, "Connection error", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun displayBakerDetails(baker: com.simats.cakeordering.model.BakerProfile) {
        // Shop name
        findViewById<TextView>(R.id.txtShopName).text = baker.shop_name
        shopName = baker.shop_name

        // Owner name
        findViewById<TextView>(R.id.txtOwnerName).text = "by ${baker.owner_name}"

        // Rating
        findViewById<TextView>(R.id.txtRating).text = String.format("%.1f", baker.rating)
        findViewById<TextView>(R.id.txtReviews).text = "(${baker.total_orders} orders)"

        // Address
        findViewById<TextView>(R.id.txtAddress).text = baker.address.ifEmpty { "Location not available" }

        // About / Description
        findViewById<TextView>(R.id.txtAbout).text = baker.description

        // Experience
        val expText = if ((baker.years_experience ?: 0) > 0) {
            "${baker.years_experience} years of professional baking"
        } else {
            "New to the platform"
        }
        findViewById<TextView>(R.id.txtExperience).text = expText

        // Profile image
        if (!baker.profile_image.isNullOrEmpty()) {
            val fullUrl = "https://zgt68nw9-80.inc1.devtunnels.ms/Custom-Cake-Ordering/${baker.profile_image}"
            Glide.with(this)
                .load(fullUrl)
                .placeholder(R.drawable.ic_baker_avatar)
                .error(R.drawable.ic_baker_avatar)
                .circleCrop()
                .into(findViewById(R.id.imgBaker))
        }

        // Specialties
        val flexbox = findViewById<FlexboxLayout>(R.id.flexSpecialties)
        flexbox.removeAllViews()

        // Add primary specialty
        val specialty = baker.specialty ?: "Custom Cakes"
        addSpecialtyTag(flexbox, specialty)

        // Add some related tags based on specialty
        val relatedTags = getRelatedTags(specialty)
        relatedTags.forEach { tag ->
            addSpecialtyTag(flexbox, tag)
        }
    }

    private fun addSpecialtyTag(flexbox: FlexboxLayout, text: String) {
        val tag = TextView(this).apply {
            this.text = text
            textSize = 13f
            setTextColor(resources.getColor(android.R.color.holo_red_light, null))
            setPadding(32, 16, 32, 16)
            setBackgroundResource(R.drawable.bg_tag_pink)
            
            val params = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 16, 16)
            layoutParams = params
        }
        flexbox.addView(tag)
    }

    private fun getRelatedTags(specialty: String): List<String> {
        return when {
            specialty.contains("Wedding", ignoreCase = true) -> listOf("Custom Designs", "Tiered Cakes")
            specialty.contains("Birthday", ignoreCase = true) -> listOf("Theme Cakes", "Photo Cakes")
            specialty.contains("Chocolate", ignoreCase = true) -> listOf("Truffle Cakes", "Ganache")
            specialty.contains("Cupcake", ignoreCase = true) -> listOf("Mini Cakes", "Party Packs")
            specialty.contains("Vegan", ignoreCase = true) -> listOf("Eggless", "Healthy Options")
            else -> listOf("Custom Designs", "Birthday Cakes")
        }
    }
}
