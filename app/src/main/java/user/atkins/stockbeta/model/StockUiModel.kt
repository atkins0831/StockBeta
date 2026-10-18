package user.atkins.stockbeta.model

data class StockUiModel(
    val code: String,              // 股票代號 (如 "2330")
    val name: String,              // 股票名稱 (如 "台積電")
    val openPrice: String,         // 開盤價
    val closePrice: String,        // 收盤價
    val highestPrice: String,      // 最高價
    val lowestPrice: String,       // 最低價
    val change: String,            // 漲跌價差
    val monthlyAvgPrice: String,   // 月平均價
    val transaction: String,       // 成交筆數
    val tradeVolume: String,       // 成交股數
    val tradeValue: String,        // 成交金額
    val peRatio: String,           // 本益比 (Dialog 用)
    val dividendYield: String,     // 殖利率 (%) (Dialog 用)
    val pbRatio: String,           // 股價淨值比 (Dialog 用)
    val isPriceAboveAvg: Boolean,  // 收盤價 > 月平均價 (紅字判斷)
    val isChangePositive: Boolean  // 漲跌價差 > 0 (紅字判斷)
)
