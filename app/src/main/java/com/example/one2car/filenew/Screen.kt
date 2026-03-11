package com.example.one2car.filenew

sealed class Screen(val route:String){

    object HomeDealer:Screen("home_dealer")

    object AddCar:Screen("add_car")

    object EditCar:Screen("edit_car/{id}"){
        fun createRoute(id:Int)="edit_car/$id"
    }

    object CarDetail:Screen("car_detail/{id}"){
        fun createRoute(id:Int)="car_detail/$id"
    }
}
