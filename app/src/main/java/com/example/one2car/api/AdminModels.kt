package com.example.one2car.api

import com.google.gson.annotations.SerializedName

data class DashboardStats(
    @SerializedName("total_users") val totalUsers: Int,
    @SerializedName("total_dealers") val totalDealers: Int,
    @SerializedName("total_cars") val totalCars: Int,
    @SerializedName("total_sold") val totalSold: Int
)

data class AdminUser(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    val email: String,
    val role: String,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("profile_image") val profileImage: String? = null,
    @SerializedName("national_id") val nationalId: String? = null
)

data class Dealership(
    @SerializedName("dealer_id") val dealerId: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("dealer_name") val dealerName: String,
    val address: String?,
    @SerializedName("dealer_phone") val dealerPhone: String?,
    @SerializedName("dealer_email") val dealerEmail: String?
)

data class AdminBooking(
    @SerializedName("booking_id") val bookingId: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("car_id") val carId: Int,
    @SerializedName("booking_date") val bookingDate: String,
    val status: String,
    @SerializedName("first_name") val firstName: String,
    val model: String,
    @SerializedName("dealer_name") val dealerName: String
)

data class AdminCar(
    @SerializedName("car_id") val carId: Int,
    @SerializedName("brand_name") val brand: String,
    @SerializedName("model") val model: String,
    @SerializedName("year") val year: Int,
    @SerializedName("price") val price: Double,
    @SerializedName("status") val status: String,
    @SerializedName("dealer_id") val dealerId: Int,
    @SerializedName("dealer_name") val dealerName: String,
    @SerializedName("contact_email") val contactEmail: String
)

data class AddBrandRequest(
    @SerializedName("brand_name") val brandName: String,
    @SerializedName("brand_logo") val brandLogo: String? = null,
    @SerializedName("country_origin") val countryOrigin: String? = null
)

data class ChangeRoleRequest(
    @SerializedName("new_role") val newRole: String
)