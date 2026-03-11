package com.example.one2car.Home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenBuyer(
    currentScreen: String,
    onNavigate: (String) -> Unit
) {

    var searchQuery by remember { mutableStateOf("") }

    Scaffold(

        bottomBar = {

            BottomNavigationBarBuyer(
                currentScreen = currentScreen,
                onNavigate = onNavigate
            )

        }

    ) { paddingValues ->

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())

        ) {

            // -------------------
            // Search Bar (แก้ไขให้คลิกได้)
            // -------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onNavigate("search_car")
                    }
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search cars, brands, etc.") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    enabled = false, // เปลี่ยนจาก readOnly เป็น false เพื่อให้คลิกที่ Box ได้ง่ายขึ้น
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = Color.LightGray,
                        disabledPlaceholderColor = Color.Gray,
                        disabledLeadingIconColor = Color.Gray,
                        disabledContainerColor = Color.Transparent
                    )
                )
            }

            // -------------------
            // Header
            // -------------------
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {

                Text(
                    text = "Find Your Dream Car",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Search from 500+ used cars",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // -------------------
            // Brand Section
            // -------------------
            BrandCategoriesSection()

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Recommended for You",
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // -------------------
            // Car List
            // -------------------
            CarListHorizontal()

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrandCategoriesSection() {
    val brands = listOf("Toyota", "Honda", "Mazda", "BMW", "Benz", "Ford")
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(text = "Brands", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(brands) { brand ->
                FilterChip(
                    selected = false,
                    onClick = { },
                    label = { Text(brand) }
                )
            }
        }
    }
}

@Composable
fun CarListHorizontal() {
    val cars = listOf("Car A", "Car B", "Car C", "Car D")
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(cars) { car ->
            Card(
                modifier = Modifier.width(200.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(Color.LightGray)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = car, fontWeight = FontWeight.Bold)
                    Text(text = "฿ 500,000", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}