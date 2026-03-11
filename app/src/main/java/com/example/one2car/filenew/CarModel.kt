package com.example.one2car.filenew

data class Car(

    val car_id: Int,
    val dealer_id: Int,
    val brand_id: Int,

    val brand_name: String?,
    val model: String,
    val year: Int,
    val price: Double,

    val color: String,
    val fuel_type: String,
    val transmission: String,

    val mileage: Int,
    val engine_capacity: Double,

    val description: String?,
    val status: String?,

    val thumbnail_url: String?,
    val images: List<CarImage>?

)

data class CarImage(

    val image_id: Int,
    val url: String

)

data class Brand(

    val brand_id: Int,
    val brand_name: String

)

data class Dealer(

    val dealer_id: Int,
    val dealer_name: String

)
