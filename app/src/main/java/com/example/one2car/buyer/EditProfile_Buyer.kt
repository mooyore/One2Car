package com.example.one2car.buyer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun EditProfile_Buyer(
    initialFirstName: String = "",
    initialLastName: String = "",
    initialEmail: String = "",
    initialPhone: String = "",
    onUpdate: (firstName: String, lastName: String, email: String, phone: String) -> Unit,
    onCancel: () -> Unit
) {
    // เปลี่ยนจาก TextFieldValue เป็น String เพื่อให้รองรับภาษาไทยได้สมบูรณ์
    var firstName by remember { mutableStateOf(initialFirstName) }
    var lastName by remember { mutableStateOf(initialLastName) }
    var email by remember { mutableStateOf(initialEmail) }
    var phone by remember { mutableStateOf(initialPhone) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "แก้ไขโปรไฟล์คนซื้อ",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFFB24021))
        )
        TextButton(onClick = { /* TODO: Image Picker */ }) {
            Text("เปลี่ยนรูปโปรไฟล์", color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EditField(label = "ชื่อ", value = firstName, onValueChange = { firstName = it })
            EditField(label = "นามสกุล", value = lastName, onValueChange = { lastName = it })
            EditField(
                label = "อีเมล",
                value = email,
                onValueChange = { email = it },
                keyboardType = KeyboardType.Email
            )
            EditField(
                label = "หมายเลขโทรศัพท์",
                value = phone,
                onValueChange = { phone = it },
                keyboardType = KeyboardType.Phone
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { onUpdate(firstName, lastName, email, phone) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003366)),
                modifier = Modifier.weight(1f).height(50.dp)
            ) {
                Text("Update", color = Color.White)
            }

            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                modifier = Modifier.weight(1f).height(50.dp)
            ) {
                Text("Cancel", color = Color.White)
            }
        }
    }
}

@Composable
fun EditField(
    label: String,
    value: String, // รับเป็น String
    onValueChange: (String) -> Unit, // คืนค่าเป็น String
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )
    }
}