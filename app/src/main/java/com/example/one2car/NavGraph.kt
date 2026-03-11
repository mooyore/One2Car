package com.example.one2car

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

import com.example.one2car.Booking_me.MyBookingScreen
import com.example.one2car.Home.HomeScreenBuyer
import com.example.one2car.Home.HomeScreenSeller
import com.example.one2car.Home.EditProfileSellerScreen
import com.example.one2car.Home.ProfileSellerScreen
import com.example.one2car.Home.SearchCarScreen
import com.example.one2car.Home.SearchResultScreen
import com.example.one2car.Home.CarDetailScreen
import com.example.one2car.Home.DealerProfileScreen

import com.example.one2car.api.ProfileClass
import com.example.one2car.api.RetrofitClient
import com.example.one2car.api.UserInfo
import com.example.one2car.buyer.EditProfile_Buyer
import com.example.one2car.buyer.Profile_Buyer
import com.example.one2car.buyer.RegisterSellerScreen
import com.example.one2car.login.LoginScreen
import com.example.one2car.login.RegisterScreen

import com.example.one2car.repository.BookingRepository
import com.example.one2car.ui.booking.BookingViewModel
import com.example.one2car.ui.booking.BookingViewModelFactory
import com.example.one2car.ui.booking.DealerBookingScreen

import com.example.one2car.filenew.AddCarScreen
import com.example.one2car.filenew.EditCarScreen

@Composable
fun NavGraph(navController: NavHostController) {
    val userViewModel: UserViewModel = viewModel()
    val context = LocalContext.current
    val sharedPref = SharedPreferencesManager(context)

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        /* ---------- Login ---------- */
        composable(Screen.Login.route) {
            LoginScreen(navController, userViewModel)
        }

        /* ---------- Register ---------- */
        composable(Screen.Register.route) {
            RegisterScreen(navController, userViewModel)
        }

        /* ---------- Profile Buyer ---------- */
        composable(Screen.ProfileBuyer.route) {
            val profile = userViewModel.userProfile
            val userId = sharedPref.getSavedUserId().toIntOrNull()
                ?: userViewModel.userProfile?.id ?: 0
            var userInfo by remember { mutableStateOf<UserInfo?>(null) }

            LaunchedEffect(userId) {
                try {
                    userInfo = UserClient.userAPI.getUserInfo(userId)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            Profile_Buyer(
                currentScreen = Screen.ProfileBuyer.route,
                firstName = userInfo?.firstName ?: profile?.name?.split(" ")?.getOrNull(0) ?: "User",
                lastName = userInfo?.lastName ?: profile?.name?.split(" ")?.getOrNull(1) ?: "",
                email = userInfo?.email ?: profile?.email ?: "",
                phone = userInfo?.phone ?: "",
                isSeller = sharedPref.getSavedRole() == "seller" || sharedPref.getSavedRole() == "dealer",
                onEditClick = {
                    navController.navigate("edit_profile_buyer")
                },
                onRegisterSellerClick = {
                    navController.navigate("register_seller")
                },
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo("home_buyer") { inclusive = false }
                    }
                },
                onSwitchToSeller = {
                    navController.navigate("home_seller") {
                        popUpTo("home_buyer") { inclusive = true }
                    }
                },
                onLogout = {
                    sharedPref.logout(false)
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        /* ---------- Profile Seller ---------- */
        composable(Screen.ProfileSeller.route) {
            val userId = sharedPref.getSavedUserId().toIntOrNull()
                ?: userViewModel.userProfile?.id ?: 0
            ProfileSellerScreen(
                userId = userId,
                currentScreen = Screen.ProfileSeller.route,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo("home_seller") { inclusive = false }
                    }
                },
                onEditClick = { _ ->
                    navController.navigate("edit_profile_seller/$userId")
                },
                onLogout = {
                    sharedPref.logout(false)
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onSwitchToBuyer = {
                    navController.navigate("home_buyer") {
                        popUpTo("home_seller") { inclusive = true }
                    }
                }
            )
        }

        /* ---------- Home Buyer ---------- */
        composable("home_buyer") {
            HomeScreenBuyer(
                currentScreen = "home_buyer",
                onNavigate = { route ->
                    navController.navigate(route)
                }
            )
        }

        /* ---------- Home Seller ---------- */
        composable("home_seller") {
            HomeScreenSeller(
                currentScreen = "home_seller",
                onNavigate = { route ->
                    navController.navigate(route)
                },
                navController = navController
            )
        }

        /* ---------- Search Car ---------- */
        composable("search_car") {
            SearchCarScreen(
                onBack = { navController.popBackStack() },
                onSearch = { filter ->
                    val brandId = filter.brandId?.toString() ?: ""
                    val model = filter.model ?: ""
                    val trans = filter.transmission ?: ""
                    val fuel = filter.fuelType ?: ""
                    val minP = filter.minPrice ?: ""
                    val maxP = filter.maxPrice ?: ""

                    val route = "search_result?brandId=$brandId&model=$model&trans=$trans&fuel=$fuel&minP=$minP&maxP=$maxP"
                    navController.navigate(route)
                }
            )
        }

        /* ---------- My Booking (Buyer) ---------- */
        composable("my_booking") {
            val savedUserId = sharedPref.getSavedUserId().toIntOrNull() ?: 0
            val userId = if (savedUserId != 0) savedUserId
            else userViewModel.userProfile?.id ?: 0

            val bookingViewModel: BookingViewModel = viewModel(
                factory = BookingViewModelFactory(
                    BookingRepository(RetrofitClient.bookingApiService)
                )
            )
            MyBookingScreen(
                viewModel = bookingViewModel,
                userId = userId,
                onBack = { navController.popBackStack() }
            )
        }

        /* ---------- Register Seller ---------- */
        composable("register_seller") {
            val savedId = sharedPref.getSavedUserId().toIntOrNull() ?: 0
            RegisterSellerScreen(
                userId = savedId,
                onSuccess = { dealerId ->
                    sharedPref.saveLoginStatus(
                        isLoggedIn = true,
                        userId = sharedPref.getSavedUserId(),
                        role = "seller",
                        email = sharedPref.getSavedEmail(),
                        dealerId = dealerId.toString()
                    )
                    navController.navigate("home_seller") {
                        popUpTo("home_buyer") { inclusive = true }
                    }
                },
                onCancel = {
                    navController.popBackStack()
                }
            )
        }

        /* ---------- Order Seller (รายการจองสำหรับคนขาย) ---------- */
        composable("order_seller") {
            val userId = sharedPref.getSavedUserId().toIntOrNull() ?: 0
            var dealerId by remember { mutableStateOf(
                sharedPref.getSavedDealerId().toIntOrNull() ?: 0
            ) }
            var isLoading by remember { mutableStateOf(true) }

            // ถ้า dealerId ยังเป็น 0 → ดึง dealer_id จริงจาก API โดยใช้ userId
            LaunchedEffect(userId) {
                if (dealerId == 0 || dealerId == userId) {
                    try {
                        val info = RetrofitClient.instance.getDealerInfo(userId)
                        dealerId = info.dealerId
                        // บันทึก dealerId ลง SharedPreferences เพื่อใช้ครั้งต่อไป
                        sharedPref.saveLoginStatus(
                            isLoggedIn = true,
                            userId = userId.toString(),
                            role = sharedPref.getSavedRole(),
                            email = sharedPref.getSavedEmail(),
                            dealerId = dealerId.toString()
                        )
                    } catch (e: Exception) {
                        android.util.Log.e("ORDER_SELLER", "Failed to get dealerId: ${e.message}")
                    }
                }
                isLoading = false
            }

            val bookingViewModel: BookingViewModel = viewModel(
                factory = BookingViewModelFactory(
                    BookingRepository(RetrofitClient.bookingApiService)
                )
            )

            if (isLoading) {
                androidx.compose.foundation.layout.Box(
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator()
                }
            } else {
                DealerBookingScreen(
                    viewModel = bookingViewModel,
                    dealerId = dealerId,
                    onBack = {
                        navController.navigate("home_seller") {
                            popUpTo("home_seller") { inclusive = true }
                        }
                    }
                )
            }
        }

        /* ---------- Edit Profile Seller ---------- */
        composable(
            route = "edit_profile_seller/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val editUserId = backStackEntry.arguments?.getInt("userId") ?: 0
            EditProfileSellerScreen(
                userId = editUserId,
                onBack = { navController.popBackStack() }
            )
        }

        /* ---------- Edit Profile Buyer ---------- */
        composable("edit_profile_buyer") {
            val profile = userViewModel.userProfile
            val userId = sharedPref.getSavedUserId().toIntOrNull() ?: 0
            var userInfo by remember { mutableStateOf<UserInfo?>(null) }
            var isLoading by remember { mutableStateOf(true) }
            val scope = rememberCoroutineScope()

            LaunchedEffect(userId) {
                try {
                    userInfo = UserClient.userAPI.getUserInfo(userId)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isLoading = false
                }
            }

            if (isLoading) {
                androidx.compose.foundation.layout.Box(
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator()
                }
            } else {
                EditProfile_Buyer(
                    initialFirstName = userInfo?.firstName ?: profile?.name?.split(" ")?.getOrNull(0) ?: "",
                    initialLastName = userInfo?.lastName ?: profile?.name?.split(" ")?.getOrNull(1) ?: "",
                    initialEmail = userInfo?.email ?: profile?.email ?: sharedPref.getSavedEmail(),
                    initialPhone = userInfo?.phone ?: "",
                    onUpdate = { firstName, lastName, email, phone ->
                        scope.launch {
                            try {
                                val updateData = mapOf(
                                    "first_name" to firstName,
                                    "last_name" to lastName,
                                    "email" to email,
                                    "phone" to phone
                                )
                                UserClient.userAPI.updateUser(userId, updateData)

                                userViewModel.updateUserProfile(ProfileClass(
                                    id = profile?.id ?: userId,
                                    name = "$firstName $lastName",
                                    email = email,
                                    role = profile?.role ?: sharedPref.getSavedRole()
                                ))
                            } catch (e: Exception) {
                                android.util.Log.e("EDIT_PROFILE", "Update failed: ${e.message}", e)
                            }
                            navController.popBackStack()
                        }
                    },
                    onCancel = { navController.popBackStack() }
                )
            }
        }

        /* ---------- Search Result ---------- */
        composable(
            route = "search_result?brandId={brandId}&model={model}&trans={trans}&fuel={fuel}&minP={minP}&maxP={maxP}",
            arguments = listOf(
                navArgument("brandId") { nullable = true; defaultValue = "" },
                navArgument("model")   { nullable = true; defaultValue = "" },
                navArgument("trans")   { nullable = true; defaultValue = "" },
                navArgument("fuel")    { nullable = true; defaultValue = "" },
                navArgument("minP")    { nullable = true; defaultValue = "" },
                navArgument("maxP")    { nullable = true; defaultValue = "" }
            )
        ) { backStackEntry ->
            val brandId = backStackEntry.arguments?.getString("brandId") ?: ""
            val model = backStackEntry.arguments?.getString("model") ?: ""
            val trans = backStackEntry.arguments?.getString("trans") ?: ""
            val fuel = backStackEntry.arguments?.getString("fuel") ?: ""
            val minP = backStackEntry.arguments?.getString("minP") ?: ""
            val maxP = backStackEntry.arguments?.getString("maxP") ?: ""

            val params = mutableMapOf<String, String>()
            if (brandId.isNotBlank()) params["brandId"] = brandId
            if (model.isNotBlank()) params["model"] = model
            if (trans.isNotBlank()) params["trans"] = trans
            if (fuel.isNotBlank()) params["fuel"] = fuel
            if (minP.isNotBlank()) params["minP"] = minP
            if (maxP.isNotBlank()) params["maxP"] = maxP

            val userId = sharedPref.getSavedUserId().toIntOrNull() ?: 0

            SearchResultScreen(
                searchParams = params,
                userId = userId,
                onBack = { navController.popBackStack() },
                onCarDetailClick = { carId -> navController.navigate("car_detail/$carId") },
                onGoToMyBooking = {
                    navController.navigate("my_booking") {
                        popUpTo("home_buyer")
                    }
                }
            )
        }

        /* ---------- Car Detail ---------- */
        composable(
            route = "car_detail/{carId}",
            arguments = listOf(navArgument("carId") { type = NavType.IntType })
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getInt("carId") ?: 0
            CarDetailScreen(
                carId = carId,
                onBack = { navController.popBackStack() },
                onBookClick = { navController.navigate("my_booking") },
                onDealerProfileClick = { dealerId ->
                    navController.navigate("dealer_profile/$dealerId")
                }
            )
        }

        /* ---------- Dealer Profile ---------- */
        composable(
            route = "dealer_profile/{dealerId}",
            arguments = listOf(navArgument("dealerId") { type = NavType.IntType })
        ) { backStackEntry ->
            val dealerId = backStackEntry.arguments?.getInt("dealerId") ?: 0
            DealerProfileScreen(
                dealerId = dealerId,
                onBack = { navController.popBackStack() },
                onCarClick = { clickedCarId ->
                    navController.navigate("car_detail/$clickedCarId")
                }
            )
        }

        /* ---------- Add Car (Seller) ---------- */
        composable("addcar") {
            AddCarScreen(navController = navController)
        }

        /* ---------- Edit Car (Seller) ---------- */
        composable(
            route = "edit_car/{carId}",
            arguments = listOf(navArgument("carId") { type = NavType.IntType })
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getInt("carId") ?: 0
            EditCarScreen(navController = navController, carId = carId)
        }

        /* ---------- Admin Main ---------- */
        composable(Screen.AdminMain.route) {
            val adminUserId = sharedPref.getSavedUserId().toIntOrNull() ?: 0
            com.example.one2car.api.AdminMainScreen(
                userId = adminUserId,
                onLogout = {
                    sharedPref.logout(false)
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
