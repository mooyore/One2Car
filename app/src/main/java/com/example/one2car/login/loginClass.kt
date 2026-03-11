package com.example.one2car.login

import com.example.one2car.api.UserInfo
import com.google.gson.annotations.SerializedName

data class LoginClass(

    @SerializedName("message")
    val message: String?,

    @SerializedName("user")
    val user: UserInfo? = null,

    @SerializedName("error")
    val error: Boolean = false
)