package com.example.one2car.filenew
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

import com.example.one2car.SharedPreferencesManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCarScreen(navController: NavController) {

    val context = LocalContext.current
    val sharedPref = SharedPreferencesManager(context)

    // ดึง dealerId จาก SharedPreferences ก่อน ถ้าไม่มีค่อยดึงจาก API
    val savedDealerId = sharedPref.getSavedDealerId().toIntOrNull() ?: 0
    val userId = sharedPref.getSavedUserId().toIntOrNull() ?: 0

    var dealerId by remember { mutableIntStateOf(savedDealerId) }
    var loading by remember { mutableStateOf(false) }
    var brandsError by remember { mutableStateOf("") }
    var dealerError by remember { mutableStateOf("") }

    var brands by remember { mutableStateOf(listOf<Brand>()) }
    var selectedBrand by remember { mutableStateOf<Brand?>(null) }

    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }

    var fuel by remember { mutableStateOf("Gasoline") }
    var transmission by remember { mutableStateOf("Auto") }

    var mileage by remember { mutableStateOf("") }
    var engine by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var brandExpanded by remember { mutableStateOf(false) }
    var fuelExpanded by remember { mutableStateOf(false) }
    var transExpanded by remember { mutableStateOf(false) }

    var imageUris by remember { mutableStateOf(listOf<Uri>()) }

    val fuelList = listOf("Gasoline", "Diesel", "Hybrid", "EV")
    val transList = listOf("Auto", "Manual")

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) {
        if (it.isNotEmpty()) imageUris = imageUris + it
    }

    // ฟังก์ชันโหลด Brands
    fun loadBrands() {
        brandsError = ""
        Log.d("AddCar", "Loading brands from API...")
        ApiClient.carAPI.getBrands().enqueue(object : Callback<List<Brand>> {
            override fun onResponse(call: Call<List<Brand>>, response: Response<List<Brand>>) {
                if (response.isSuccessful) {
                    brands = response.body() ?: emptyList()
                    brandsError = ""
                    Log.d("AddCar", "✅ Loaded ${brands.size} brands")
                    if (brands.isEmpty()) {
                        brandsError = "ไม่มีข้อมูลยี่ห้อรถในระบบ"
                    }
                } else {
                    val errMsg = response.errorBody()?.string() ?: "Unknown"
                    brandsError = "โหลดยี่ห้อไม่สำเร็จ (${response.code()}): $errMsg"
                    Log.e("AddCar", "❌ Brands API error: ${response.code()} - $errMsg")
                    Toast.makeText(context, brandsError, Toast.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<List<Brand>>, t: Throwable) {
                brandsError = "Network error: ${t.message}"
                Log.e("AddCar", "❌ Brands network error: ${t.message}", t)
                Toast.makeText(context, "โหลดยี่ห้อไม่ได้: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    // ฟังก์ชันโหลด DealerId
    fun loadDealerId() {
        dealerError = ""
        if (dealerId == 0 && userId != 0) {
            Log.d("AddCar", "Loading dealerId for userId: $userId")
            ApiClient.carAPI.getDealerId(userId).enqueue(object : Callback<Dealer> {
                override fun onResponse(call: Call<Dealer>, response: Response<Dealer>) {
                    if (response.isSuccessful) {
                        dealerId = response.body()?.dealer_id ?: 0
                        dealerError = ""
                        Log.d("AddCar", "✅ Got dealerId: $dealerId for userId: $userId")
                        if (dealerId == 0) {
                            dealerError = "ไม่พบ Dealer สำหรับ userId: $userId"
                        }
                    } else {
                        val errMsg = response.errorBody()?.string() ?: "Unknown"
                        dealerError = "ดึง DealerId ไม่ได้ (${response.code()}): $errMsg"
                        Log.e("AddCar", "❌ DealerId API error: ${response.code()} - $errMsg")
                        Toast.makeText(context, dealerError, Toast.LENGTH_LONG).show()
                    }
                }
                override fun onFailure(call: Call<Dealer>, t: Throwable) {
                    dealerError = "Network error: ${t.message}"
                    Log.e("AddCar", "❌ DealerId network error: ${t.message}", t)
                    Toast.makeText(context, "ดึง DealerId ไม่ได้: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        } else if (dealerId != 0) {
            Log.d("AddCar", "✅ Using saved dealerId: $dealerId, userId: $userId")
        } else {
            dealerError = "userId เป็น 0 - กรุณา Login ใหม่"
            Log.e("AddCar", "❌ userId is 0, cannot get dealerId")
        }
    }

    LaunchedEffect(Unit) {
        loadBrands()
        loadDealerId()
    }

    Scaffold(

        topBar = {
            TopAppBar(
                title = { Text("Add Car") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }

    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // === แสดงสถานะ Debug ===
            if (brandsError.isNotEmpty() || dealerError.isNotEmpty() || dealerId == 0 || brands.isEmpty()) {
                Surface(
                    color = Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("⚠️ สถานะ:", color = Color.Red, style = MaterialTheme.typography.titleSmall)
                        Text("DealerId: $dealerId (userId: $userId)", style = MaterialTheme.typography.bodySmall)
                        Text("Brands loaded: ${brands.size}", style = MaterialTheme.typography.bodySmall)
                        if (brandsError.isNotEmpty()) {
                            Text("❌ Brand: $brandsError", color = Color.Red, style = MaterialTheme.typography.bodySmall)
                        }
                        if (dealerError.isNotEmpty()) {
                            Text("❌ Dealer: $dealerError", color = Color.Red, style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(onClick = {
                            loadBrands()
                            loadDealerId()
                            Toast.makeText(context, "กำลังโหลดข้อมูลใหม่...", Toast.LENGTH_SHORT).show()
                        }) {
                            Text("🔄 โหลดข้อมูลใหม่")
                        }
                    }
                }
            }

            Text("Car Images (${imageUris.size})")

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                item {

                    Surface(
                        onClick = { launcher.launch("image/*") },
                        shape = RoundedCornerShape(8.dp),
                        color = Color.LightGray.copy(0.4f),
                        modifier = Modifier.size(100.dp)
                    ) {

                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Icon(Icons.Default.Add, null)
                            Text("เพิ่มรูป")

                        }
                    }
                }

                items(imageUris) { uri ->

                    Box(modifier = Modifier.size(100.dp)) {

                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        IconButton(
                            onClick = { imageUris = imageUris.filter { it != uri } },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Black.copy(0.5f)
                            ) {

                                Icon(
                                    Icons.Default.Close,
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = brandExpanded,
                onExpandedChange = { brandExpanded = !brandExpanded }
            ) {

                OutlinedTextField(
                    value = selectedBrand?.brand_name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Brand") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = brandExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = brandExpanded,
                    onDismissRequest = { brandExpanded = false }
                ) {

                    brands.forEach { brand ->

                        DropdownMenuItem(
                            text = { Text(brand.brand_name) },
                            onClick = {

                                selectedBrand = brand
                                brandExpanded = false

                            }
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text("Model") },
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = year,
                    onValueChange = { year = it },
                    label = { Text("Year") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = color,
                    onValueChange = { color = it },
                    label = { Text("Color") },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                ExposedDropdownMenuBox(
                    expanded = fuelExpanded,
                    onExpandedChange = { fuelExpanded = !fuelExpanded },
                    modifier = Modifier.weight(1f)
                ) {

                    OutlinedTextField(
                        value = fuel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fuel") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = fuelExpanded)
                        },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = fuelExpanded,
                        onDismissRequest = { fuelExpanded = false }
                    ) {

                        fuelList.forEach {

                            DropdownMenuItem(
                                text = { Text(it) },
                                onClick = {

                                    fuel = it
                                    fuelExpanded = false

                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = transExpanded,
                    onExpandedChange = { transExpanded = !transExpanded },
                    modifier = Modifier.weight(1f)
                ) {

                    OutlinedTextField(
                        value = transmission,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Trans.") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = transExpanded)
                        },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = transExpanded,
                        onDismissRequest = { transExpanded = false }
                    ) {

                        transList.forEach {

                            DropdownMenuItem(
                                text = { Text(it) },
                                onClick = {

                                    transmission = it
                                    transExpanded = false

                                }
                            )
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                OutlinedTextField(
                    value = mileage,
                    onValueChange = { mileage = it },
                    label = { Text("Mileage") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = engine,
                    onValueChange = { engine = it },
                    label = { Text("Engine") },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            Button(

                onClick = {

                    Log.d("AddCar", "========== ADD CAR BUTTON CLICKED ==========")
                    Log.d("AddCar", "dealerId: $dealerId")
                    Log.d("AddCar", "userId: $userId")
                    Log.d("AddCar", "selectedBrand: ${selectedBrand?.brand_name} (id: ${selectedBrand?.brand_id})")
                    Log.d("AddCar", "model: $model, year: $year, price: $price")
                    Log.d("AddCar", "color: $color, fuel: $fuel, trans: $transmission")
                    Log.d("AddCar", "mileage: $mileage, engine: $engine")
                    Log.d("AddCar", "images: ${imageUris.size}")
                    Log.d("AddCar", "brands loaded: ${brands.size}")

                    if (selectedBrand == null) {
                        val msg = if (brands.isEmpty()) {
                            "ยี่ห้อรถยังไม่โหลด (brands=${brands.size}) กรุณากดโหลดข้อมูลใหม่ด้านบน"
                        } else {
                            "กรุณาเลือกยี่ห้อรถจาก dropdown (มี ${brands.size} ยี่ห้อ)"
                        }
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        Log.e("AddCar", "❌ Validation failed: selectedBrand is null, brands.size=${brands.size}")
                        return@Button
                    }
                    if (model.isEmpty()) {
                        Toast.makeText(context, "กรุณากรอกรุ่นรถ", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (year.isEmpty()) {
                        Toast.makeText(context, "กรุณากรอกปี", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (dealerId == 0) {
                        Toast.makeText(context, "ไม่พบข้อมูล Dealer (dealerId=0, userId=$userId) กรุณา Login ใหม่หรือกดโหลดข้อมูลใหม่", Toast.LENGTH_LONG).show()
                        Log.e("AddCar", "❌ Validation failed: dealerId is 0")
                        return@Button
                    }

                    loading = true
                    Log.d("AddCar", "✅ Validation passed, sending request...")

                    val mediaType = "text/plain".toMediaType()

                    val parts = imageUris.mapIndexed { index, uri ->

                        val file = File(context.cacheDir, "img_$index.jpg")

                        context.contentResolver.openInputStream(uri)?.use { input ->
                            FileOutputStream(file).use { output ->
                                input.copyTo(output)
                            }
                        }

                        MultipartBody.Part.createFormData(
                            "car_images",
                            file.name,
                            file.asRequestBody("image/*".toMediaType())
                        )
                    }

                    Log.d("AddCar", "Sending: dealer_id=$dealerId, brand_id=${selectedBrand!!.brand_id}, model=$model, year=$year, price=$price, color=$color, fuel=$fuel, trans=$transmission, mileage=$mileage, engine=$engine, images=${parts.size}")

                    // ส่งค่า default สำหรับ field ตัวเลขที่ว่าง
                    val safePrice = price.ifEmpty { "0" }
                    val safeMileage = mileage.ifEmpty { "0" }
                    val safeEngine = engine.ifEmpty { "0" }

                    ApiClient.carAPI.addCar(

                        dealerId.toString().toRequestBody(mediaType),
                        selectedBrand!!.brand_id.toString().toRequestBody(mediaType),
                        model.toRequestBody(mediaType),
                        year.toRequestBody(mediaType),
                        safePrice.toRequestBody(mediaType),
                        color.toRequestBody(mediaType),
                        fuel.toRequestBody(mediaType),
                        transmission.toRequestBody(mediaType),
                        safeMileage.toRequestBody(mediaType),
                        safeEngine.toRequestBody(mediaType),
                        description.toRequestBody(mediaType),
                        parts

                    ).enqueue(object : Callback<Map<String, Any>> {

                        override fun onResponse(
                            call: Call<Map<String, Any>>,
                            response: Response<Map<String, Any>>
                        ) {

                            loading = false

                            if (response.isSuccessful) {
                                Log.d("AddCar", "✅ Car added successfully: ${response.body()}")
                                Toast.makeText(context, "เพิ่มรถสำเร็จ!", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()

                            } else {
                                val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                                Log.e("AddCar", "❌ Add car failed: ${response.code()} - $errorMsg")
                                Toast.makeText(context, "เพิ่มรถไม่สำเร็จ (${response.code()}): $errorMsg", Toast.LENGTH_LONG).show()
                            }
                        }

                        override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                            loading = false
                            Log.e("AddCar", "❌ Network error on add car: ${t.message}", t)
                            Toast.makeText(context, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
                        }
                    })
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),

                shape = RoundedCornerShape(12.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6750A4)
                )

            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Add Car")
                }
            }
        }
    }
}