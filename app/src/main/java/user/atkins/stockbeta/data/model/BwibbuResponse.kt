package user.atkins.stockbeta.data.model

import com.squareup.moshi.Json

data class BwibbuResponse(
    @Json(name = "Code") val code: String,
    @Json(name = "Name") val name: String,
    @Json(name = "PEratio") val peRatio: String?,
    @Json(name = "Dividend Yield") val dividendYield: String?,
    @Json(name = "PBratio") val pbRatio: String?)
