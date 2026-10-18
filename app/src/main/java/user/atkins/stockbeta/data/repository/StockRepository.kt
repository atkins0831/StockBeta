package user.atkins.stockbeta.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import user.atkins.stockbeta.model.StockUiModel
import user.atkins.stockbeta.data.api.NetworkModule
import user.atkins.stockbeta.data.api.TwseApiService

class StockRepository @JvmOverloads constructor(
    private val apiService: TwseApiService = NetworkModule.apiService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun fetchCombinedStockData(): Result<List<StockUiModel>> = withContext(ioDispatcher) {
        runCatching {
            coroutineScope {
                // 平行發起請求，縮短整體載入時間
                val bwibbuDeferred = async { apiService.getBwibbuAll() }
                val avgDeferred = async { apiService.getStockDayAvgAll() }
                val dayDeferred = async { apiService.getStockDayAll() }

                val bwibbuList = bwibbuDeferred.await()
                val avgList = avgDeferred.await()
                val dayList = dayDeferred.await()

                // 建立 Map 以便透過 Code 快速查找
                val bwibbuMap = bwibbuList.associateBy { it.code }
                val avgMap = avgList.associateBy { it.code }

                // 以日成交資訊為主體進行整合
                dayList.map { dayItem ->
                    val code = dayItem.code
                    val bwibbuItem = bwibbuMap[code]
                    val avgItem = avgMap[code]

                    val closePriceVal = dayItem.closingPrice?.parseToDouble() ?: 0.0
                    val monthlyAvgVal = avgItem?.monthlyAveragePrice?.parseToDouble() ?: 0.0
                    val changeVal = dayItem.change?.parseToDouble() ?: 0.0

                    StockUiModel(
                        code = code,
                        name = dayItem.name,
                        openPrice = dayItem.openingPrice ?: "-",
                        closePrice = dayItem.closingPrice ?: "-",
                        highestPrice = dayItem.highestPrice ?: "-",
                        lowestPrice = dayItem.lowestPrice ?: "-",
                        change = dayItem.change ?: "-",
                        monthlyAvgPrice = avgItem?.monthlyAveragePrice ?: "-",
                        transaction = dayItem.transaction ?: "-",
                        tradeVolume = dayItem.tradeVolume ?: "-",
                        tradeValue = dayItem.tradeValue ?: "-",
                        peRatio = bwibbuItem?.peRatio ?: "-",
                        dividendYield = bwibbuItem?.dividendYield ?: "-",
                        pbRatio = bwibbuItem?.pbRatio ?: "-",
                        isPriceAboveAvg = closePriceVal > monthlyAvgVal,
                        isChangePositive = changeVal > 0.0
                    )
                }
            }
        }
    }

    // 字串解析工具函式 (去除逗號並防錯)
    private fun String.parseToDouble(): Double? {
        return this.replace(",", "").replace("+", "").trim().toDoubleOrNull()
    }
}