import com.google.gson.annotations.SerializedName

data class Car(

    @SerializedName("car_id")
    val carId: Int,

    @SerializedName("brand")
    val brand: String,

    val model: String,

    val year: Int,

    val price: Int,

    val mileage: Int,

    val transmission: String,

    @SerializedName("fuel_type")
    val fuelType: String,

    val color: String,

    @SerializedName("engine_capacity")
    val engineCapacity: String,

    val description: String,

    @SerializedName("thumbnail_url")
    val thumbnailUrl: String?
)