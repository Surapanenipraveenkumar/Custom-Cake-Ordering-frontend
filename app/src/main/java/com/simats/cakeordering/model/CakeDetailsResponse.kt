package com.simats.cakeordering.model

import com.google.gson.annotations.SerializedName

data class CakeDetailsResponse(
    val status: String,
    val cake: CakeDetails?
)

data class CakeDetails(
    @SerializedName("cake_id")
    val cake_id: Int = 0,
    @SerializedName("baker_id")
    val baker_id: Int = 0,
    @SerializedName("cake_name")
    val cake_name: String? = null,
    @SerializedName("shop_name")
    val shop_name: String? = null,
    val description: String? = null,
    val price: Double = 0.0,
    val image: String? = null,
    val availability: Int = 1,
    val rating: Double = 4.5,
    val review_count: Int = 0
)
