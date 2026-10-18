package user.atkins.stockbeta.data.api

import retrofit2.http.GET
import user.atkins.stockbeta.data.model.BwibbuResponse
import user.atkins.stockbeta.data.model.StockDayAllResponse
import user.atkins.stockbeta.data.model.StockDayAvgResponse

interface TwseApiService {
    // 1. 本益比、殖利率、股價淨值比
    @GET("exchangeReport/BWIBBU_ALL")
    suspend fun getBwibbuAll(): List<BwibbuResponse>

    // 2. 日收盤價及月平均價
    @GET("exchangeReport/STOCK_DAY_AVG_ALL")
    suspend fun getStockDayAvgAll(): List<StockDayAvgResponse>

    // 3. 日成交資訊
    @GET("exchangeReport/STOCK_DAY_ALL")
    suspend fun getStockDayAll(): List<StockDayAllResponse>
}