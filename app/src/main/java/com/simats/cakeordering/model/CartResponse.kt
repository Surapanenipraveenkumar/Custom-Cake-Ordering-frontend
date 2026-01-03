package com.simats.cakeordering.model

import com.google.gson.annotations.SerializedName

data class CartResponse(
    val status: String,
    val cart: List<CartItem>,
    @SerializedName("cart_total")
    val cartTotal: Double
)

data class CartItem(
    @SerializedName("cart_id")
    val cartId: Int,
    @SerializedName("cake_id")
    val cakeId: Int,
    @SerializedName("cake_name")
    val cakeName: String,
    val price: Double,
    val quantity: Int,
    val image: String?,
    val availability: Int,
    @SerializedName("item_total")
    val itemTotal: Double,
    // Customization fields (optional)
    val weight: String? = null,
    val shape: String? = null,
    val color: String? = null,
    val flavor: String? = null,
    val toppings: String? = null
)
