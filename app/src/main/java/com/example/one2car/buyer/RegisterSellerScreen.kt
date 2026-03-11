package com.example.one2car.buyer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.one2car.UserClient
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

@Composable
fun RegisterSellerScreen(
    userId: Int,
    onSuccess: (Int) -> Unit,
    onCancel: () -> Unit
) {
    var shopName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    LaunchedEffect(userId) {
        try {
            val user = UserClient.userAPI.getUserInfo(userId)
            shopName = user.firstName
            phoneNumber = user.phone
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "หน้าสมัครขายรถ",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFFB24021))
        )
        TextButton(onClick = { /* TODO: เพิ่ม Image Picker */ }) {
            Text("เปลี่ยนรูปโปรไฟล์ (logo_image)", color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = shopName,
            onValueChange = { shopName = it },
            label = { Text("ชื่อเต็นท์รถ") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("เบอร์โทรติดต่อ") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("ที่อยู่") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onCancel,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            try {
                                val mediaType = "text/plain".toMediaTypeOrNull()
                                val userIdPart = userId.toString().toRequestBody(mediaType)
                                val namePart = shopName.toRequestBody(mediaType)
                                val phonePart = phoneNumber.toRequestBody(mediaType)
                                val addressPart = address.toRequestBody(mediaType)

                                val response = UserClient.userAPI.upgradeToDealer(
                                    userId = userIdPart,
                                    dealerName = namePart,
                                    phone = phonePart,
                                    address = addressPart,
                                    logoImage = null
                                )

                                onSuccess(response.dealerId)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003366)),
                    modifier = Modifier.weight(1f).height(50.dp),
                    enabled = shopName.isNotBlank() && phoneNumber.isNotBlank()
                ) {
                    Text("สร้างเต็นท์ของฉัน")
                }
            }
        }
    }
}