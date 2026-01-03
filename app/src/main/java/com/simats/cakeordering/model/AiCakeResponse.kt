package com.simats.cakeordering.model

import com.google.gson.annotations.SerializedName

data class AiCakeResponse(
    @SerializedName("status")
    val status: String,
    
    @SerializedName("images")
    val images: List<String>? = null,
    
    @SerializedName("prompt_used")
    val promptUsed: String? = null,
    
    @SerializedName("message")
    val message: String? = null
)
