package com.example.one2car.filenew
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

@Composable
fun HomeDealerScreen(navController: NavController) {

    val viewModel: CarViewModel = viewModel()
    val lifecycleOwner = LocalLifecycleOwner.current

    // โหลดข้อมูลใหม่จาก Database ทุกครั้งที่กลับมาหน้านี้ (เช่น หลังกด Save จากหน้า Edit)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadCars(1)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val cars = viewModel.carList

    Column {
        DealerHeader()
        HorizontalDivider()

        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "My Garage", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Button(onClick = { navController.navigate("addcar") }, shape = RoundedCornerShape(10.dp)) {
                Text("Sell Car")
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 20.dp)) {
            items(cars) { car ->
                DealerCarCard(car = car, navController = navController, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun DealerHeader(){
    Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically){
        Box(modifier = Modifier.size(60.dp).background(Color(0xFFA63D2B), CircleShape))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text("Dealer Dashboard", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Manage your cars", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

@Composable
fun DealerCarCard(car: Car, navController: NavController, viewModel: CarViewModel){
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)
            .clickable { navController.navigate("car_detail/${car.car_id}") },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ){
        Column {
            val carImages = car.images ?: emptyList()
            Box {
                if (carImages.isNotEmpty()) {
                    val pagerState = rememberPagerState(pageCount = { carImages.size })
                    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth().height(200.dp)) { page ->
                        Image(painter = rememberAsyncImagePainter(carImages[page].url), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    }
                } else {
                    Image(painter = rememberAsyncImagePainter(car.thumbnail_url), contentDescription = null, modifier = Modifier.fillMaxWidth().height(200.dp), contentScale = ContentScale.Crop)
                }

                // ปุ่มแก้ไขและลบ
                Row(modifier = Modifier.align(Alignment.TopEnd).padding(10.dp)){
                    IconButton(
                        onClick = { navController.navigate("edit_car/${car.car_id}") },
                        modifier = Modifier.size(36.dp).background(Color.White.copy(alpha = 0.8f), CircleShape)
                    ) { Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.DarkGray) }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { viewModel.deleteCar(car.car_id) },
                        modifier = Modifier.size(36.dp).background(Color.White.copy(alpha = 0.8f), CircleShape)
                    ) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red) }
                }
            }

            Column(modifier = Modifier.padding(14.dp)){
                Text(text = "${car.brand_name ?: ""} ${car.model} ${car.year}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "฿ ${car.price}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))

                Spacer(modifier = Modifier.height(4.dp))
                // แสดงข้อมูลเพิ่มเติมจาก Database
                Text(text = "Color: ${car.color} | Trans: ${car.transmission} | Fuel: ${car.fuel_type}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Mileage: ${car.mileage} km | Engine: ${car.engine_capacity}L", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                if (!car.description.isNullOrBlank()) {
                    Text(text = car.description, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray, maxLines = 1, modifier = Modifier.padding(top = 4.dp))
                }

                Spacer(modifier = Modifier.height(10.dp))
                Box(modifier = Modifier.background(if(car.status == "Available" || car.status == "ACTIVE") Color(0xFF4CAF50) else Color.Gray, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 4.dp)){
                    Text(text = car.status ?: "Unknown", color = Color.White, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
