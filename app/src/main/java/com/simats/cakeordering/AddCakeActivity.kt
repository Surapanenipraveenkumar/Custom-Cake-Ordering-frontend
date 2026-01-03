package com.simats.cakeordering

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.simats.cakeordering.api.ApiClient
import com.simats.cakeordering.model.AddCakeRequest
import com.simats.cakeordering.model.BasicResponse
import com.simats.cakeordering.model.CakeEditResponse
import com.simats.cakeordering.model.ImageUploadResponse
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class AddCakeActivity : AppCompatActivity() {

    private var bakerId = 0
    private var cakeId = 0
    private var isEditMode = false
    private var selectedImageUri: Uri? = null
    private var uploadedImageUrl: String = ""

    private lateinit var etCakeName: EditText
    private lateinit var etDescription: EditText
    private lateinit var etPrice: EditText
    private lateinit var imgCakePreview: ImageView
    private lateinit var uploadPlaceholder: LinearLayout
    private lateinit var imageUploadContainer: FrameLayout
    private lateinit var cgShapes: ChipGroup
    private lateinit var cgColors: ChipGroup
    private lateinit var cgFlavors: ChipGroup
    private lateinit var cgToppings: ChipGroup
    private lateinit var btnCreateCake: Button

    private val availableShapes = listOf("Round", "Square", "Heart", "Rectangle", "Oval")
    private val availableColors = listOf("Pink", "Blue", "White", "Purple", "Red", "Yellow", "Green", "Multi-color")
    private val availableFlavors = listOf("Chocolate", "Vanilla", "Strawberry", "Red Velvet", "Lemon", "Carrot", "Marble")
    private val availableToppings = listOf("Sprinkles", "Fruits", "Chocolate Chips", "Nuts", "Edible Flowers", "Fondant Figures", "Macarons")

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            showImagePreview(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_cake)

        // Get intent extras
        bakerId = intent.getIntExtra("baker_id", 0)
        cakeId = intent.getIntExtra("cake_id", 0)
        isEditMode = intent.getBooleanExtra("edit_mode", false)

        if (bakerId == 0) {
            Toast.makeText(this, "Invalid baker", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize views
        etCakeName = findViewById(R.id.etCakeName)
        etDescription = findViewById(R.id.etDescription)
        etPrice = findViewById(R.id.etPrice)
        imgCakePreview = findViewById(R.id.imgCakePreview)
        uploadPlaceholder = findViewById(R.id.uploadPlaceholder)
        imageUploadContainer = findViewById(R.id.imageUploadContainer)
        cgShapes = findViewById(R.id.cgShapes)
        cgColors = findViewById(R.id.cgColors)
        cgFlavors = findViewById(R.id.cgFlavors)
        cgToppings = findViewById(R.id.cgToppings)
        btnCreateCake = findViewById(R.id.btnCreateCake)

        // Back button
        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Update UI for edit mode
        if (isEditMode) {
            btnCreateCake.text = "Update Cake"
        }

        // Populate chips
        populateChipGroup(cgShapes, availableShapes)
        populateChipGroup(cgColors, availableColors)
        populateChipGroup(cgFlavors, availableFlavors)
        populateChipGroup(cgToppings, availableToppings)

        // Image upload click
        imageUploadContainer.setOnClickListener {
            pickImage.launch("image/*")
        }

        // If edit mode, load existing cake data
        if (isEditMode && cakeId > 0) {
            loadCakeData()
        }

        // Create/Update button
        btnCreateCake.setOnClickListener {
            if (selectedImageUri != null) {
                uploadImageAndSave()
            } else {
                saveCake()
            }
        }

        // Cancel button
        findViewById<Button>(R.id.btnCancel).setOnClickListener {
            finish()
        }
    }

    private fun showImagePreview(uri: Uri) {
        uploadPlaceholder.visibility = View.GONE
        imgCakePreview.visibility = View.VISIBLE
        Glide.with(this)
            .load(uri)
            .centerCrop()
            .into(imgCakePreview)
    }

    private fun populateChipGroup(group: ChipGroup, list: List<String>) {
        group.removeAllViews()
        list.forEach {
            val chip = Chip(this)
            chip.text = it
            chip.isCheckable = true
            chip.setChipBackgroundColorResource(android.R.color.white)
            chip.setTextColor(resources.getColor(android.R.color.black, null))
            chip.chipStrokeWidth = 2f
            chip.setChipStrokeColorResource(R.color.chip_stroke_color)
            group.addView(chip)
        }
    }

    private fun selectChips(group: ChipGroup, selectedItems: List<String>) {
        for (i in 0 until group.childCount) {
            val chip = group.getChildAt(i) as? Chip
            if (chip != null && selectedItems.contains(chip.text.toString())) {
                chip.isChecked = true
            }
        }
    }

    private fun getSelected(group: ChipGroup): List<String> {
        return group.checkedChipIds.map {
            group.findViewById<Chip>(it).text.toString()
        }
    }

    private fun loadCakeData() {
        ApiClient.api.getCakeForEdit(cakeId)
            .enqueue(object : Callback<CakeEditResponse> {
                override fun onResponse(
                    call: Call<CakeEditResponse>,
                    response: Response<CakeEditResponse>
                ) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        val cake = response.body()!!.cake

                        etCakeName.setText(cake.cake_name)
                        etDescription.setText(cake.description ?: "")
                        etPrice.setText(cake.price.toString())

                        // Load existing image
                        if (!cake.image.isNullOrEmpty()) {
                            uploadedImageUrl = cake.image
                            uploadPlaceholder.visibility = View.GONE
                            imgCakePreview.visibility = View.VISIBLE
                            val imageUrl = "https://zgt68nw9-80.inc1.devtunnels.ms/Custom-Cake-Ordering/${cake.image}"
                            Glide.with(this@AddCakeActivity)
                                .load(imageUrl)
                                .centerCrop()
                                .into(imgCakePreview)
                        }

                        selectChips(cgShapes, cake.shapes)
                        selectChips(cgColors, cake.colours)
                        selectChips(cgFlavors, cake.flavours)
                        selectChips(cgToppings, cake.toppings)
                    }
                }

                override fun onFailure(call: Call<CakeEditResponse>, t: Throwable) {
                    Toast.makeText(this@AddCakeActivity, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun uploadImageAndSave() {
        btnCreateCake.isEnabled = false
        btnCreateCake.text = "Uploading..."

        try {
            val inputStream = contentResolver.openInputStream(selectedImageUri!!)
            val tempFile = File.createTempFile("cake_", ".jpg", cacheDir)
            val outputStream = FileOutputStream(tempFile)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()

            val mediaType = MediaType.parse("image/*")
            val requestBody = RequestBody.create(mediaType, tempFile)
            val imagePart = MultipartBody.Part.createFormData("image", tempFile.name, requestBody)

            ApiClient.api.uploadCakeImage(imagePart)
                .enqueue(object : Callback<ImageUploadResponse> {
                    override fun onResponse(
                        call: Call<ImageUploadResponse>,
                        response: Response<ImageUploadResponse>
                    ) {
                        tempFile.delete()
                        if (response.isSuccessful && response.body()?.status == "success") {
                            uploadedImageUrl = response.body()?.image_url ?: ""
                            saveCake()
                        } else {
                            btnCreateCake.isEnabled = true
                            btnCreateCake.text = if (isEditMode) "Update Cake" else "Create Cake"
                            Toast.makeText(this@AddCakeActivity, "Image upload failed", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<ImageUploadResponse>, t: Throwable) {
                        tempFile.delete()
                        btnCreateCake.isEnabled = true
                        btnCreateCake.text = if (isEditMode) "Update Cake" else "Create Cake"
                        Toast.makeText(this@AddCakeActivity, "Upload error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        } catch (e: Exception) {
            btnCreateCake.isEnabled = true
            btnCreateCake.text = if (isEditMode) "Update Cake" else "Create Cake"
            Log.e("AddCake", "Image error", e)
            saveCake() // Try saving without image
        }
    }

    private fun saveCake() {
        val name = etCakeName.text.toString().trim()
        val desc = etDescription.text.toString().trim()
        val price = etPrice.text.toString().toDoubleOrNull()

        if (name.isEmpty() || desc.isEmpty() || price == null || price <= 0) {
            Toast.makeText(this, "Fill all required fields", Toast.LENGTH_SHORT).show()
            btnCreateCake.isEnabled = true
            btnCreateCake.text = if (isEditMode) "Update Cake" else "Create Cake"
            return
        }

        if (isEditMode) {
            updateCake(name, desc, price)
        } else {
            createCake(name, desc, price)
        }
    }

    private fun createCake(name: String, desc: String, price: Double) {
        btnCreateCake.isEnabled = false
        btnCreateCake.text = "Creating..."

        val request = AddCakeRequest(
            bakerId = bakerId,
            cakeName = name,
            description = desc,
            price = price,
            image = uploadedImageUrl,
            shapes = getSelected(cgShapes),
            colours = getSelected(cgColors),
            flavours = getSelected(cgFlavors),
            toppings = getSelected(cgToppings)
        )

        ApiClient.api.addCake(request)
            .enqueue(object : Callback<BasicResponse> {
                override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        Toast.makeText(this@AddCakeActivity, "Cake added successfully!", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        btnCreateCake.isEnabled = true
                        btnCreateCake.text = "Create Cake"
                        Toast.makeText(this@AddCakeActivity, "Failed to add cake", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                    btnCreateCake.isEnabled = true
                    btnCreateCake.text = "Create Cake"
                    Toast.makeText(this@AddCakeActivity, t.message, Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun updateCake(name: String, desc: String, price: Double) {
        btnCreateCake.isEnabled = false
        btnCreateCake.text = "Updating..."

        val body = mapOf(
            "cake_id" to cakeId,
            "baker_id" to bakerId,
            "cake_name" to name,
            "description" to desc,
            "price" to price,
            "image" to uploadedImageUrl,
            "shapes" to getSelected(cgShapes),
            "colours" to getSelected(cgColors),
            "flavours" to getSelected(cgFlavors),
            "toppings" to getSelected(cgToppings)
        )

        ApiClient.api.updateCake(body)
            .enqueue(object : Callback<BasicResponse> {
                override fun onResponse(call: Call<BasicResponse>, response: Response<BasicResponse>) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        Toast.makeText(this@AddCakeActivity, "Cake updated successfully!", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        btnCreateCake.isEnabled = true
                        btnCreateCake.text = "Update Cake"
                        Toast.makeText(this@AddCakeActivity, "Failed to update cake", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                    btnCreateCake.isEnabled = true
                    btnCreateCake.text = "Update Cake"
                    Toast.makeText(this@AddCakeActivity, t.message, Toast.LENGTH_LONG).show()
                }
            })
    }
}
