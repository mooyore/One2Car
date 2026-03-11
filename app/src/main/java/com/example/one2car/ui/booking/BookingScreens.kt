package com.example.one2car.ui.booking

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
// แก้ไข Import ให้ถูกต้อง
import com.example.one2car.api.DealerBooking

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealerBookingScreen(
    viewModel: BookingViewModel,
    dealerId: Int,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    val bookings by viewModel.dealerBookings.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var bookingToCancel by remember { mutableStateOf<DealerBooking?>(null) }

    LaunchedEffect(dealerId) {
        viewModel.fetchDealerBookings(dealerId)
    }

    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessage()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text("รายการจองที่เข้ามา", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8F9FA))
        ) {
            when {
                loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                bookings.isEmpty() -> {
                    Text("ไม่มีรายการจองในขณะนี้", modifier = Modifier.align(Alignment.Center), color = Color.Gray)
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(items = bookings, key = { it.bookingId }) { booking ->
                            DealerBookingItem(
                                booking = booking,
                                onComplete = {
                                    viewModel.completeBooking(booking.bookingId, dealerId)
                                },
                                onCancelClick = {
                                    bookingToCancel = booking
                                },
                                onContact = {
                                    Toast.makeText(
                                        context,
                                        "เบอร์ลูกค้า: ${booking.customerPhone ?: "-"}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    bookingToCancel?.let { booking ->
        AlertDialog(
            onDismissRequest = { bookingToCancel = null },
            title = { Text("ยืนยันการยกเลิกการจอง") },
            text = {
                Text("คุณต้องการยกเลิกการจองรถ ${booking.brandName ?: ""} ${booking.model ?: ""} ของคุณ ${booking.firstName ?: ""} ใช่หรือไม่?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.cancelBooking(bookingId = booking.bookingId, dealerId = dealerId)
                        bookingToCancel = null
                    }
                ) {
                    Text("ยืนยัน", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { bookingToCancel = null }) {
                    Text("ย้อนกลับ")
                }
            }
        )
    }
}

@Composable
fun DealerBookingItem(
    booking: DealerBooking,
    onComplete: () -> Unit,
    onCancelClick: () -> Unit,
    onContact: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFEEEEEE))
            ) {
                if (!booking.thumbnailUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = booking.thumbnailUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("ไม่มีรูปภาพ")
                    }
                }

                val badgeColor = when (booking.status.lowercase()) {
                    "pending" -> Color.Gray
                    "completed" -> Color(0xFF2E7D32)
                    "canceled", "cancelled" -> Color.Red
                    else -> Color.DarkGray
                }

                Surface(
                    modifier = Modifier.padding(8.dp),
                    color = badgeColor,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        booking.status,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(6.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded }
                        .padding(10.dp)
                ) {
                    Text("รายละเอียดรถ", modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                    Text(if (expanded) "▲" else "▼")
                }

                AnimatedVisibility(expanded) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("ยี่ห้อ: ${booking.brandName ?: "-"}")
                        Spacer(Modifier.height(4.dp))
                        Text("รุ่น: ${booking.model ?: "-"}")
                        Spacer(Modifier.height(4.dp))
                        Text("ราคา: ${booking.price ?: "-"} บาท")
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (booking.status.lowercase() == "pending") {
                    Button(onClick = onComplete, modifier = Modifier.weight(1f)) {
                        Text("ยืนยันสำเร็จ", fontSize = 12.sp)
                    }
                    Button(
                        onClick = onCancelClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("ยกเลิก", fontSize = 12.sp)
                    }
                }

                OutlinedButton(
                    onClick = onContact,
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, Color.Blue)
                ) {
                    Text("ติดต่อลูกค้า", fontSize = 12.sp)
                }
            }
        }
    }
}