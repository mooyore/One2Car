package com.example.one2car.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface BookingApiService {

    @POST("add-booking")
    suspend fun addBooking(
        @Body request: AddBookingRequest
    ): Response<BookingResponse>

    @PUT("user-cancel-booking/{booking_id}")
    suspend fun cancelBooking(
        @Path("booking_id") bookingId: Int
    ): Response<BookingResponse>

    @PUT("dealer-complete-booking/{booking_id}")
    suspend fun completeBooking(
        @Path("booking_id") bookingId: Int
    ): Response<BookingResponse>

    // แก้ไขให้ตรงกับ Backend: /my-bookings/:user_id
    @GET("my-bookings/{user_id}")
    suspend fun getMyBookings(
        @Path("user_id") userId: Int
    ): Response<List<UserBooking>>

    // แก้ไขให้ตรงกับ Backend: /dealer-bookings/:dealer_id
    @GET("dealer-bookings/{dealer_id}")
    suspend fun getDealerBookings(
        @Path("dealer_id") dealerId: Int
    ): Response<List<DealerBooking>>
}