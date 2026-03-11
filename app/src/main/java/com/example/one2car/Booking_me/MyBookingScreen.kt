package com.example.one2car.Booking_me

import androidx.compose.foundation.BorderStroke
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.one2car.api.UserBooking
import com.example.one2car.ui.booking.BookingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingScreen(
    viewModel: BookingViewModel,
    userId: Int,
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    val bookings by viewModel.userBookings.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var bookingToCancel by remember { mutableStateOf<UserBooking?>(null) }

    LaunchedEffect(userId) {
        android.util.Log.d("MY_BOOKING_DEBUG", "MyBookingScreen userId = $userId")
        if (userId > 0) {
            viewModel.fetchUserBookings(userId)
        }
    }

    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("My Booking", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF2F2F7))
        ) {
            when {
                loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                bookings.isEmpty() -> {
                    Text(
                        "คุณยังไม่มีรายการจอง",
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(bookings, key = { it.bookingId }) { booking ->
                            UserBookingItem(
                                booking = booking,
                                onCancelClick = { bookingToCancel = booking }
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
            title = { Text("ยกเลิกการจองรถ") },
            text = { Text("คุณต้องการยกเลิกการจองรถ ${booking.brandName ?: ""} ${booking.model} ใช่หรือไม่?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.cancelBooking(booking.bookingId, userId = userId)
                        bookingToCancel = null
                    }
                ) {
                    Text("ยืนยัน", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { bookingToCancel = null }) {
                    Text("ยกเลิก")
                }
            }
        )
    }
}

@Composable
fun UserBookingItem(
    booking: UserBooking,
    onCancelClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(Color.LightGray)
            ) {
                if (!booking.thumbnailUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = booking.thumbnailUrl,
                        contentDescription = "${booking.brandName} ${booking.model}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${booking.brandName ?: ""} ${booking.model ?: ""}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    val statusColor = when (booking.status.lowercase()) {
                        "pending" -> Color.Gray
                        "completed" -> Color(0xFF28A745)
                        "canceled", "cancelled" -> Color.Red
                        else -> Color.DarkGray
                    }
                    Text(
                        text = booking.status.replaceFirstChar { it.uppercase() },
                        color = statusColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }

                Text(
                    text = "วันที่จอง: ${booking.bookingDate}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                if (booking.status.lowercase() == "pending") {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onCancelClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color.Red)
                    ) {
                        Text("ยกเลิกการจอง", color = Color.Red)
                    }
                }
            }
        }
    }
}
