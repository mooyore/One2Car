package com.example.one2car

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.one2car.api.*
import com.example.one2car.login.LoginClass
import com.example.one2car.login.RegisterClass
import com.example.one2car.login.RegisterResponse
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {

    var userProfile by mutableStateOf<ProfileClass?>(null)
        private set

    // ---------------- LOGIN RESULT ----------------
    private val _loginResult = MutableStateFlow<LoginClass?>(null)
    val loginResult: StateFlow<LoginClass?> = _loginResult.asStateFlow()

    // ---------------- PROFILE ----------------
    private val _profile = MutableStateFlow<ProfileClass?>(null)
    val profile: StateFlow<ProfileClass?> = _profile.asStateFlow()

    // --- BOOKING HISTORY (USER) ---
    private val _userBookings = MutableStateFlow<List<UserBooking>>(emptyList())
    val userBookings: StateFlow<List<UserBooking>> = _userBookings.asStateFlow()

    // ---------------- ERROR MESSAGE ----------------
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // ---------------- LOADING ----------------
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // reset login state
    fun resetLoginResult() {
        _loginResult.value = null
    }

    fun clearMessage() {
        _errorMessage.value = null
    }

    fun updateUserProfile(profile: ProfileClass) {
        userProfile = profile
        _profile.value = profile
    }

    // ---------------- LOGIN ----------------
    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val loginData = mapOf("email" to email, "password" to password)
                Log.d("LOGIN_DEBUG", "Login Data: $loginData")
                val response = UserClient.userAPI.loginUser(loginData)

                if (response.isSuccessful && response.body() != null) {
                    _loginResult.value = response.body()
                    val user = _loginResult.value?.user
                    userProfile = user?.let {
                        ProfileClass(
                            id = it.userId,
                            name = "${it.firstName} ${it.lastName}",
                            email = it.email,
                            role = it.role
                        )
                    }
                    Log.d("LOGIN_DEBUG", "Login Success, userId=${user?.userId}, role=${user?.role}")
                } else {
                    _errorMessage.value = "Email or Password incorrect"
                    _loginResult.value = LoginClass(
                        error = true,
                        message = "Email or Password incorrect"
                    )
                    Log.e("LOGIN_DEBUG", "Login Failed")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network Error: ${e.message}"
                _loginResult.value = LoginClass(
                    error = true,
                    message = _errorMessage.value
                )
                Log.e("LOGIN_DEBUG", "Network Error", e)
            }
        }
    }

    // ---------------- GET PROFILE ----------------
    fun getProfile(id: String) {
        viewModelScope.launch {
            try {
                val response = UserClient.userAPI.getUserProfile(id)
                if (response.isSuccessful && response.body() != null) {
                    _profile.value = response.body()
                    userProfile = _profile.value
                } else {
                    _errorMessage.value = "Profile not found"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            }
        }
    }

    // --- FETCH USER BOOKINGS ---
    fun fetchUserBookings(userId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d("BOOKING_DEBUG", "Fetching bookings for User ID: $userId")
                val response = UserClient.userAPI.getMyBookings(userId)
                if (response.isSuccessful) {
                    _userBookings.value = response.body() ?: emptyList()
                    if (_userBookings.value.isEmpty()){
                        Log.d("BOOKING_DEBUG", "Booking list is empty.")
                    }
                } else {
                    _errorMessage.value = "Error fetching bookings: ${response.message()}"
                    Log.e("BOOKING_DEBUG", "Failed to fetch bookings: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
                Log.e("BOOKING_DEBUG", "Exception fetching bookings", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ---------------- REGISTER ----------------
    fun register(context: Context, user: RegisterClass, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = UserClient.userAPI.registerUser(user)
                if (response.isSuccessful) {
                    Toast.makeText(context, "Register Successful", Toast.LENGTH_SHORT).show()
                    onSuccess()
                } else {
                    val errorRaw = response.errorBody()?.string()
                    val finalMessage = if (!errorRaw.isNullOrEmpty()) {
                        try {
                            Gson().fromJson(errorRaw, RegisterResponse::class.java).message
                        } catch (e: Exception) {
                            errorRaw
                        }
                    } else {
                        response.message()
                    }
                    _errorMessage.value = finalMessage ?: "Unknown Error"
                    Toast.makeText(context, _errorMessage.value, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Network Error: ${e.message}"
                Toast.makeText(context, _errorMessage.value, Toast.LENGTH_SHORT).show()
            }
        }
    }
}