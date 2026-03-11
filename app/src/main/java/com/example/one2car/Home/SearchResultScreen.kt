package com.example.one2car.Home

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.one2car.api.CreateBookingRequest
import com.example.one2car.api.RetrofitClient
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultScreen(
    searchParams: Map<String, String>,
    userId: Int,
    onBack: () -> Unit,
    onCarDetailClick: (Int) -> Unit,
    onGoToMyBooking: () -> Unit
) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    var cars          by remember { mutableStateOf<List<CarResponse>>(emptyList()) }
    var isLoading     by remember { mutableStateOf(true) }
    var isBooking     by remember { mutableStateOf(false) }
    var showDialog    by remember { mutableStateOf(false) }
    var selectedCar   by remember { mutableStateOf<CarResponse?>(null) }

    LaunchedEffect(searchParams) {
        isLoading = true
        try {
            val bName = searchParams["brandName"] ?: searchParams["brand_name"]
            val mdl   = searchParams["model"]
            val yr    = searchParams["year"]?.toIntOrNull()
            val trans = searchParams["trans"] ?: searchParams["transmission"]
            val fuel  = searchParams["fuel"] ?: searchParams["fuel_type"]
            val minP  = searchParams["minP"]?.toIntOrNull()
            val maxP  = searchParams["maxP"]?.toIntOrNull()

            val result = RetrofitClient.instance.searchCars(
                brandName    = bName,
                model        = mdl,
                year         = yr,
                transmission = trans,
                fuelType     = fuel,
                minPrice     = minP,
                maxPrice     = maxP,
                status       = null
            )
            cars = result
        } catch (e: Exception) {
            Log.e("SEARCH", "Error: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isLoading) "กำลังค้นหา..."
                        else "รายการรถที่พบทั้งหมด ${cars.size} คัน",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF001F3F))
            } else if (cars.isEmpty()) {
                Text("ไม่พบรถที่ตรงตามเงื่อนไข", modifier = Modifier.align(Alignment.Center), color = Color.Gray)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(cars) { car ->
                        CarResultCard(
                            car = car,
                            onClick = { onCarDetailClick(car.car_id) },
                            onCallClick = {
                                val phone = car.dealer_phone ?: "ไม่มีข้อมูลเบอร์โทร"
                                Toast.makeText(context, "โทร: $phone", Toast.LENGTH_SHORT).show()
                            },
                            onBookClick = {
                                selectedCar = car
                                showDialog = true
                            }
                        )
                    }
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { if (!isBooking) showDialog = false },
                title = { Text("ยืนยันการจองรถ") },
                text  = { Text("คุณต้องการจอง ${selectedCar?.brand_name} ${selectedCar?.model}?") },
                confirmButton = {
                    Button(onClick = {
                        selectedCar?.let { car ->
                            isBooking = true
                            scope.launch {
                                try {
                                    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                    RetrofitClient.instance.createBooking(CreateBookingRequest(userId, car.car_id, today))
                                    showDialog = false
                                    onGoToMyBooking()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "จองไม่สำเร็จ", Toast.LENGTH_SHORT).show()
                                } finally { isBooking = false }
                            }
                        }
                    }) { Text("ยืนยัน") }
                },
                dismissButton = { TextButton(onClick = { showDialog = false }) { Text("ยกเลิก") } }
            )
        }
    }
}

@Composable
fun CarResultCard(car: CarResponse, onClick: () -> Unit, onCallClick: () -> Unit, onBookClick: () -> Unit) {
    val fmt = DecimalFormat("#,###")
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(8.dp)).background(Color.LightGray)) {
                AsyncImage(
                    model = car.thumbnail_url?.replace("localhost", "10.0.2.2"),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth().border(1.dp, Color.Black, RoundedCornerShape(8.dp)).padding(12.dp)) {
                Column {
                    Text("฿${fmt.format(car.price)}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF001F3F))
                    Text("${car.brand_name} ${car.model}", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Text("ปี ${car.year} | ไมล์ ${fmt.format(car.mileage)} กม.", fontSize = 13.sp, color = Color.Gray)
                    Text("ดีลเลอร์: ${car.dealer_name ?: "ไม่ระบุ"}", fontSize = 13.sp, color = Color(0xFF001F3F))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { onCallClick() },
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, Color.Black)
                ) {
                    Text("โทร", color = Color.Black)
                }
                Button(onClick = onBookClick, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF001F3F))) {
                    Text("จองรถ", color = Color.White)
                }
            }
        }
    }
}