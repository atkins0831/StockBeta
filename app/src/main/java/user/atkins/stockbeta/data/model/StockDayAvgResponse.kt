package user.atkins.stockbeta.data.model

import com.squareup.moshi.Json

data class StockDayAvgResponse(
    @Json(name = "Code") val code: String,
    @Json(name = "Name") val name: String,
    @Json(name = "Closing Price") val closingPrice: String?,
    @Json(name = "Monthly Average Price") val monthlyAveragePrice: String?
)
