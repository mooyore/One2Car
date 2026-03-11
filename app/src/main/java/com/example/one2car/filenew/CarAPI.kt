package com.example.one2car.filenew


import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface CarAPI {
    @GET("cars")
    fun getCars(): Call<List<Car>>

    @GET("car-detail/{car_id}")
    fun getCarDetail(@Path("car_id") carId: Int): Call<Car>

    @GET("dealer-cars/{dealer_id}")
    fun getDealerCars(@Path("dealer_id") dealerId: Int): Call<List<Car>>

    @GET("brands")
    fun getBrands(): Call<List<Brand>>

    @GET("dealer-by-user/{user_id}")
    fun getDealerId(@Path("user_id") userId: Int): Call<Dealer>

    @Multipart
    @POST("add-car")
    fun addCar(
        @Part("dealer_id") dealerId: RequestBody,
        @Part("brand_id") brandId: RequestBody,
        @Part("model") model: RequestBody,
        @Part("year") year: RequestBody,
        @Part("price") price: RequestBody,
        @Part("color") color: RequestBody,
        @Part("fuel_type") fuelType: RequestBody,
        @Part("transmission") transmission: RequestBody,
        @Part("mileage") mileage: RequestBody,
        @Part("engine_capacity") engine: RequestBody,
        @Part("description") description: RequestBody,
        @Part car_images: List<MultipartBody.Part>
    ): Call<Map<String, Any>>

    @PUT("update-car/{car_id}")
    fun updateCar(
        @Path("car_id") carId: Int,
        @Body data: Map<String, @JvmSuppressWildcards Any?>
    ): Call<Map<String, Any>>

    @DELETE("delete-car/{car_id}")
    fun deleteCar(@Path("car_id") carId: Int): Call<Map<String, Any>>
}