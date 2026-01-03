package com.simats.cakeordering

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.simats.cakeordering.api.ApiClient
import com.simats.cakeordering.databinding.ActivityAiCakeGeneratorBinding
import com.simats.cakeordering.model.AiCakeResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AiCakeGeneratorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAiCakeGeneratorBinding
    private var currentPrompt: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAiCakeGeneratorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Back button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Generate button
        binding.btnGenerate.setOnClickListener {
            val prompt = binding.etPrompt.text.toString().trim()
            if (prompt.isEmpty()) {
                Toast.makeText(this, "Please describe your dream cake", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            currentPrompt = prompt
            generateCakeDesigns(prompt)
        }

        // Regenerate button - uses same prompt
        binding.btnRegenerate.setOnClickListener {
            if (currentPrompt.isNotEmpty()) {
                generateCakeDesigns(currentPrompt)
            }
        }
    }

    private fun generateCakeDesigns(prompt: String) {
        // Show loading state
        binding.loadingState.visibility = View.VISIBLE
        binding.resultsSection.visibility = View.GONE
        binding.btnGenerate.isEnabled = false

        val body = mapOf("prompt" to prompt)

        // Use aiApi for long-running AI generation (has 120s timeout)
        ApiClient.aiApi.generateAiCake(body).enqueue(object : Callback<AiCakeResponse> {
            override fun onResponse(call: Call<AiCakeResponse>, response: Response<AiCakeResponse>) {
                binding.loadingState.visibility = View.GONE
                binding.btnGenerate.isEnabled = true

                val result = response.body()
                if (response.isSuccessful && result?.status == "success" && !result.images.isNullOrEmpty()) {
                    displayGeneratedImages(result.images)
                } else {
                    val errorMsg = result?.message ?: "Failed to generate cake designs"
                    Toast.makeText(this@AiCakeGeneratorActivity, errorMsg, Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<AiCakeResponse>, t: Throwable) {
                binding.loadingState.visibility = View.GONE
                binding.btnGenerate.isEnabled = true
                Toast.makeText(
                    this@AiCakeGeneratorActivity,
                    "Network error: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun displayGeneratedImages(images: List<String>) {
        binding.resultsSection.visibility = View.VISIBLE

        val imageViews = listOf(
            binding.ivCake1,
            binding.ivCake2,
            binding.ivCake3,
            binding.ivCake4
        )

        images.forEachIndexed { index, imageUrl ->
            if (index < imageViews.size) {
                Glide.with(this)
                    .load(imageUrl)
                    .placeholder(android.R.color.darker_gray)
                    .error(android.R.color.holo_red_light)
                    .centerCrop()
                    .into(imageViews[index])
            }
        }

        Toast.makeText(this, "🎂 4 AI designs generated!", Toast.LENGTH_SHORT).show()
    }
}
