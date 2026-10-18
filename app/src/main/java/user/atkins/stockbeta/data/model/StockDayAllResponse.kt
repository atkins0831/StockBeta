package user.atkins.stockbeta.data.model

import com.squareup.moshi.Json

data class StockDayAllResponse(
    @Json(name = "Code") val code: String,
    @Json(name = "Name") val name: String,
    @Json(name = "TradeVolume") val tradeVolume: String?,
    @Json(name = "TradeValue") val tradeValue: String?,
    @Json(name = "OpeningPrice") val openingPrice: String?,
    @Json(name = "HighestPrice") val highestPrice: String?,
    @Json(name = "LowestPrice") val lowestPrice: String?,
    @Json(name = "ClosingPrice") val closingPrice: String?,
    @Json(name = "Change") val change: String?,
    @Json(name = "Transaction") val transaction: String?
)
