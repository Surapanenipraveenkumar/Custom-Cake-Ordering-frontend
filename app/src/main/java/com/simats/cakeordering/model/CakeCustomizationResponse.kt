package com.simats.cakeordering.model

import com.google.gson.annotations.SerializedName

data class CakeCustomizationResponse(
    val status: String,

    @SerializedName("customization_options")
    val customizationOptions: CustomizationOptions
)

data class CustomizationOptions(
    val shapes: List<String>,
    val colours: List<String>,
    val flavours: List<String>,
    val toppings: List<String>
)
