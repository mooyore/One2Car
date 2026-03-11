package com.example.one2car.Home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.one2car.api.BrandInfo
import com.example.one2car.api.CarModelInfo
import com.example.one2car.UserClient

data class CarSearchFilter(
    val brandId: Int?,
    val model: String?,
    val transmission: String?,
    val fuelType: String?,
    val minPrice: String?,
    val maxPrice: String?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchCarScreen(
    onBack: () -> Unit,
    onSearch: (CarSearchFilter) -> Unit
) {

    var keyword by remember { mutableStateOf("") }

    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }

    var transmission by remember { mutableStateOf("ทั้งหมด") }
    var fuelType by remember { mutableStateOf("") }

    var minPrice by remember { mutableStateOf("") }
    var maxPrice by remember { mutableStateOf("") }

    var brandList by remember { mutableStateOf<List<BrandInfo>>(emptyList()) }
    var modelList by remember { mutableStateOf<List<CarModelInfo>>(emptyList()) }

    // โหลด Brand
    LaunchedEffect(Unit) {
        try {
            brandList = UserClient.userAPI.getBrands()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // โหลด Model ตาม Brand
    LaunchedEffect(brand) {

        if (brand.isNotEmpty()) {

            val selectedBrand = brandList.find { it.brandName == brand }

            if (selectedBrand != null) {

                try {
                    modelList = UserClient.userAPI.getModels(selectedBrand.brandId)
                    model = ""
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

        } else {

            modelList = emptyList()
            model = ""

        }
    }

    Scaffold(

        topBar = {

            TopAppBar(

                title = {

                    OutlinedTextField(

                        value = keyword,
                        onValueChange = { keyword = it },

                        placeholder = { Text("ค้นหา", fontSize = 14.sp) },

                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .padding(end = 8.dp),

                        shape = RoundedCornerShape(25.dp),

                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )

                    )

                },

                actions = {

                    TextButton(onClick = onBack) {
                        Text("ย้อนกลับ", color = Color.Black)
                    }

                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )

            )

        }

    ) { paddingValues ->

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)

        ) {

            Text("ยี่ห้อ, รุ่น", fontWeight = FontWeight.Bold, fontSize = 16.sp)

            Spacer(modifier = Modifier.height(8.dp))

            SearchDropdownField(
                label = "เลือกยี่ห้อ",
                value = brand,
                options = brandList.map { it.brandName }
            ) { brand = it }

            SearchDropdownField(
                label = "เลือกรุ่น",
                value = model,
                options = modelList.map { it.modelName }
            ) { model = it }

            Spacer(modifier = Modifier.height(16.dp))

            Text("เกียร์", fontWeight = FontWeight.Bold)

            Row(verticalAlignment = Alignment.CenterVertically) {

                listOf("ทั้งหมด", "Auto", "Manual").forEach { option ->

                    Row(verticalAlignment = Alignment.CenterVertically) {

                        RadioButton(
                            selected = transmission == option,
                            onClick = { transmission = option }
                        )

                        Text(option)

                    }

                    Spacer(modifier = Modifier.width(8.dp))

                }

            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("ชนิดเชื้อเพลิง", fontWeight = FontWeight.Bold)

            SearchDropdownField(
                label = "เลือกชนิดเชื้อเพลิง",
                value = fuelType,
                options = listOf("Gasoline", "Diesel", "EV", "Hybrid")
            ) { fuelType = it }

            Spacer(modifier = Modifier.height(16.dp))

            Text("ช่วงราคา", fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(8.dp))

            RangeInputRow(
                "ราคา",
                minPrice,
                maxPrice,
                { minPrice = it },
                { maxPrice = it }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(

                onClick = {

                    val selectedBrand = brandList.find { it.brandName == brand }

                    onSearch(

                        CarSearchFilter(

                            brandId = selectedBrand?.brandId,
                            model = model.ifEmpty { null },

                            transmission =
                                if (transmission == "ทั้งหมด") null
                                else transmission,

                            fuelType = fuelType.ifEmpty { null },

                            minPrice = minPrice.ifEmpty { null },
                            maxPrice = maxPrice.ifEmpty { null }

                        )

                    )

                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF001F3F)
                ),

                shape = RoundedCornerShape(8.dp)

            ) {

                Text(
                    "ยืนยัน",
                    fontSize = 18.sp,
                    color = Color.White
                )

            }

        }

    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchDropdownField(
    label: String,
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    // คลุมด้วย Box เพื่อจัดการขนาดและตำแหน่ง
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        OutlinedTextField(
            // แสดงค่าที่เลือก ถ้ายังไม่เลือกให้เป็นค่าว่างเพื่อให้เห็น Placeholder
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) }, // ใช้ Label แทน Placeholder จะดูเป็นระเบียบกว่า
            trailingIcon = {
                // เพิ่ม animation ลูกศร
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor() // สำคัญมากสำหรับการวางตำแหน่ง Menu
                .fillMaxWidth(),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (options.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("ไม่มีข้อมูล", color = Color.Gray) },
                    onClick = { expanded = false },
                    enabled = false // กดไม่ได้
                )
            } else {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}

@Composable
fun RangeInputRow(
    label: String,
    minVal: String,
    maxVal: String,
    onMinChange: (String) -> Unit,
    onMaxChange: (String) -> Unit
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        OutlinedTextField(
            value = minVal,
            onValueChange = onMinChange,
            placeholder = { Text("$label (ต่ำสุด)") },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        OutlinedTextField(
            value = maxVal,
            onValueChange = onMaxChange,
            placeholder = { Text("$label (สูงสุด)") },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

    }

}
