package user.atkins.stockbeta.ui

import user.atkins.stockbeta.model.StockUiModel

interface StockUiState {
    object Loading : StockUiState
    data class Success(
        val stocks: List<StockUiModel>,
        val currentSortOrder: SortOrder = SortOrder.DESCENDING // 預設降序
    ) : StockUiState
    data class Error(val message: String) : StockUiState

    enum class SortOrder {
        ASCENDING,  // 依股票代號升序
        DESCENDING  // 依股票代號降序
    }
}