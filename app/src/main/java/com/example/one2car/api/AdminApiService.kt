package com.example.one2car.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface AdminApiService {

    @GET("admin/dashboard")
    suspend fun getDashboardStats(): DashboardStats

    @GET("admin/users")
    suspend fun getUsers(): List<AdminUser>

    @PUT("admin/change-role/{user_id}")
    suspend fun changeUserRole(
        @Path("user_id") userId: Int,
        @Body request: ChangeRoleRequest
    ): MessageResponse

    @DELETE("admin/delete-user/{user_id}")
    suspend fun deleteUser(@Path("user_id") userId: Int): MessageResponse

    @GET("admin/all-cars")
    suspend fun getAllCars(): List<AdminCar>

    @DELETE("admin/delete-car/{car_id}")
    suspend fun deleteCar(@Path("car_id") carId: Int): MessageResponse

    @GET("admin/profile/{user_id}")
    suspend fun getAdminProfile(@Path("user_id") userId: Int): AdminUser

    @GET("admin/bookings")
    suspend fun getBookings(): List<AdminBooking>

    @Multipart
    @POST("admin/add-brand")
    suspend fun addBrand(
        @Part("brand_name") name: RequestBody,
        @Part brand_logo: MultipartBody.Part?,
        @Part("country_origin") country: RequestBody?
    ): MessageResponse
}
