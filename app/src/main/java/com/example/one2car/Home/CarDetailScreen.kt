package com.example.one2car.Home

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import com.example.one2car.api.CarResponse
import com.example.one2car.api.RetrofitClient
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarDetailScreen(
    carId: Int,
    onBack: () -> Unit,
    onBookClick: () -> Unit,
    onDealerProfileClick: (Int) -> Unit
) {
    val context = LocalContext.current
    var car by remember { mutableStateOf<CarResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    val fmt = DecimalFormat("#,###")

    LaunchedEffect(carId) {
        isLoading = true
        hasError = false
        try {
            // เรียกใช้ ApiService ผ่าน RetrofitClient.instance
            val response = RetrofitClient.instance.getCarDetail(carId)
            car = response
        } catch (e: Exception) {
            e.printStackTrace()
            hasError = true
            Toast.makeText(context, "โหลดข้อมูลล้มเหลว", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        bottomBar = {
            car?.let { carData ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .navigationBarsPadding(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val phone = carData.dealer_phone ?: "ไม่มีข้อมูลเบอร์โทร"
                                Toast.makeText(context, "เบอร์โทรดีลเลอร์: $phone", Toast.LENGTH_LONG).show()
                            },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(2.dp, Color.Black)
                        ) {
                            Text("โทร", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }

                        Button(
                            onClick = onBookClick,
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            border = BorderStroke(2.dp, Color.Black)
                        ) {
                            Text("จองรถ", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues).background(Color.White)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (hasError || car == null) {
                Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ไม่พบข้อมูล")
                    Button(onClick = onBack) { Text("กลับ") }
                }
            } else {
                val carData = car!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. ส่วนรูปภาพ
                    Card(
                        modifier = Modifier.fillMaxWidth().height(250.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(4.dp, Color.Black),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            val thumbnailUrl = carData.thumbnail_url
                            if (!thumbnailUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = thumbnailUrl.replace("localhost", "10.0.2.2"),
                                    contentDescription = carData.model,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text("ไม่มีรูปภาพ", modifier = Modifier.align(Alignment.Center), color = Color.Gray)
                            }

                            IconButton(onClick = onBack, modifier = Modifier.padding(8.dp)) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    modifier = Modifier.size(32.dp),
                                    tint = if (!thumbnailUrl.isNullOrEmpty()) Color.White else Color.Black
                                )
                            }
                        }
                    }

                    // 2. รายละเอียดรถ
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(0.dp),
                        border = BorderStroke(4.dp, Color.Black),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "รายละเอียดรถ ราคา: ฿${fmt.format(carData.price)} ชื่อ: ${carData.brand_name} รุ่น: ${carData.model}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // 3. รายละเอียดอื่นๆ
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(0.dp),
                        border = BorderStroke(4.dp, Color.Black),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "รายละเอียดรถอื่นๆ ทั้งหมด เลขไมล์: ${fmt.format(carData.mileage)} เชื้อเพลิง: ${carData.fuel_type}",
                                fontSize = 16.sp,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "คำอธิบาย: ${carData.description}",
                                fontSize = 16.sp,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    }

                    // 4. ส่วน Dealer Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val dId = carData.dealer_id
                                if (dId != 0) {
                                    onDealerProfileClick(dId)
                                } else {
                                    Toast.makeText(context, "ไม่พบข้อมูลโปรไฟล์ผู้ขาย", Toast.LENGTH_SHORT).show()
                                }
                            },
                        shape = RoundedCornerShape(0.dp),
                        border = BorderStroke(4.dp, Color.Black),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFB33A3A))
                                    .border(2.dp, Color.Black, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = carData.dealer_name ?: "ดูโปรไฟล์ผู้ขาย",
                                fontSize = 18.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}