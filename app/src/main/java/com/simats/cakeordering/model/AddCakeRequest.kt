package com.simats.cakeordering.model

import com.google.gson.annotations.SerializedName

data class AddCakeRequest(
    @SerializedName("baker_id")
    val bakerId: Int,
    @SerializedName("cake_name")
    val cakeName: String,
    val description: String,
    val price: Double,
    val image: String,
    val shapes: List<String>,
    val colours: List<String>,
    val flavours: List<String>,
    val toppings: List<String>
)

data class AddCakeResponse(
    val status: String,
    val message: String
)
