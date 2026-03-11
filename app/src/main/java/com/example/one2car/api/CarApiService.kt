package com.example.one2car.api

import com.example.one2car.login.LoginClass
import com.example.one2car.login.RegisterClass
import com.example.one2car.login.RegisterResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ---------------- REGISTER ----------------
    @POST("register")
    suspend fun registerUser(
        @Body userData: RegisterClass
    ): Response<RegisterResponse>

    // ---------------- LOGIN ----------------
    @POST("login")
    suspend fun loginUser(
        @Body loginData: Map<String, String>
    ): Response<LoginClass>

    // ---------------- PROFILE ----------------
    @GET("search/{id}")
    suspend fun getUserProfile(
        @Path("id") id: String
    ): Response<ProfileClass>

    // ---------------- USER BOOKING HISTORY ----------------
    @GET("my-bookings/{user_id}")
    suspend fun getMyBookings(
        @Path("user_id") userId: Int
    ): Response<List<UserBooking>>

    // ---------------- CANCEL BOOKING ----------------
    @PUT("user-cancel-booking/{booking_id}")
    suspend fun cancelBooking(
        @Path("booking_id") bookingId: Int
    ): Response<MessageResponse>

    // ---------------- BRANDS ----------------
    @GET("brands")
    suspend fun getBrands(): List<BrandInfo>

    // ---------------- MODELS ----------------
    @GET("models/{brandId}")
    suspend fun getModels(
        @Path("brandId") brandId: Int
    ): List<CarModelInfo>

    // ---------------- DEALER INFO ----------------
    @GET("dealer-info/{userId}")
    suspend fun getDealerInfo(
        @Path("userId") userId: Int
    ): DealerInfo

    // ---------------- USER INFO ----------------
    @GET("user-info/{userId}")
    suspend fun getUserInfo(
        @Path("userId") userId: Int
    ): UserInfo

    // ---------------- UPDATE USER ----------------
    @PUT("update-user/{userId}")
    suspend fun updateUser(
        @Path("userId") userId: Int,
        @Body userData: Map<String, String>
    ): Response<MessageResponse>

    // ---------------- UPDATE DEALER ----------------
    @Multipart
    @PUT("update-dealer/{dealerId}")
    suspend fun updateDealer(
        @Path("dealerId") dealerId: Int,
        @Part("dealer_name") dealerName: RequestBody?,
        @Part("address") address: RequestBody?,
        @Part("phone") phone: RequestBody?,
        @Part logoImage: MultipartBody.Part?
    ): Response<UpdateDealerResponse>

    // ---------------- UPGRADE DEALER ----------------
    @Multipart
    @POST("upgrade-to-dealer")
    suspend fun upgradeToDealer(
        @Part("user_id") userId: RequestBody,
        @Part("dealer_name") dealerName: RequestBody,
        @Part("address") address: RequestBody,
        @Part("phone") phone: RequestBody,
        @Part logoImage: MultipartBody.Part?
    ): UpgradeResponse

    // ---------------- BOOKING ----------------
    @POST("add-booking")
    suspend fun addBooking(
        @Body bookingData: Map<String, Any>
    ): Response<MessageResponse>

    @POST("add-booking")
    suspend fun createBooking(
        @Body request: CreateBookingRequest
    ): CreateBookingResponse

    // ---------------- CAR DETAIL ----------------
    @GET("car-detail/{car_id}")
    suspend fun getCarDetail(
        @Path("car_id") carId: Int
    ): CarResponse

    // ---------------- DEALER PROFILE & CARS ----------------
    @GET("dealer-cars/{dealer_id}")
    suspend fun getMyCars(
        @Path("dealer_id") dealerId: Int
    ): List<CarResponse>

    @GET("dealer-reviews/{dealer_id}")
    suspend fun getDealerReviews(
        @Path("dealer_id") dealerId: Int
    ): List<ReviewResponse>

    @GET("dealer-rating-summary/{dealer_id}")
    suspend fun getDealerRatingSummary(
        @Path("dealer_id") dealerId: Int
    ): RatingSummary

    // ---------------- SEARCH CARS ----------------
    @GET("search-cars")
    suspend fun searchCars(
        @Query("brand_name") brandName: String?,
        @Query("model") model: String?,
        @Query("year") year: Int?,
        @Query("transmission") transmission: String?,
        @Query("fuel_type") fuelType: String?,
        @Query("minPrice") minPrice: Int?,
        @Query("maxPrice") maxPrice: Int?,
        @Query("status") status: String?
    ): List<CarResponse>
}
