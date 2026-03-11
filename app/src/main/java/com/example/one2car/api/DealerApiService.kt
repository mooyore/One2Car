package com.example.one2car.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface DealerApiService {

    // ดึงข้อมูล Dealer จาก user_id
    @GET("dealer-info/{user_id}")
    suspend fun getDealerInfo(
        @Path("user_id") userId: Int
    ): DealerInfo

    // ดึงข้อมูล user ปกติ
    @GET("user-info/{user_id}")
    suspend fun getUserInfo(
        @Path("user_id") userId: Int
    ): UserInfo

    @Multipart
    @PUT("update-dealer/{dealer_id}")
    suspend fun updateDealer(
        @Path("dealer_id") dealerId: Int,
        @Part("dealer_name") dealerName: RequestBody?,
        @Part("address") address: RequestBody?,
        @Part("phone") phone: RequestBody?,
        @Part logoImage: MultipartBody.Part?
    ): Response<UpdateDealerResponse>

    // สมัครเป็น Dealer (Upgrade)
    @Multipart
    @POST("upgrade-to-dealer")
    suspend fun upgradeToDealer(
        @Part("user_id") userId: RequestBody,
        @Part("dealer_name") dealerName: RequestBody,
        @Part("address") address: RequestBody,
        @Part("phone") phone: RequestBody,
        @Part logoImage: MultipartBody.Part?
    ): UpgradeResponse

    // --- เพิ่มสำหรับ SearchCarScreen ---
    @GET("brands")
    suspend fun getBrands(): List<BrandInfo>

    @GET("models/{brand_id}")
    suspend fun getModels(@Path("brand_id") brandId: Int): List<CarModelInfo>
}
