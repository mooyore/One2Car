package com.example.one2car.ui.booking


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.one2car.api.DealerBooking
import com.example.one2car.api.UserBooking
import com.example.one2car.repository.BookingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BookingViewModel(private val repository: BookingRepository) : ViewModel() {

    private val _userBookings = MutableStateFlow<List<UserBooking>>(emptyList())
    val userBookings: StateFlow<List<UserBooking>> = _userBookings.asStateFlow()

    private val _dealerBookings = MutableStateFlow<List<DealerBooking>>(emptyList())
    val dealerBookings: StateFlow<List<DealerBooking>> = _dealerBookings.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    /* ---------------- USER BOOKINGS ---------------- */

    fun fetchUserBookings(userId: Int) {

        viewModelScope.launch {

            _loading.value = true

            try {

                Log.d("BOOKING_DEBUG", "Fetching user bookings for userId = $userId")

                val response = repository.getMyBookings(userId)

                Log.d("BOOKING_DEBUG", "User Booking Response code = ${response.code()}")
                Log.d("BOOKING_DEBUG", "User Booking Response body = ${response.body()}")

                if (response.isSuccessful) {

                    _userBookings.value = response.body() ?: emptyList()

                    Log.d(
                        "BOOKING_DEBUG",
                        "User Bookings Size = ${_userBookings.value.size}"
                    )

                } else {

                    val errorBody = response.errorBody()?.string()
                    _message.value = "Error: ${response.code()} ${response.message()}"
                    Log.e("BOOKING_DEBUG", "User Booking API Error code=${response.code()} msg=${response.message()} body=$errorBody")

                }

            } catch (e: Exception) {

                _message.value = e.message
                Log.e("BOOKING_DEBUG", "User Booking Exception = ${e.message}", e)

            }

            _loading.value = false

        }

    }

    /* ---------------- DEALER BOOKINGS ---------------- */

    fun fetchDealerBookings(dealerId: Int) {

        viewModelScope.launch {

            _loading.value = true

            try {

                Log.d("BOOKING_DEBUG", "Fetching dealer bookings dealerId = $dealerId")

                val response = repository.getDealerBookings(dealerId)

                Log.d("BOOKING_DEBUG", "Dealer Booking Response = ${response.body()}")

                if (response.isSuccessful) {

                    _dealerBookings.value = response.body() ?: emptyList()

                    Log.d(
                        "BOOKING_DEBUG",
                        "Dealer Bookings Size = ${_dealerBookings.value.size}"
                    )

                } else {

                    _message.value = "Error: ${response.message()}"
                    Log.d("BOOKING_DEBUG", "Dealer Booking API Error = ${response.message()}")

                }

            } catch (e: Exception) {

                _message.value = e.message
                Log.d("BOOKING_DEBUG", "Dealer Booking Exception = ${e.message}")

            }

            _loading.value = false

        }

    }

    /* ---------------- CANCEL BOOKING ---------------- */

    fun cancelBooking(
        bookingId: Int,
        userId: Int? = null,
        dealerId: Int? = null
    ) {

        viewModelScope.launch {

            try {

                Log.d("BOOKING_DEBUG", "Cancel booking id = $bookingId")

                val response = repository.cancelBooking(bookingId)

                if (response.isSuccessful) {

                    _message.value = "Booking cancelled successfully"

                    userId?.let { fetchUserBookings(it) }
                    dealerId?.let { fetchDealerBookings(it) }

                } else {

                    _message.value = "Failed to cancel: ${response.message()}"
                    Log.d("BOOKING_DEBUG", "Cancel API Error = ${response.message()}")

                }

            } catch (e: Exception) {

                _message.value = e.message
                Log.d("BOOKING_DEBUG", "Cancel Exception = ${e.message}")

            }

        }

    }

    /* ---------------- COMPLETE BOOKING ---------------- */

    fun completeBooking(
        bookingId: Int,
        dealerId: Int
    ) {

        viewModelScope.launch {

            try {

                Log.d("BOOKING_DEBUG", "Complete booking id = $bookingId")

                val response = repository.completeBooking(bookingId)

                if (response.isSuccessful) {

                    _message.value = "Booking completed successfully"

                    fetchDealerBookings(dealerId)

                } else {

                    _message.value = "Failed to complete: ${response.message()}"
                    Log.d("BOOKING_DEBUG", "Complete API Error = ${response.message()}")

                }

            } catch (e: Exception) {

                _message.value = e.message
                Log.d("BOOKING_DEBUG", "Complete Exception = ${e.message}")

            }

        }

    }

    /* ---------------- CLEAR MESSAGE ---------------- */

    fun clearMessage() {
        _message.value = null
    }
}