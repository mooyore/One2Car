package com.example.one2car.Home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.one2car.api.DealerInfo
import com.example.one2car.UserClient

@Composable
fun ProfileSellerScreen(
    userId: Int, // รับ userId มาจาก NavGraph
    currentScreen: String,
    onNavigate: (String) -> Unit,
    onEditClick: (Int) -> Unit,
    onLogout: () -> Unit,
    onSwitchToBuyer: () -> Unit
) {
    var isSellerMode by remember { mutableStateOf(true) }
    var dealerInfo by remember { mutableStateOf<DealerInfo?>(null) }
    var userName by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // ดึงข้อมูลจาก API เมื่อเปิดหน้า โดยใช้ userId จริงๆ
    LaunchedEffect(userId) {
        try {
            val info = UserClient.userAPI.getDealerInfo(userId)
            dealerInfo = info
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // ดึงชื่อ user เสมอ (ไม่ว่า dealer จะสำเร็จหรือไม่)
        try {
            val user = UserClient.userAPI.getUserInfo(userId)
            userName = "${user.firstName} ${user.lastName}"
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        isLoading = false
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBarSeller(
                currentScreen = currentScreen,
                onNavigate = onNavigate
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- คำทักทาย ---
                Text(
                    text = "สวัสดีผู้ขาย ${userName ?: ""}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(60.dp),
                            shape = CircleShape,
                            color = Color(0xFFAD4226)
                        ) {}
                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = dealerInfo?.dealerName ?: userName ?: "ยังไม่มีข้อมูลร้าน",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }

                    Button(
                        onClick = { onEditClick(dealerInfo?.dealerId ?: 0) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Edit", color = Color.Black)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(thickness = 1.dp, color = Color.LightGray)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("เปลี่ยนไปซื้อ", fontSize = 16.sp)

                    Row(
                        modifier = Modifier
                            .background(Color(0xFFEEEEEE), RoundedCornerShape(25.dp))
                            .padding(4.dp)
                            .clickable {
                                isSellerMode = !isSellerMode
                                if (!isSellerMode) onSwitchToBuyer()
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    if (!isSellerMode) Color.Red else Color.Transparent,
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Text("ซื้อ", color = if (!isSellerMode) Color.White else Color.Black)
                        }

                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSellerMode) Color.Red else Color.Transparent,
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Text("ขาย", color = if (isSellerMode) Color.White else Color.Black)
                        }
                    }
                }



                Button(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color.Black),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Text("ออกจากระบบ", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}