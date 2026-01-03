package com.simats.cakeordering

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.simats.cakeordering.api.ApiClient
import com.simats.cakeordering.model.BasicResponse
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

class BakerRegisterActivity : AppCompatActivity() {

    private lateinit var etShopName: EditText
    private lateinit var etOwnerName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var etAddress: EditText
    private lateinit var btnCreateAccount: Button
    private lateinit var imgProfile: ImageView
    private lateinit var btnSetLocation: Button
    private lateinit var tvLocationStatus: TextView
    private lateinit var spinnerSpecialty: android.widget.Spinner
    private lateinit var etYearsExperience: EditText
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var selectedImageUri: Uri? = null
    private var bakerLatitude: Double = 0.0
    private var bakerLongitude: Double = 0.0
    private var isLocationSet: Boolean = false

    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 1003
    }

    // Image picker
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            Glide.with(this)
                .load(it)
                .circleCrop()
                .into(imgProfile)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_baker_register)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialize views
        etShopName = findViewById(R.id.etShopName)
        etOwnerName = findViewById(R.id.etOwnerName)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        etAddress = findViewById(R.id.etAddress)
        btnCreateAccount = findViewById(R.id.btnCreateAccount)
        imgProfile = findViewById(R.id.imgProfile)
        btnSetLocation = findViewById(R.id.btnSetLocation)
        tvLocationStatus = findViewById(R.id.tvLocationStatus)
        spinnerSpecialty = findViewById(R.id.spinnerSpecialty)
        etYearsExperience = findViewById(R.id.etYearsExperience)

        // Setup specialty spinner
        setupSpecialtySpinner()

        // Back button
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Login link
        findViewById<TextView>(R.id.txtLogin).setOnClickListener {
            finish() // Go back to login
        }

        // Profile image picker
        imgProfile.setOnClickListener {
            pickImage.launch("image/*")
        }
        findViewById<ImageView>(R.id.btnPickImage).setOnClickListener {
            pickImage.launch("image/*")
        }

        // Set Location button
        btnSetLocation.setOnClickListener {
            getShopLocation()
        }

        // Create account button
        btnCreateAccount.setOnClickListener {
            registerBaker()
        }
    }

    private fun setupSpecialtySpinner() {
        val specialties = arrayOf(
            "Select Specialty",
            "Custom Cakes",
            "Wedding Cakes",
            "Birthday Cakes",
            "Cupcakes",
            "Pastries",
            "Cookies",
            "Brownies",
            "Cheesecakes",
            "Vegan Cakes",
            "Eggless Cakes",
            "Photo Cakes",
            "Theme Cakes",
            "Other"
        )
        val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, specialties)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSpecialty.adapter = adapter
    }

    private fun getShopLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
            return
        }

        tvLocationStatus.text = "📍 Getting location..."
        btnSetLocation.isEnabled = false

        val cancellationToken = CancellationTokenSource()
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationToken.token)
            .addOnSuccessListener { location: Location? ->
                btnSetLocation.isEnabled = true
                if (location != null) {
                    bakerLatitude = location.latitude
                    bakerLongitude = location.longitude
                    isLocationSet = true
                    
                    // Get address from location
                    try {
                        val geocoder = Geocoder(this, Locale.getDefault())
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(bakerLatitude, bakerLongitude, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val addr = addresses[0]
                            val locationName = addr.locality ?: addr.subAdminArea ?: "Location Set"
                            tvLocationStatus.text = "📍 $locationName (${String.format("%.4f", bakerLatitude)}, ${String.format("%.4f", bakerLongitude)})"
                        } else {
                            tvLocationStatus.text = "📍 Location Set (${String.format("%.4f", bakerLatitude)}, ${String.format("%.4f", bakerLongitude)})"
                        }
                    } catch (e: Exception) {
                        tvLocationStatus.text = "📍 Location Set (${String.format("%.4f", bakerLatitude)}, ${String.format("%.4f", bakerLongitude)})"
                    }
                    
                    tvLocationStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
                    Toast.makeText(this, "Location set successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    tvLocationStatus.text = "📍 Could not get location"
                    Toast.makeText(this, "Could not get location", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                btnSetLocation.isEnabled = true
                tvLocationStatus.text = "📍 Location error"
                Toast.makeText(this, "Error getting location", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getShopLocation()
            } else {
                Toast.makeText(this, "Location permission required to set shop location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerBaker() {
        val shopName = etShopName.text.toString().trim()
        val ownerName = etOwnerName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val password = etPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()
        val address = etAddress.text.toString().trim()

        // Validation
        if (shopName.isEmpty()) {
            etShopName.error = "Shop name required"
            etShopName.requestFocus()
            return
        }

        if (ownerName.isEmpty()) {
            etOwnerName.error = "Owner name required"
            etOwnerName.requestFocus()
            return
        }

        if (email.isEmpty()) {
            etEmail.error = "Email required"
            etEmail.requestFocus()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Invalid email format"
            etEmail.requestFocus()
            return
        }

        if (phone.isEmpty()) {
            etPhone.error = "Phone number required"
            etPhone.requestFocus()
            return
        }

        if (password.isEmpty()) {
            etPassword.error = "Password required"
            etPassword.requestFocus()
            return
        }

        if (password.length < 6) {
            etPassword.error = "Password must be at least 6 characters"
            etPassword.requestFocus()
            return
        }

        if (password != confirmPassword) {
            etConfirmPassword.error = "Passwords don't match"
            etConfirmPassword.requestFocus()
            return
        }

        // Get specialty and years experience
        val specialty = if (spinnerSpecialty.selectedItemPosition > 0) {
            spinnerSpecialty.selectedItem.toString()
        } else {
            "Custom Cakes"
        }
        val yearsExperience = etYearsExperience.text.toString().toIntOrNull() ?: 0

        // Disable button and show loading
        btnCreateAccount.isEnabled = false
        btnCreateAccount.text = "Creating Account..."

        // If image is selected, use multipart upload
        if (selectedImageUri != null) {
            registerWithImage(shopName, ownerName, email, phone, password, address, specialty, yearsExperience)
        } else {
            // Use regular form upload
            registerWithoutImage(shopName, ownerName, email, phone, password, address, specialty, yearsExperience)
        }
    }

    private fun registerWithoutImage(shopName: String, ownerName: String, email: String, phone: String, password: String, address: String, specialty: String, yearsExperience: Int) {
        ApiClient.api.registerBaker(shopName, ownerName, email, phone, password, address, bakerLatitude, bakerLongitude, specialty, yearsExperience)
            .enqueue(object : Callback<BasicResponse> {
                override fun onResponse(
                    call: Call<BasicResponse>,
                    response: Response<BasicResponse>
                ) {
                    handleResponse(response)
                }

                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                    handleError(t)
                }
            })
    }

    private fun registerWithImage(shopName: String, ownerName: String, email: String, phone: String, password: String, address: String, specialty: String, yearsExperience: Int) {
        try {
            // Convert URI to File
            val inputStream = contentResolver.openInputStream(selectedImageUri!!)
            val tempFile = File.createTempFile("profile_", ".jpg", cacheDir)
            val outputStream = FileOutputStream(tempFile)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()

            // Create multipart request
            val mediaType = MediaType.parse("image/*")
            val requestBody = RequestBody.create(mediaType, tempFile)
            val imagePart = MultipartBody.Part.createFormData("image", tempFile.name, requestBody)

            // Create form fields
            val shopNameBody = RequestBody.create(MediaType.parse("text/plain"), shopName)
            val ownerNameBody = RequestBody.create(MediaType.parse("text/plain"), ownerName)
            val emailBody = RequestBody.create(MediaType.parse("text/plain"), email)
            val phoneBody = RequestBody.create(MediaType.parse("text/plain"), phone)
            val passwordBody = RequestBody.create(MediaType.parse("text/plain"), password)
            val addressBody = RequestBody.create(MediaType.parse("text/plain"), address)
            val latitudeBody = RequestBody.create(MediaType.parse("text/plain"), bakerLatitude.toString())
            val longitudeBody = RequestBody.create(MediaType.parse("text/plain"), bakerLongitude.toString())
            val specialtyBody = RequestBody.create(MediaType.parse("text/plain"), specialty)
            val yearsExpBody = RequestBody.create(MediaType.parse("text/plain"), yearsExperience.toString())

            ApiClient.api.registerBakerWithImage(shopNameBody, ownerNameBody, emailBody, phoneBody, passwordBody, addressBody, latitudeBody, longitudeBody, specialtyBody, yearsExpBody, imagePart)
                .enqueue(object : Callback<BasicResponse> {
                    override fun onResponse(
                        call: Call<BasicResponse>,
                        response: Response<BasicResponse>
                    ) {
                        tempFile.delete()
                        handleResponse(response)
                    }

                    override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                        tempFile.delete()
                        handleError(t)
                    }
                })
        } catch (e: Exception) {
            Log.e("BakerRegister", "Image processing error", e)
            // Fallback to registration without image
            registerWithoutImage(shopName, ownerName, email, phone, password, address, specialty, yearsExperience)
        }
    }

    private fun handleResponse(response: Response<BasicResponse>) {
        btnCreateAccount.isEnabled = true
        btnCreateAccount.text = "Create Account"

        if (response.isSuccessful && response.body()?.status == "success") {
            Toast.makeText(
                this,
                "Account created successfully! Please login.",
                Toast.LENGTH_LONG
            ).show()
            finish() // Go back to login
        } else {
            val message = response.body()?.message ?: "Registration failed"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            Log.e("BakerRegister", "Registration failed: $message")
        }
    }

    private fun handleError(t: Throwable) {
        btnCreateAccount.isEnabled = true
        btnCreateAccount.text = "Create Account"
        Toast.makeText(
            this,
            "Error: ${t.message}",
            Toast.LENGTH_SHORT
        ).show()
        Log.e("BakerRegister", "Error", t)
    }
}
