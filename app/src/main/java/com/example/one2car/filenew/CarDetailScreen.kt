package com.example.one2car.filenew
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarDetailScreen(
    navController: NavController,
    id: Int
) {

    val context = LocalContext.current

    var car by remember { mutableStateOf<Car?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {

        ApiClient.carAPI.getCarDetail(id)
            .enqueue(object : Callback<Car> {

                override fun onResponse(
                    call: Call<Car>,
                    response: Response<Car>
                ) {
                    car = response.body()
                    loading = false
                }

                override fun onFailure(
                    call: Call<Car>,
                    t: Throwable
                ) {
                    loading = false
                    Toast.makeText(
                        context,
                        "Failed to load car",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            })
    }

    Scaffold(

        topBar = {

            TopAppBar(

                title = { Text("Car Details") },

                navigationIcon = {

                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {

                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )

                    }

                }

            )

        }

    ) { padding ->

        if (loading) {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

        } else {

            car?.let { c ->

                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {

                    // ==========================
                    // IMAGE SLIDER
                    // ==========================

                    val images = c.images ?: emptyList()

                    if (images.isNotEmpty()) {

                        val pagerState = rememberPagerState(
                            pageCount = { images.size }
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                        ) {

                            HorizontalPager(
                                state = pagerState
                            ) { page ->

                                AsyncImage(
                                    model = images[page].url,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                            }

                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(12.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Black.copy(alpha = 0.6f)
                            ) {

                                Text(
                                    text = "${pagerState.currentPage + 1} / ${images.size}",
                                    color = Color.White,
                                    modifier = Modifier.padding(
                                        horizontal = 10.dp,
                                        vertical = 4.dp
                                    )
                                )

                            }

                        }

                    }

                    // ==========================
                    // CAR INFO
                    // ==========================

                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {

                        Text(
                            text = "${c.brand_name} ${c.model}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "฿ ${c.price}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        DetailItem("Year", c.year.toString())
                        DetailItem("Color", c.color)
                        DetailItem("Fuel Type", c.fuel_type)
                        DetailItem("Transmission", c.transmission)
                        DetailItem("Mileage", "${c.mileage} km")
                        DetailItem("Engine", "${c.engine_capacity} cc")

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = c.description ?: "No description",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )

                    }

                }

            }

        }

    }

}

@Composable
fun DetailItem(
    label: String,
    value: String
) {

    Column {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),

            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = label,
                color = Color.Gray
            )

            Text(
                text = value,
                fontWeight = FontWeight.Medium
            )

        }

        HorizontalDivider(
            color = Color.LightGray.copy(alpha = 0.5f)
        )

    }

}