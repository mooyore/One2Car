package com.example.one2car

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("one2car_prefs", Context.MODE_PRIVATE)

    fun saveLoginStatus(
        isLoggedIn: Boolean,
        userId: String,
        role: String,
        email: String,
        dealerId: String = "0"
    ) {
        val editor = prefs.edit()
        editor.putBoolean("isLoggedIn", isLoggedIn)
        editor.putString("userId", userId)
        editor.putString("role", role)
        editor.putString("email", email)
        editor.putString("dealerId", dealerId)
        editor.apply()
    }

    fun getSavedUserId(): String = prefs.getString("userId", "0") ?: "0"
    fun getSavedRole(): String = prefs.getString("role", "") ?: ""
    fun getSavedEmail(): String = prefs.getString("email", "") ?: ""
    fun getSavedDealerId(): String = prefs.getString("dealerId", "0") ?: "0"

    fun isLoggedIn(): Boolean = prefs.getBoolean("isLoggedIn", false)

    fun logout(clearAll: Boolean = true) {
        if (clearAll) {
            prefs.edit().clear().apply()
        } else {
            prefs.edit().putBoolean("isLoggedIn", false).apply()
        }
    }
}