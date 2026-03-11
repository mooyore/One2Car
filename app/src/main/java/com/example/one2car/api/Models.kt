package com.example.one2car.api

import com.google.gson.annotations.SerializedName

// --- User & Auth ---
data class UserInfo(
    @SerializedName("user_id") val userId: Int,
    val email: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    val role: String,
    val phone: String,
    @SerializedName("profile_image") val profileImage: String? = null,
    @SerializedName("profile_url") val profileUrl: String? = null
)

data class LoginResponse(
    val message: String,
    val user: UserInfo
)

// --- Dealer ---
data class DealerInfo(
    @SerializedName("dealer_id") val dealerId: Int,
    @SerializedName("user_id") val userId: Int = 0,
    @SerializedName("dealer_name") val dealerName: String,
    @SerializedName("address") val address: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("logo_image") val logoImage: String? = null,
    @SerializedName("logo_url") val logoUrl: String? = null
)

data class UpgradeResponse(
    val message: String,
    @SerializedName("dealer_id") val dealerId: Int,
    @SerializedName("logo_url") val logoUrl: String? = null
)

data class UpdateDealerResponse(
    val message: String,
    val updated: List<String>? = null
)

// --- Car & Search ---
data class BrandInfo(
    @SerializedName("brand_id") val brandId: Int,
    @SerializedName("brand_name") val brandName: String
)

data class CarModelInfo(
    @SerializedName("model_id") val modelId: Int? = null,
    @SerializedName("brand_id") val brandId: Int? = null,
    @SerializedName("model_name") val modelName: String
)

data class CarResponse(
    val car_id: Int,
    val dealer_id: Int = 0,
    val brand_id: Int = 0,
    val brand_name: String,
    val model: String,
    val year: Int,
    val price: Int,
    val mileage: Int,
    val transmission: String,
    val fuel_type: String,
    val status: String = "",
    val color: String = "",
    val engine_capacity: Int = 0,
    val description: String = "",
    val dealer_name: String? = null,
    val dealer_phone: String? = null,
    val thumbnail_url: String? = null
)

data class ReviewResponse(
    val rating: Int,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    val comment: String?
)

data class RatingSummary(
    @SerializedName("average_rating") val avgRating: String? = "0.0"
)

data class ProfileClass(
    val id: Int,
    val name: String,
    val email: String,
    val role: String
)

// --- Booking System ---
data class CreateBookingRequest(
    val user_id: Int,
    val car_id: Int,
    val booking_date: String = ""
)

data class CreateBookingResponse(
    val message: String,
    val booking_id: Int? = null
)

data class CancelBookingResponse(
    val message: String
)

data class AddBookingRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("car_id") val carId: Int,
    @SerializedName("booking_date") val bookingDate: String
)

data class BookingResponse(
    val message: String? = null,
    val error: Boolean = false
)

data class UserBooking(
    @SerializedName("booking_id") val bookingId: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("car_id") val carId: Int,
    @SerializedName("status") val status: String,
    @SerializedName("booking_date") val bookingDate: String,
    @SerializedName("model") val model: String? = null,
    @SerializedName("price") val price: Double? = null,
    @SerializedName("brand_name") val brandName: String? = null,
    @SerializedName("thumbnail_url") val thumbnailUrl: String? = null
)

data class DealerBooking(
    @SerializedName("booking_id") val bookingId: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("car_id") val carId: Int = 0,
    @SerializedName("status") val status: String,
    @SerializedName("booking_date") val bookingDate: String,
    @SerializedName("model") val model: String? = null,
    @SerializedName("brand_name") val brandName: String? = null,
    @SerializedName("price") val price: Double? = null,
    @SerializedName("first_name") val firstName: String? = null,
    @SerializedName("last_name") val lastName: String? = null,
    @SerializedName("customer_phone") val customerPhone: String? = null,
    @SerializedName("thumbnail_url") val thumbnailUrl: String? = null,
    @SerializedName("year") val year: Int? = null,
    @SerializedName("mileage") val mileage: Int? = null,
    @SerializedName("transmission") val transmission: String? = null,
    @SerializedName("fuel_type") val fuelType: String? = null,
    @SerializedName("color") val color: String? = null
)

data class BookingClass(
    val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("car_id") val carId: Int,
    @SerializedName("booking_date") val bookingDate: String
)