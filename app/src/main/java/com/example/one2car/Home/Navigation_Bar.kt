package com.example.one2car.Home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.one2car.Screen


// ---------------------------
// Bottom Navigation Buyer
// ---------------------------
@Composable
fun BottomNavigationBarBuyer(
    currentScreen: String,
    onNavigate: (String) -> Unit
) {

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {

        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = currentScreen == "home_buyer",
            onClick = { onNavigate("home_buyer") }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.DateRange, contentDescription = "My Booking") },
            label = { Text("My Booking") },
            selected = currentScreen == "my_booking",
            onClick = { onNavigate("my_booking") }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") },
            selected = currentScreen == Screen.ProfileBuyer.route,
            onClick = { onNavigate(Screen.ProfileBuyer.route) }
        )
    }
}



// ---------------------------
// Bottom Navigation Seller
// ---------------------------
@Composable
fun BottomNavigationBarSeller(
    currentScreen: String,
    onNavigate: (String) -> Unit
) {

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {

        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = currentScreen == "home_seller",
            onClick = { onNavigate("home_seller") }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.List, contentDescription = "Order") },
            label = { Text("Order") },
            selected = currentScreen == "order_seller",
            onClick = { onNavigate("order_seller") }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") },
            selected = currentScreen == Screen.ProfileSeller.route,
            onClick = { onNavigate(Screen.ProfileSeller.route) }
        )
    }
}
