package com.example.one2car.filenew

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun CarItem(
    car: Car,
    navController: NavController,
    viewModel: CarViewModel
){

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
    ){

        Column(
            modifier = Modifier.padding(10.dp)
        ){

            Text("${car.model}")

            Text("ราคา ${car.price}")

            Text("Status: ${car.status}")

            Row{

                Button(
                    onClick = {
                        navController.navigate("edit_car/${car.car_id}")
                    }
                ){
                    Text("แก้ไข")
                }

                Spacer(modifier = Modifier.width(10.dp))

                Button(
                    onClick = {
                        viewModel.deleteCar(car.car_id)
                    }
                ){
                    Text("ลบ")
                }

            }

        }

    }

}
