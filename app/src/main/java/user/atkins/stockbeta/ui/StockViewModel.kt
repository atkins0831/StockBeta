package user.atkins.stockbeta.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import user.atkins.stockbeta.data.api.NetworkModule
import user.atkins.stockbeta.data.repository.StockRepository
import user.atkins.stockbeta.model.StockUiModel
import kotlin.onFailure

class StockViewModel @JvmOverloads constructor(
    private val repository: StockRepository = StockRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<StockUiState>(StockUiState.Loading)
    val uiState: StateFlow<StockUiState> = _uiState.asStateFlow()

    init {
        loadStockData()
    }

    fun loadStockData() {
        viewModelScope.launch {
            _uiState.value = StockUiState.Loading

            repository.fetchCombinedStockData()
                .onSuccess { rawList ->
                    // 題目規範：預設依股票代號降序排序
                    val sortedList = sortList(rawList, StockUiState.SortOrder.DESCENDING)
                    _uiState.value = StockUiState.Success(
                        stocks = sortedList,
                        currentSortOrder = StockUiState.SortOrder.DESCENDING
                    )
                }
                .onFailure { error ->
                    _uiState.value = StockUiState.Error(
                        message = error.localizedMessage ?: "發生未知錯誤，請重新試試"
                    )
                }
        }
    }

    // 更新排序方式 (降序 / 升序)
    fun updateSortOrder(sortOrder: StockUiState.SortOrder) {
        val currentState = _uiState.value
        if (currentState is StockUiState.Success) {
            val reSortedList = sortList(currentState.stocks, sortOrder)
            _uiState.value = currentState.copy(
                stocks = reSortedList,
                currentSortOrder = sortOrder
            )
        }
    }

    // 排序核心邏輯
    private fun sortList(list: List<StockUiModel>, order: StockUiState.SortOrder): List<StockUiModel> {
        return when (order) {
            StockUiState.SortOrder.DESCENDING -> list.sortedByDescending { it.code }
            StockUiState.SortOrder.ASCENDING -> list.sortedBy { it.code }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                StockViewModel(StockRepository(NetworkModule.apiService))
            }
        }
    }
}