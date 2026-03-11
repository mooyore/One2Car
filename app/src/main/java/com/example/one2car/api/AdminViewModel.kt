package com.example.one2car.api

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class AdminViewModel : ViewModel() {

    private val api = RetrofitClient.adminApiService

    private val _dashboardStats = MutableStateFlow<DashboardStats?>(null)
    val dashboardStats: StateFlow<DashboardStats?> = _dashboardStats

    private val _users = MutableStateFlow<List<AdminUser>>(emptyList())
    val users: StateFlow<List<AdminUser>> = _users

    private val _cars = MutableStateFlow<List<AdminCar>>(emptyList())
    val cars: StateFlow<List<AdminCar>> = _cars

    private val _adminProfile = MutableStateFlow<AdminUser?>(null)
    val adminProfile: StateFlow<AdminUser?> = _adminProfile

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun clearMessage() {
        _message.value = null
    }

    fun clearError() {
        _error.value = null
    }

    fun fetchDashboardStats() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _dashboardStats.value = api.getDashboardStats()
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchUsers() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _users.value = api.getUsers()
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchCars() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _cars.value = api.getAllCars()
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchAdminProfile(adminId: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _adminProfile.value = api.getAdminProfile(adminId)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun changeUserRole(userId: Int, newRole: String) {
        viewModelScope.launch {
            try {
                api.changeUserRole(userId, ChangeRoleRequest(newRole))
                _message.value = "เปลี่ยนบทบาทผู้ใช้เรียบร้อยแล้ว"
                fetchUsers()
                fetchDashboardStats()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun deleteUser(userId: Int) {
        viewModelScope.launch {
            try {
                api.deleteUser(userId)
                _message.value = "ลบผู้ใช้เรียบร้อยแล้ว"
                fetchUsers()
                fetchDashboardStats()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun deleteCar(carId: Int) {
        viewModelScope.launch {
            try {
                api.deleteCar(carId)
                _message.value = "ลบรายการรถเรียบร้อยแล้ว"
                fetchCars()
                fetchDashboardStats()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun addBrand(context: Context, brandName: String, imageUri: Uri?, countryOrigin: String?) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val namePart = brandName.toRequestBody("text/plain".toMediaTypeOrNull())
                val countryPart = countryOrigin?.toRequestBody("text/plain".toMediaTypeOrNull())

                var imagePart: MultipartBody.Part? = null

                imageUri?.let { uri ->
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val file =
                        File(context.cacheDir, "temp_brand_logo_${System.currentTimeMillis()}.jpg")
                    val outputStream = FileOutputStream(file)
                    inputStream?.copyTo(outputStream)
                    inputStream?.close()
                    outputStream.close()

                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    imagePart =
                        MultipartBody.Part.createFormData("brand_logo", file.name, requestFile)
                }

                val response = api.addBrand(namePart, imagePart, countryPart)
                _message.value = response.message
            } catch (e: Exception) {
                _error.value = "Upload failed: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
