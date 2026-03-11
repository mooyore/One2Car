package com.example.one2car.ui.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.one2car.repository.BookingRepository

class BookingViewModelFactory(
    private val repository: BookingRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookingViewModel::class.java)) {
            return BookingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}