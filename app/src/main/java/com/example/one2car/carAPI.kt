package com.example.one2car

import com.example.one2car.api.*
import com.example.one2car.login.LoginClass
import com.example.one2car.login.RegisterClass
import com.example.one2car.login.RegisterResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface UserAPI {

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
    // JS : /my-bookings/:user_id
    @GET("my-bookings/{user_id}")
    suspend fun getMyBookings(
        @Path("user_id") userId: Int
    ): Response<List<UserBooking>>

    // ---------------- CANCEL BOOKING ----------------
    // JS : /user-cancel-booking/:booking_id
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
        @Part("dealer_name") dealerName: RequestBody,
        @Part("address") address: RequestBody,
        @Part("phone") phone: RequestBody,
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

    // เพิ่มใน interface UserAPI
    @POST("add-booking")
    suspend fun addBooking(
        @Body bookingData: Map<String, Any> // ส่ง user_id, car_id, booking_date
    ): Response<MessageResponse>
}