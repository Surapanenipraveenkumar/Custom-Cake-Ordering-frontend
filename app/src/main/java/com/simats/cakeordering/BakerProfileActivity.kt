package com.simats.cakeordering

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.simats.cakeordering.api.ApiClient
import com.simats.cakeordering.model.BasicResponse
import com.simats.cakeordering.model.BakerProfileResponse
import com.simats.cakeordering.model.ImageUploadResponse
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class BakerProfileActivity : AppCompatActivity() {

    private var bakerId: Int = 0
    private lateinit var imgAvatar: ImageView
    private lateinit var switchOnline: androidx.appcompat.widget.SwitchCompat
    private lateinit var txtOnlineStatus: TextView

    // Image picker
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            uploadProfileImage(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_baker_profile)

        bakerId = intent.getIntExtra("baker_id", 0)
        imgAvatar = findViewById(R.id.imgAvatar)
        switchOnline = findViewById(R.id.switchOnline)
        txtOnlineStatus = findViewById(R.id.txtOnlineStatus)

        // Load saved online status
        val prefs = getSharedPreferences("CakeOrderingPrefs", MODE_PRIVATE)
        val isOnline = prefs.getBoolean("baker_online_$bakerId", true)
        switchOnline.isChecked = isOnline
        updateOnlineStatusText(isOnline)

        // Back button
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Online/Offline toggle switch
        switchOnline.setOnCheckedChangeListener { _, isChecked ->
            updateOnlineStatusText(isChecked)
            // Save to SharedPreferences
            prefs.edit().putBoolean("baker_online_$bakerId", isChecked).apply()
            
            // Update status in database via API
            updateOnlineStatusInDatabase(isChecked)
        }

        // Click avatar to upload new image
        imgAvatar.setOnClickListener {
            pickImage.launch("image/*")
        }

        // Edit profile button
        findViewById<Button>(R.id.btnEditProfile).setOnClickListener {
            Toast.makeText(this, "Edit Profile coming soon", Toast.LENGTH_SHORT).show()
        }

        // Logout button
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            // Clear saved data and go to login
            getSharedPreferences("CakeOrderingPrefs", MODE_PRIVATE).edit().clear().apply()
            val intent = Intent(this, LoginTypeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Load profile data
        loadProfile()
    }

    private fun updateOnlineStatusText(isOnline: Boolean) {
        txtOnlineStatus.text = if (isOnline) "Online" else "Offline"
    }

    private fun updateOnlineStatusInDatabase(isOnline: Boolean) {
        val onlineValue = if (isOnline) 1 else 0
        
        ApiClient.api.updateBakerStatus(bakerId, onlineValue)
            .enqueue(object : Callback<BasicResponse> {
                override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        val statusMsg = if (isOnline) "You are now Online" else "You are now Offline"
                        Toast.makeText(this@BakerProfileActivity, statusMsg, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@BakerProfileActivity, "Failed to update status", Toast.LENGTH_SHORT).show()
                        Log.e("BakerProfile", "Status update failed: ${response.body()?.message}")
                    }
                }

                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                    Toast.makeText(this@BakerProfileActivity, "Connection error", Toast.LENGTH_SHORT).show()
                    Log.e("BakerProfile", "Status update error", t)
                }
            })
    }

    private fun uploadProfileImage(uri: Uri) {
        try {
            Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show()

            // Convert URI to File
            val inputStream = contentResolver.openInputStream(uri)
            val tempFile = File.createTempFile("profile_", ".jpg", cacheDir)
            val outputStream = FileOutputStream(tempFile)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()

            // Create multipart request
            val mediaType = MediaType.parse("image/*")
            val requestBody = RequestBody.create(mediaType, tempFile)
            val imagePart = MultipartBody.Part.createFormData("image", tempFile.name, requestBody)
            val bakerIdBody = RequestBody.create(MediaType.parse("text/plain"), bakerId.toString())

            // Upload image
            ApiClient.api.uploadProfileImage(bakerIdBody, imagePart)
                .enqueue(object : Callback<ImageUploadResponse> {
                    override fun onResponse(
                        call: Call<ImageUploadResponse>,
                        response: Response<ImageUploadResponse>
                    ) {
                        if (response.isSuccessful && response.body()?.status == "success") {
                            Toast.makeText(this@BakerProfileActivity, "Profile image updated!", Toast.LENGTH_SHORT).show()
                            // Load the new image
                            val imageUrl = response.body()?.image_url
                            if (!imageUrl.isNullOrEmpty()) {
                                val fullUrl = "https://zgt68nw9-80.inc1.devtunnels.ms/Custom-Cake-Ordering/$imageUrl"
                                Glide.with(this@BakerProfileActivity)
                                    .load(fullUrl)
                                    .placeholder(R.drawable.ic_baker_avatar)
                                    .error(R.drawable.ic_baker_avatar)
                                    .circleCrop()
                                    .into(imgAvatar)
                            }
                        } else {
                            Toast.makeText(this@BakerProfileActivity, "Failed to upload image", Toast.LENGTH_SHORT).show()
                            Log.e("BakerProfile", "Upload failed: ${response.body()?.message}")
                        }
                        tempFile.delete()
                    }

                    override fun onFailure(call: Call<ImageUploadResponse>, t: Throwable) {
                        Toast.makeText(this@BakerProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                        Log.e("BakerProfile", "Upload error", t)
                        tempFile.delete()
                    }
                })
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("BakerProfile", "Image processing error", e)
        }
    }

    private fun loadProfile() {
        Log.d("BakerProfile", "Loading profile for bakerId: $bakerId")
        
        ApiClient.api.getBakerProfile(bakerId)
            .enqueue(object : Callback<BakerProfileResponse> {
                override fun onResponse(
                    call: Call<BakerProfileResponse>,
                    response: Response<BakerProfileResponse>
                ) {
                    Log.d("BakerProfile", "Response code: ${response.code()}")
                    
                    if (response.isSuccessful) {
                        val body = response.body()
                        Log.d("BakerProfile", "Response body: $body")
                        
                        if (body?.status == "success" && body.baker != null) {
                            displayProfile(body.baker)
                        } else {
                            val errorMsg = body?.status ?: "Unknown error"
                            Log.e("BakerProfile", "API error: $errorMsg")
                            Toast.makeText(this@BakerProfileActivity, "Failed to load profile: $errorMsg", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("BakerProfile", "HTTP error ${response.code()}: $errorBody")
                        Toast.makeText(this@BakerProfileActivity, "Server error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<BakerProfileResponse>, t: Throwable) {
                    Log.e("BakerProfile", "Network error", t)
                    Toast.makeText(this@BakerProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun displayProfile(profile: com.simats.cakeordering.model.BakerProfile) {
        // Profile card
        findViewById<TextView>(R.id.txtShopName).text = profile.shop_name
        findViewById<TextView>(R.id.txtOwnerName).text = profile.owner_name

        // Load profile image if available
        if (!profile.profile_image.isNullOrEmpty()) {
            val fullUrl = "https://zgt68nw9-80.inc1.devtunnels.ms/Custom-Cake-Ordering/${profile.profile_image}"
            Glide.with(this)
                .load(fullUrl)
                .placeholder(R.drawable.ic_baker_avatar)
                .error(R.drawable.ic_baker_avatar)
                .circleCrop()
                .into(imgAvatar)
        }

        // Stats
        findViewById<TextView>(R.id.txtTotalOrders).text = profile.total_orders.toString()
        findViewById<TextView>(R.id.txtRating).text = String.format("%.1f", profile.rating)
        
        // Format income
        val income = if (profile.monthly_income >= 1000) {
            String.format("₹%.1fk", profile.monthly_income / 1000)
        } else {
            "₹${profile.monthly_income.toInt()}"
        }
        findViewById<TextView>(R.id.txtMonthlyIncome).text = income

        // Specialty and Experience
        findViewById<TextView>(R.id.txtSpecialty).text = profile.specialty ?: "Custom Cakes"
        val expText = if ((profile.years_experience ?: 0) > 0) {
            "${profile.years_experience} years experience"
        } else {
            "New Baker"
        }
        findViewById<TextView>(R.id.txtExperience).text = expText

        // Set online status from database
        val isOnline = profile.is_online ?: true
        switchOnline.isChecked = isOnline
        updateOnlineStatusText(isOnline)

        // Contact Information
        findViewById<TextView>(R.id.txtEmail).text = profile.email.ifEmpty { "Not set" }
        findViewById<TextView>(R.id.txtPhone).text = profile.phone.ifEmpty { "Not set" }
        findViewById<TextView>(R.id.txtAddress).text = profile.address.ifEmpty { "Not set" }
        
        // About
        findViewById<TextView>(R.id.txtDescription).text = profile.description
    }
}
