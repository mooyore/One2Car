package com.example.one2car.filenew

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CarViewModel : ViewModel() {

    // list รถ
    var carList = mutableStateListOf<Car>()

    // =============================
    // โหลดรถของ dealer
    // =============================

    fun loadCars(dealerId: Int) {

        ApiClient.carAPI.getDealerCars(dealerId)
            .enqueue(object : Callback<List<Car>> {

                override fun onResponse(
                    call: Call<List<Car>>,
                    response: Response<List<Car>>
                ) {

                    if (response.isSuccessful) {

                        carList.clear()

                        response.body()?.let {
                            carList.addAll(it)
                        }

                    }

                }

                override fun onFailure(
                    call: Call<List<Car>>,
                    t: Throwable
                ) {
                    t.printStackTrace()
                }

            })

    }

    // =============================
    // ลบรถ
    // =============================

    fun deleteCar(carId: Int) {

        ApiClient.carAPI.deleteCar(carId)
            .enqueue(object : Callback<Map<String, Any>> {

                override fun onResponse(
                    call: Call<Map<String, Any>>,
                    response: Response<Map<String, Any>>
                ) {

                    if (response.isSuccessful) {

                        carList.removeIf { it.car_id == carId }

                    }

                }

                override fun onFailure(
                    call: Call<Map<String, Any>>,
                    t: Throwable
                ) {
                    t.printStackTrace()
                }

            })

    }

}