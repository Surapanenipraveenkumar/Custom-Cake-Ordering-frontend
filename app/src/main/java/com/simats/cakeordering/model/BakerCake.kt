package com.simats.cakeordering.model

data class BakerCake(
    val cake_id: Int,
    val cake_name: String,
    val price: Int,
    val image: String?,
    val orders: Int
)
