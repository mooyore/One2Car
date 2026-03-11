package com.example.one2car.Home

import androidx.compose.foundation.background
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
import com.example.one2car.UserClient
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileSellerScreen(
    userId: Int, // รับ userId มาจากหน้า Profile → ใช้ดึงข้อมูล dealer
    onBack: () -> Unit
) {
    var dealerName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var actualDealerId by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    // ดึงข้อมูล dealer โดยใช้ userId (backend: /dealer-info/:user_id → WHERE user_id = ?)
    LaunchedEffect(userId) {
        if (userId == 0) {
            isLoading = false
            return@LaunchedEffect
        }
        try {
            val info = UserClient.userAPI.getDealerInfo(userId)
            dealerName = info.dealerName
            phone = info.phone ?: ""
            address = info.address ?: ""
            actualDealerId = info.dealerId // เก็บ dealer_id จริงไว้สำหรับ update
        } catch (e: Exception) {
            android.util.Log.e("EDIT_SELLER", "getDealerInfo failed: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = Color(0xFFAD4226)
            ) {}

            TextButton(onClick = { /* เลือกรูป */ }) {
                Text("เปลี่ยนรูปโปรไฟล์", color = Color.Black, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
            Spacer(modifier = Modifier.height(20.dp))

            EditFieldItem(label = "ชื่อเต็นรถ", value = dealerName, onValueChange = { dealerName = it })
            EditFieldItem(label = "เบอร์โทรติดต่อ", value = phone, onValueChange = { phone = it })
            EditFieldItem(label = "ที่อยู่", value = address, onValueChange = { address = it })

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        if (actualDealerId == 0) {
                            android.util.Log.e("EDIT_SELLER", "Cannot update: dealerId is 0")
                            return@Button
                        }
                        scope.launch {
                            try {
                                val namePart = dealerName.toRequestBody("text/plain".toMediaTypeOrNull())
                                val addressPart = address.toRequestBody("text/plain".toMediaTypeOrNull())
                                val phonePart = phone.toRequestBody("text/plain".toMediaTypeOrNull())

                                android.util.Log.d("EDIT_SELLER", "Updating dealerId=$actualDealerId")

                                val response = UserClient.userAPI.updateDealer(
                                    dealerId = actualDealerId,
                                    dealerName = namePart,
                                    address = addressPart,
                                    phone = phonePart,
                                    logoImage = null
                                )

                                android.util.Log.d("EDIT_SELLER", "Response: ${response.code()}")

                                if (response.isSuccessful) {
                                    onBack()
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("EDIT_SELLER", "Update failed: ${e.message}", e)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF001F3F)),
                    modifier = Modifier.width(140.dp).height(45.dp),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text("update", color = Color.White)
                }

                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.width(140.dp).height(45.dp),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text("cancel", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun EditFieldItem(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Black
            ),
            singleLine = true
        )
    }
}