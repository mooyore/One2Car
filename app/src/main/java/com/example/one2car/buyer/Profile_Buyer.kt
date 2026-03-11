package com.example.one2car.buyer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.one2car.Home.BottomNavigationBarBuyer

@Composable
fun Profile_Buyer(
    // ข้อมูลผู้ใช้
    currentScreen: String,
    firstName: String,
    lastName: String,
    email: String = "",
    phone: String = "",
    isSeller: Boolean,                   // เช็คว่าเป็นผู้ขายหรือยัง

    // Callbacks สำหรับการนำทาง
    onNavigate: (String) -> Unit,         // ไปหน้า Home, Booking
    onEditClick: () -> Unit,              // ไปหน้าแก้ไขโปรไฟล์
    onRegisterSellerClick: () -> Unit,    // ไปหน้าสมัครขายรถ
    onSwitchToSeller: () -> Unit,         // สลับไปหน้า Home Seller
    onLogout: () -> Unit                  // ออกจากระบบ
) {
    Scaffold(
        bottomBar = {
            BottomNavigationBarBuyer(
                currentScreen = currentScreen,
                onNavigate = onNavigate
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("สวัสดีผู้ซื้อ", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            // --- ส่วน Header: รูปโปรไฟล์ + ชื่อ + ปุ่ม Edit ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFB24021))
                )
                Spacer(modifier = Modifier.width(12.dp))

                // แสดงชื่อจริงและนามสกุลที่รับมาจาก State/Database
                Text("$firstName $lastName", fontSize = 18.sp, fontWeight = FontWeight.Medium)

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onEditClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Edit", color = Color.Black)
                }
            }

            HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
            Spacer(modifier = Modifier.height(8.dp))

            // --- ส่วน Mode Switcher: แสดงตามสถานะ isSeller ---
            if (isSeller) {
                // ถ้าเป็น dealer แล้ว → แสดง toggle ซื้อ/ขาย
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("เปลี่ยนไปขาย", fontSize = 16.sp)

                    Row(
                        modifier = Modifier
                            .background(Color(0xFFEEEEEE), RoundedCornerShape(25.dp))
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // ปุ่มซื้อ (active)
                        Box(
                            modifier = Modifier
                                .background(Color.Red, RoundedCornerShape(20.dp))
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Text("ซื้อ", color = Color.White)
                        }

                        // ปุ่มขาย (inactive, กดแล้วสลับไปหน้า seller)
                        Box(
                            modifier = Modifier
                                .clickable { onSwitchToSeller() }
                                .background(Color.Transparent, RoundedCornerShape(20.dp))
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Text("ขาย", color = Color.Black)
                        }
                    }
                }
            } else {
                // ถ้ายังไม่เป็น dealer → แสดงปุ่มสมัครขายรถ
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.Black)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("สมัครเพื่อลงขายรถ")
                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = onRegisterSellerClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text(
                            text = "ซื้อ",
                            color = Color.White,
                            modifier = Modifier
                                .background(Color.DarkGray)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                        Text(
                            text = " สมัครขายรถ",
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            // --- ส่วนแสดงข้อมูลเพิ่มเติม หรือ ประวัติ ---
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (email.isNotEmpty()) Text("Email: $email", color = Color.Gray)
                    if (phone.isNotEmpty()) Text("Tel: $phone", color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("ประวัติการดูรถล่าสุด", color = Color.LightGray)
                }
            }

            // --- ปุ่มออกจากระบบ ---
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RectangleShape,
                border = BorderStroke(1.dp, Color.Black)
            ) {
                Text("ออกจากระบบ", color = Color.Black)
            }
        }
    }
}