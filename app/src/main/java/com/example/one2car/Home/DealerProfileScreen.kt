package com.example.one2car.Home


import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.one2car.api.*
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealerProfileScreen(
    dealerId: Int,
    onBack: () -> Unit,
    onCarClick: (Int) -> Unit
) {
    val context = LocalContext.current
    var dealerInfo by remember { mutableStateOf<DealerInfo?>(null) }
    var cars by remember { mutableStateOf<List<CarResponse>>(emptyList()) }
    var reviews by remember { mutableStateOf<List<ReviewResponse>>(emptyList()) }
    var ratingSummary by remember { mutableStateOf<RatingSummary?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(dealerId) {
        // แยก try-catch แต่ละ API เพื่อให้ถ้าอันไหน fail อันอื่นยังทำงานได้
        try {
            dealerInfo = RetrofitClient.instance.getDealerInfo(dealerId)
        } catch (e: Exception) {
            android.util.Log.e("DEALER_PROFILE", "getDealerInfo failed: ${e.message}")
        }

        try {
            cars = RetrofitClient.instance.getMyCars(dealerId)
        } catch (e: Exception) {
            android.util.Log.e("DEALER_PROFILE", "getMyCars failed: ${e.message}")
        }

        try {
            reviews = RetrofitClient.instance.getDealerReviews(dealerId)
        } catch (e: Exception) {
            android.util.Log.e("DEALER_PROFILE", "getDealerReviews failed: ${e.message}")
        }

        try {
            ratingSummary = RetrofitClient.instance.getDealerRatingSummary(dealerId)
        } catch (e: Exception) {
            android.util.Log.e("DEALER_PROFILE", "getDealerRatingSummary failed: ${e.message}")
        }

        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("โปรไฟล์ผู้ขาย", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFAD4226))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                item {
                    DealerHeader(dealerInfo, ratingSummary)
                }

                item {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.White,
                        contentColor = Color(0xFFAD4226)
                    ) {
                        Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                            Text("รายการประกาศ", Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                        }
                        Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                            Text("รีวิว", Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (selectedTab == 0) {
                    if (cars.isEmpty()) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                                Text("ไม่พบรายการรถ", color = Color.Gray)
                            }
                        }
                    } else {
                        items(cars) { car -> CarDealerCard(car, onCarClick) }
                    }
                } else {
                    if (reviews.isEmpty()) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                                Text("ยังไม่มีรีวิว", color = Color.Gray)
                            }
                        }
                    } else {
                        items(reviews) { review -> ReviewCard(review) }
                    }
                }
            }
        }
    }
}

@Composable
fun DealerHeader(info: DealerInfo?, summary: RatingSummary?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = info?.logoUrl,
            contentDescription = "Dealer Logo",
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .border(1.dp, Color.LightGray, CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = info?.dealerName ?: "ชื่อร้าน",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        val avg = summary?.avgRating ?: "0.0"

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = avg,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFAD4226),
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Row {
                repeat(5) { i ->
                    val ratingValue = avg.toDoubleOrNull() ?: 0.0
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (i < ratingValue.toInt()) Color(0xFFAD4226) else Color.LightGray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Text(
            text = "ที่อยู่: ${info?.address ?: "-"}",
            color = Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun CarDealerCard(car: CarResponse, onClick: (Int) -> Unit) {
    val fmt = DecimalFormat("#,###")
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 8.dp),
        border = BorderStroke(1.dp, Color.Black),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(12.dp)) {
            AsyncImage(
                model = car.thumbnail_url,
                contentDescription = "Car Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "฿${fmt.format(car.price)}",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black
            )
            Text(
                text = "${car.brand_name} ${car.model} | ปี ${car.year}",
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
            Button(
                onClick = { onClick(car.car_id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("ดูรายละเอียด", color = Color.White)
            }
        }
    }
}

@Composable
fun ReviewCard(review: ReviewResponse) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
        border = BorderStroke(0.5.dp, Color.LightGray)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(modifier = Modifier.padding(bottom = 4.dp)) {
                repeat(5) { i ->
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (i < review.rating) Color(0xFFAD4226) else Color.LightGray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Text(
                text = "${review.firstName} ${review.lastName}",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Text(
                text = review.comment ?: "ไม่มีข้อความรีวิว",
                fontSize = 14.sp,
                color = Color.DarkGray
            )
        }
    }
}