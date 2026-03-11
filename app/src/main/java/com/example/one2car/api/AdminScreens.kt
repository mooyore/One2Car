package com.example.one2car.api

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMainScreen(userId: Int, onLogout: () -> Unit, viewModel: AdminViewModel = viewModel()) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Dashboard", "Users", "Cars", "Profile")

    val context = LocalContext.current
    val message by viewModel.message.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, "Error: $it", Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ยินดีต้อนรับสู่ผู้ดูแลระบบ", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        label = { Text(title) },
                        icon = {
                            Icon(
                                when (index) {
                                    0 -> Icons.Default.Dashboard
                                    1 -> Icons.Default.People
                                    2 -> Icons.Default.DirectionsCar
                                    else -> Icons.Default.Person
                                },
                                contentDescription = title
                            )
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> DashboardScreen(viewModel)
                1 -> UserManagementScreen(viewModel)
                2 -> CarManagementScreen(viewModel)
                3 -> ProfileScreen(viewModel, userId = userId, onLogout = onLogout)
            }
        }
    }
}

@Composable
fun DashboardScreen(viewModel: AdminViewModel) {
    val stats by viewModel.dashboardStats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showAddBrandDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchDashboardStats()
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("สรุปข้อมูลทั้งหมด", fontSize = 22.sp, fontWeight = FontWeight.Bold)

            stats?.let {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard("จำนวนลูกค้าทั้งหมด", it.totalUsers.toString(), Modifier.weight(1f), Color(0xFF2196F3), Icons.Default.Group)
                    StatCard("จำนวนผู้ขายรถ", it.totalDealers.toString(), Modifier.weight(1f), Color(0xFF4CAF50), Icons.Default.Store)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard("จำนวนรถทั้งหมด", it.totalCars.toString(), Modifier.weight(1f), Color(0xFFFF9800), Icons.Default.DirectionsCar)
                    StatCard("จำนวนรถที่ขายทั้งหมด", it.totalSold.toString(), Modifier.weight(1f), Color(0xFFF44336), Icons.Default.CheckCircle)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showAddBrandDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("เพิ่มยี่ห้อรถยนต์")
            }
        }
    }

    if (showAddBrandDialog) {
        var brandName by remember { mutableStateOf("") }
        var countryOrigin by remember { mutableStateOf("") }

        // 1. เพิ่มตัวแปรสำหรับเก็บ Uri ของรูปที่เลือก
        var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
        val context = LocalContext.current

        // 2. ตั้งค่า Photo Picker Launcher
        val photoPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
            contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia(),
            onResult = { uri -> selectedImageUri = uri }
        )

        AlertDialog(
            onDismissRequest = { showAddBrandDialog = false },
            title = { Text("เพิ่มยี่ห้อรถยนต์") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = brandName,
                        onValueChange = { brandName = it },
                        label = { Text("ชื่อยี่ห้อ") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // 3. ส่วนแสดงรูปภาพและปุ่มเลือกรูป
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (selectedImageUri != null) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .background(Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Image, contentDescription = null, tint = Color.Gray)
                            }
                        }

                        TextButton(onClick = {
                            photoPickerLauncher.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        }) {
                            Text(if (selectedImageUri == null) "เลือกรูปโลโก้" else "เปลี่ยนรูปภาพ")
                        }
                    }

                    OutlinedTextField(
                        value = countryOrigin,
                        onValueChange = { countryOrigin = it },
                        label = { Text("ประเทศต้นสังกัด") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (brandName.isNotBlank()) {
                        // 4. ส่ง context และ imageUri ไปที่ ViewModel ตัวใหม่ที่เราแก้ไว้
                        viewModel.addBrand(context, brandName, selectedImageUri, countryOrigin.ifBlank { null })
                        showAddBrandDialog = false
                    }
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBrandDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier, color: Color, icon: ImageVector) {
    Card(
        modifier = modifier.height(110.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = title, fontSize = 12.sp, color = color, fontWeight = FontWeight.Medium)
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun UserManagementScreen(viewModel: AdminViewModel) {
    val users by viewModel.users.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchUsers()
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(users) { user ->
                UserItem(user,
                    onChangeRole = { viewModel.changeUserRole(user.userId, it) },
                    onDelete = { viewModel.deleteUser(user.userId) }
                )
            }
        }
    }
}

@Composable
fun UserItem(user: AdminUser, onChangeRole: (String) -> Unit, onDelete: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRoleDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("ยืนยันการลบ") },
            text = { Text("คุณแน่ใจหรือไม่ว่าต้องการลบผู้ใช้ ${user.firstName} ${user.lastName}?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("ลบ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("ยกเลิก")
                }
            }
        )
    }

    if (showRoleDialog) {
        val newRole = if (user.role == "user") "dealer" else "user"
        AlertDialog(
            onDismissRequest = { showRoleDialog = false },
            title = { Text("ยืนยันการเปลี่ยนบทบาท") },
            text = { Text("คุณต้องการเปลี่ยนบทบาทของ ${user.firstName} เป็น ${newRole.uppercase()} ใช่หรือไม่?") },
            confirmButton = {
                Button(
                    onClick = {
                        onChangeRole(newRole)
                        showRoleDialog = false
                    }
                ) {
                    Text("ตกลง")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRoleDialog = false }) {
                    Text("ยกเลิก")
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("${user.firstName} ${user.lastName}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(user.email, fontSize = 13.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = if (user.role == "dealer") Color(0xFFE3F2FD) else Color(0xFFF5F5F5),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = user.role.uppercase(),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (user.role == "dealer") Color(0xFF1976D2) else Color.DarkGray
                    )
                }
            }
            Row {
                IconButton(onClick = { showRoleDialog = true }) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = "Change Role", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFE53935))
                }
            }
        }
    }
}

@Composable
fun CarManagementScreen(viewModel: AdminViewModel) {
    val cars by viewModel.cars.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchCars()
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(cars) { car ->
                CarItem(car, onDelete = { viewModel.deleteCar(car.carId) })
            }
        }
    }
}

@Composable
fun CarItem(car: AdminCar, onDelete: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("ยืนยันการลบ") },
            text = { Text("คุณแน่ใจหรือไม่ว่าต้องการลบรถ ${car.brand} ${car.model}?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("ลบ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("ยกเลิก")
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("${car.brand} ${car.model} (${car.year})", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Price: ${car.price} THB", color = Color(0xFF2E7D32), fontWeight = FontWeight.Medium)
                Text("Dealer: ${car.dealerName}", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                StatusBadge(status = car.status)
            }
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFE53935))
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = when (status) {
        "Sold", "Completed" -> Color(0xFF4CAF50)
        "Pending" -> Color(0xFFFF9800)
        "Cancelled" -> Color(0xFFF44336)
        else -> Color.Gray
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun ProfileScreen(viewModel: AdminViewModel, userId: Int, onLogout: () -> Unit) {
    val profile by viewModel.adminProfile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchAdminProfile(userId)
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        profile?.let { admin ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Image Design
                Box(contentAlignment = Alignment.BottomEnd) {
                    val imageUrl = if (!admin.profileImage.isNullOrEmpty()) {
                        "http://10.0.2.2:3000/uploads/profile_image/${admin.profileImage}"
                    } else {
                        null
                    }

                    if (imageUrl != null) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Admin Profile Picture",
                            modifier = Modifier
                                .size(130.dp)
                                .clip(CircleShape)
                                .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(130.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape,
                        modifier = Modifier.size(32.dp).offset(x = (-8).dp, y = (-8).dp)
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = Color.White, modifier = Modifier.padding(6.dp))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "${admin.firstName} ${admin.lastName}",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = admin.role.uppercase(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Modern Info List
                ProfileInfoItem(icon = Icons.Default.Email, label = "อีเมล", value = admin.email)
                ProfileInfoItem(icon = Icons.Default.Phone, label = "หมายเลขโทรศัพท์", value = admin.phone ?: "Not sent")
                ProfileInfoItem(icon = Icons.Default.AdminPanelSettings, label = "ระดับการเข้าถึง", value = "Full Access")

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ProfileInfoItem(icon: ImageVector, label: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(8.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = label, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
