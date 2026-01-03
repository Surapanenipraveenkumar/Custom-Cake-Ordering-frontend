package com.simats.cakeordering.model

data class BakerCakesResponse(
    val status: String,
    val cakes: List<BakerCake>
)
