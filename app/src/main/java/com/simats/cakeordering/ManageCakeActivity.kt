package com.simats.cakeordering

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.simats.cakeordering.api.ApiClient
import com.simats.cakeordering.databinding.ActivityManageCakeBinding
import com.simats.cakeordering.model.BasicResponse
import com.simats.cakeordering.model.CakeDetailsResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ManageCakeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageCakeBinding
    private var cakeId: Int = 0
    private var bakerId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityManageCakeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get cake ID and baker ID from intent
        cakeId = intent.getIntExtra("cake_id", 0)
        bakerId = intent.getIntExtra("baker_id", 0)

        if (cakeId == 0) {
            Toast.makeText(this, "Invalid cake", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Back button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Edit button
        binding.btnEdit.setOnClickListener {
            startActivity(
                Intent(this, AddCakeActivity::class.java)
                    .putExtra("baker_id", bakerId)
                    .putExtra("cake_id", cakeId)
                    .putExtra("edit_mode", true)
            )
        }

        // Delete button
        binding.btnDelete.setOnClickListener {
            showDeleteConfirmation()
        }

        // Load cake details
        loadCakeDetails()
    }

    override fun onResume() {
        super.onResume()
        loadCakeDetails()
    }

    private fun loadCakeDetails() {
        ApiClient.api.getCakeDetails(cakeId)
            .enqueue(object : Callback<CakeDetailsResponse> {
                override fun onResponse(
                    call: Call<CakeDetailsResponse>,
                    response: Response<CakeDetailsResponse>
                ) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        val cake = response.body()?.cake
                        if (cake != null) {
                            binding.txtCakeName.text = cake.cake_name ?: "Unknown Cake"
                            binding.txtCakeDescription.text = cake.description ?: "No description"
                            binding.txtCakePrice.text = "₹${cake.price.toInt()}"

                            // Load image
                            if (!cake.image.isNullOrEmpty()) {
                                val imageUrl = "https://zgt68nw9-80.inc1.devtunnels.ms/Custom-Cake-Ordering/${cake.image}"
                                Glide.with(this@ManageCakeActivity)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.sample_cake)
                                    .error(R.drawable.sample_cake)
                                    .into(binding.imgCake)
                            }
                        } else {
                            Toast.makeText(
                                this@ManageCakeActivity,
                                "Cake not found",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@ManageCakeActivity,
                            "Failed to load cake details",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<CakeDetailsResponse>, t: Throwable) {
                    Toast.makeText(
                        this@ManageCakeActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Delete Cake")
            .setMessage("Are you sure you want to delete this cake?")
            .setPositiveButton("Delete") { _, _ ->
                deleteCake()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteCake() {
        ApiClient.api.deleteCake(cakeId)
            .enqueue(object : Callback<BasicResponse> {
                override fun onResponse(
                    call: Call<BasicResponse>,
                    response: Response<BasicResponse>
                ) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        Toast.makeText(
                            this@ManageCakeActivity,
                            "Cake deleted successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@ManageCakeActivity,
                            "Failed to delete cake",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<BasicResponse>, t: Throwable) {
                    Toast.makeText(
                        this@ManageCakeActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
}
