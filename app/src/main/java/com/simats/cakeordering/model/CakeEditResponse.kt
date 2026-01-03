package com.simats.cakeordering.model

data class CakeEditResponse(
    val status: String,
    val cake: CakeEditData
)

data class CakeEditData(
    val cake_id: Int,
    val cake_name: String,
    val description: String?,
    val price: Int,
    val image: String?,
    val shapes: List<String>,
    val colours: List<String>,
    val flavours: List<String>,
    val toppings: List<String>
)
