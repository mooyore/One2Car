package com.example.one2car.login
import com.google.gson.annotations.SerializedName

data class RegisterClass(

    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("first_name")
    val firstName: String,

    @SerializedName("last_name")
    val lastName: String,

    @SerializedName("phone")
    val phone: String,

    @SerializedName("national_id")
    val nationalId: String,

    @SerializedName("role")
    val role: String = "user"
)
