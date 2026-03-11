package com.example.one2car

sealed class Screen(val route: String) {

    object Login : Screen("login")

    object Register : Screen("register")

    object Profile : Screen("profile")

    object ProfileBuyer : Screen("profile_buyer")

    object ProfileSeller : Screen("profile_seller")

    object AdminMain : Screen("admin_main")

}