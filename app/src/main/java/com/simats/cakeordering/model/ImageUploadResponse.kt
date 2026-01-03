package com.simats.cakeordering.model

data class ImageUploadResponse(
    val status: String,
    val message: String,
    val image_url: String?
)
