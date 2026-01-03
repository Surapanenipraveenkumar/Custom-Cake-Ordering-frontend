package com.simats.cakeordering.model

import com.google.gson.annotations.SerializedName

data class CakeCustomizationOptions(
    @SerializedName("shapes")
    val shapes: List<String>,

    @SerializedName("colours")
    val colours: List<String>,

    @SerializedName("flavours")
    val flavours: List<String>,

    @SerializedName("toppings")
    val toppings: List<String>
)
