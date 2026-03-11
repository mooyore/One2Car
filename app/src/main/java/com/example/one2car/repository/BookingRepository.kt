package com.example.one2car.repository

import com.example.one2car.api.BookingApiService
import com.example.one2car.api.AddBookingRequest
import com.example.one2car.api.BookingResponse
import com.example.one2car.api.DealerBooking
import com.example.one2car.api.UserBooking
import retrofit2.Response

class BookingRepository(
    private val apiService: BookingApiService
) {

    suspend fun addBooking(
        userId: Int,
        carId: Int,
        bookingDate: String
    ): Response<BookingResponse> {
        val request = AddBookingRequest(
            userId = userId,
            carId = carId,
            bookingDate = bookingDate
        )
        return apiService.addBooking(request)
    }

    suspend fun cancelBooking(
        bookingId: Int
    ): Response<BookingResponse> {
        return apiService.cancelBooking(bookingId)
    }

    suspend fun completeBooking(
        bookingId: Int
    ): Response<BookingResponse> {
        return apiService.completeBooking(bookingId)
    }

    suspend fun getMyBookings(
        userId: Int
    ): Response<List<UserBooking>> {
        return apiService.getMyBookings(userId)
    }

    suspend fun getDealerBookings(
        dealerId: Int
    ): Response<List<DealerBooking>> {
        return apiService.getDealerBookings(dealerId)
    }
}
