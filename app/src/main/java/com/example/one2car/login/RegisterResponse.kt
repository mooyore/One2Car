package com.example.one2car.login

import com.google.gson.annotations.SerializedName

data class RegisterResponse(
    @SerializedName(value = "error") val error: Boolean,
    @SerializedName(value = "message") val message: String
)
