package com.example.one2car.Home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter

import com.example.one2car.SharedPreferencesManager
import com.example.one2car.filenew.ApiClient
import com.example.one2car.filenew.Car
import com.example.one2car.filenew.CarViewModel
import com.example.one2car.filenew.Dealer

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenSeller(
    currentScreen: String,
    onNavigate: (String) -> Unit,
    navController: NavController
) {
    val context = LocalContext.current
    val sharedPref = SharedPreferencesManager(context)
    val viewModel: CarViewModel = viewModel()
    val lifecycleOwner = LocalLifecycleOwner.current

    // ดึง dealerId จาก SharedPreferences หรือจาก API
    var dealerId by remember {
        mutableIntStateOf(sharedPref.getSavedDealerId().toIntOrNull() ?: 0)
    }
    val userId = sharedPref.getSavedUserId().toIntOrNull() ?: 0

    // ถ้า dealerId ยังเป็น 0 → ดึง dealer_id จริงจาก API โดยใช้ userId
    LaunchedEffect(userId) {
        if (dealerId == 0 || dealerId == userId) {
            try {
                ApiClient.carAPI.getDealerId(userId).enqueue(object : Callback<Dealer> {
                    override fun onResponse(call: Call<Dealer>, response: Response<Dealer>) {
                        if (response.isSuccessful) {
                            dealerId = response.body()?.dealer_id ?: 0
                            if (dealerId != 0) {
                                viewModel.loadCars(dealerId)
                            }
                        }
                    }
                    override fun onFailure(call: Call<Dealer>, t: Throwable) {
                        t.printStackTrace()
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            viewModel.loadCars(dealerId)
        }
    }

    // โหลดข้อมูลใหม่ทุกครั้งที่กลับมาหน้านี้
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && dealerId != 0) {
                viewModel.loadCars(dealerId)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val cars = viewModel.carList

    Scaffold(
        bottomBar = {
            BottomNavigationBarSeller(
                currentScreen = currentScreen,
                onNavigate = onNavigate
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ===== Dealer Header =====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color(0xFFA63D2B), CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "Dealer Dashboard",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Manage your cars",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            HorizontalDivider()

            // ===== My Garage Header + Sell Car Button =====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Garage",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { navController.navigate("addcar") },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text("Sell Car")
                }
            }

            // ===== Car Listing =====
            if (cars.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No cars listed yet.\nTap \"Sell Car\" to add your first car!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(cars) { car ->
                        SellerCarCard(
                            car = car,
                            navController = navController,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SellerCarCard(car: Car, navController: NavController, viewModel: CarViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clickable { navController.navigate("car_detail/${car.car_id}") },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column {
            // ===== รูปภาพรถ =====
            val carImages = car.images ?: emptyList()
            Box {
                if (carImages.isNotEmpty()) {
                    val pagerState = rememberPagerState(pageCount = { carImages.size })
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) { page ->
                        Image(
                            painter = rememberAsyncImagePainter(carImages[page].url),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                } else {
                    Image(
                        painter = rememberAsyncImagePainter(car.thumbnail_url),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                // ===== ปุ่มแก้ไขและลบ =====
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                ) {
                    IconButton(
                        onClick = { navController.navigate("edit_car/${car.car_id}") },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.8f), CircleShape)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.DarkGray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { viewModel.deleteCar(car.car_id) },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.8f), CircleShape)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                    }
                }
            }

            // ===== ข้อมูลรถ =====
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "${car.brand_name ?: ""} ${car.model} ${car.year}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "฿ ${car.price}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Color: ${car.color} | Trans: ${car.transmission} | Fuel: ${car.fuel_type}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Mileage: ${car.mileage} km | Engine: ${car.engine_capacity}L",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                if (!car.description.isNullOrBlank()) {
                    Text(
                        text = car.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray,
                        maxLines = 1,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Status badge
                Box(
                    modifier = Modifier
                        .background(
                            if (car.status == "Available" || car.status == "ACTIVE")
                                Color(0xFF4CAF50) else Color.Gray,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = car.status ?: "Unknown",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}