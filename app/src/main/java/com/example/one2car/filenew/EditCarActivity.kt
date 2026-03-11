package com.example.one2car.filenew

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCarScreen(navController: NavController, carId: Int) {

    val context = LocalContext.current

    var brands by remember { mutableStateOf<List<Brand>>(emptyList()) }
    var selectedBrand by remember { mutableStateOf<Brand?>(null) }
    var brandExpanded by remember { mutableStateOf(false) }

    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var fuel by remember { mutableStateOf("") }
    var transmission by remember { mutableStateOf("") }
    var mileage by remember { mutableStateOf("") }
    var engine by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }

    var carImages by remember { mutableStateOf<List<CarImage>>(emptyList()) }

    val fuelOptions = listOf("Gasoline","Diesel","Hybrid","EV")
    val transOptions = listOf("Auto", "Manual")

    var fuelExpanded by remember { mutableStateOf(false) }
    var transExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(carId) {

        ApiClient.carAPI.getBrands().enqueue(object: Callback<List<Brand>>{

            override fun onResponse(call: Call<List<Brand>>, response: Response<List<Brand>>) {

                brands = response.body() ?: emptyList()

                ApiClient.carAPI.getCarDetail(carId)
                    .enqueue(object: Callback<Car>{

                        override fun onResponse(call: Call<Car>, response: Response<Car>) {

                            response.body()?.let { car ->

                                model = car.model
                                year = car.year.toString()
                                price = car.price.toString()
                                color = car.color
                                fuel = car.fuel_type
                                transmission = car.transmission
                                mileage = car.mileage.toString()
                                engine = car.engine_capacity.toString()
                                description = car.description ?: ""
                                status = car.status ?: "Available"

                                carImages = car.images ?: emptyList()

                                selectedBrand =
                                    brands.find { it.brand_id == car.brand_id }

                            }

                        }

                        override fun onFailure(call: Call<Car>, t: Throwable) {}

                    })

            }

            override fun onFailure(call: Call<List<Brand>>, t: Throwable) {}

        })

    }

    Scaffold(

        topBar = {

            TopAppBar(

                title = { Text("Edit Car Detail") },

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

        },

        bottomBar = {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {

                Button(

                    onClick = {

                        val updateData = mapOf(

                            "brand_id" to (selectedBrand?.brand_id ?: 0),
                            "model" to model,
                            "year" to (year.toIntOrNull() ?: 0),
                            "price" to (price.toDoubleOrNull() ?: 0.0),
                            "color" to color,
                            "fuel_type" to fuel,
                            "transmission" to transmission,
                            "mileage" to (mileage.toIntOrNull() ?: 0),
                            "engine_capacity" to (engine.toDoubleOrNull() ?: 0.0),
                            "description" to description,
                            "status" to status

                        )

                        ApiClient.carAPI.updateCar(carId, updateData)
                            .enqueue(object: Callback<Map<String,Any>>{

                                override fun onResponse(
                                    call: Call<Map<String, Any>>,
                                    response: Response<Map<String, Any>>
                                ) {

                                    Toast.makeText(context,"Updated successfully",Toast.LENGTH_SHORT).show()
                                    navController.popBackStack()

                                }

                                override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {

                                    Toast.makeText(context,"Error ${t.message}",Toast.LENGTH_SHORT).show()

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

                ){

                    Text("Save Changes")

                }

            }

        }

    ){ padding ->

        Column(

            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),

            verticalArrangement = Arrangement.spacedBy(16.dp)

        ){

            Text("Car Images", style = MaterialTheme.typography.titleMedium)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)){

                Surface(
                    onClick = { },
                    shape = RoundedCornerShape(8.dp),
                    color = Color.LightGray.copy(.2f),
                    modifier = Modifier.size(100.dp)
                ){

                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ){

                        Icon(Icons.Default.Add,null)
                        Text("Add Photo")

                    }

                }

                carImages.forEach{ image ->

                    Box(modifier = Modifier.size(100.dp)){

                        AsyncImage(
                            model = image.url,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        IconButton(
                            onClick = { },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ){

                            Icon(Icons.Default.Close,null)

                        }

                    }

                }

            }

            Box{

                OutlinedTextField(
                    value = selectedBrand?.brand_name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Brand") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        Icon(Icons.Default.KeyboardArrowDown,null,
                            Modifier.clickable { brandExpanded = true })
                    }
                )

                DropdownMenu(
                    expanded = brandExpanded,
                    onDismissRequest = { brandExpanded = false }
                ){

                    brands.forEach{ b ->

                        DropdownMenuItem(
                            text = { Text(b.brand_name) },
                            onClick = {
                                selectedBrand = b
                                brandExpanded = false
                            }
                        )

                    }

                }

            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)){

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
                    modifier = Modifier.weight(1f)
                )

            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)){

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price") },
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = color,
                    onValueChange = { color = it },
                    label = { Text("Color") },
                    modifier = Modifier.weight(1f)
                )

            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)){

                Box(modifier = Modifier.weight(1f)){

                    OutlinedTextField(
                        value = fuel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fuel") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            Icon(Icons.Default.KeyboardArrowDown,null,
                                Modifier.clickable { fuelExpanded = true })
                        }
                    )

                    DropdownMenu(
                        expanded = fuelExpanded,
                        onDismissRequest = { fuelExpanded = false }
                    ){

                        fuelOptions.forEach{

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

                Box(modifier = Modifier.weight(1f)){

                    OutlinedTextField(
                        value = transmission,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Trans.") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            Icon(Icons.Default.KeyboardArrowDown,null,
                                Modifier.clickable { transExpanded = true })
                        }
                    )

                    DropdownMenu(
                        expanded = transExpanded,
                        onDismissRequest = { transExpanded = false }
                    ){

                        transOptions.forEach{

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

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)){

                OutlinedTextField(
                    value = mileage,
                    onValueChange = { mileage = it },
                    label = { Text("Mileage") },
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

            Spacer(modifier = Modifier.height(80.dp))

        }

    }

}