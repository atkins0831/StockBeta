package user.atkins.stockbeta.ui

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import user.atkins.stockbeta.data.repository.StockRepository
import user.atkins.stockbeta.model.StockUiModel

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

class StockViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: StockRepository = mockk()

    private fun createDummyStock(code: String, name: String): StockUiModel {
        return StockUiModel(
            code = code,
            name = name,
            openPrice = "100.0",
            closePrice = "105.0",
            highestPrice = "110.0",
            lowestPrice = "98.0",
            change = "5.0",
            monthlyAvgPrice = "100.0",
            transaction = "1000",
            tradeVolume = "50000",
            tradeValue = "5250000",
            peRatio = "15.0",
            dividendYield = "3.5",
            pbRatio = "1.5",
            isPriceAboveAvg = true,
            isChangePositive = true
        )
    }

    @Test
    fun init_loadStockData_success_transitionsToSuccessAndSortsDescending() = runTest {
        // Arrange
        val stock2330 = createDummyStock("2330", "台積電")
        val stock2317 = createDummyStock("2317", "鴻海")
        val stock2454 = createDummyStock("2454", "聯發科")
        // Unsorted input list
        val rawList = listOf(stock2330, stock2317, stock2454)

        coEvery { repository.fetchCombinedStockData() } returns Result.success(rawList)

        // Act & Assert using Turbine
        val viewModel = StockViewModel(repository)

        viewModel.uiState.test {
            // Initial state is Loading
            assertEquals(StockUiState.Loading, awaitItem())

            // Process async loadStockData
            testScheduler.runCurrent()

            // Next state should be Success with stocks sorted in DESCENDING order (2454 -> 2330 -> 2317)
            val successState = awaitItem() as StockUiState.Success
            assertEquals(StockUiState.SortOrder.DESCENDING, successState.currentSortOrder)

            val sortedCodes = successState.stocks.map { it.code }
            assertEquals(listOf("2454", "2330", "2317"), sortedCodes)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateSortOrder_toAscending_sortsStocksAscending() = runTest {
        // Arrange
        val stock2330 = createDummyStock("2330", "台積電")
        val stock2317 = createDummyStock("2317", "鴻海")
        val stock2454 = createDummyStock("2454", "聯發科")
        val rawList = listOf(stock2330, stock2317, stock2454)

        coEvery { repository.fetchCombinedStockData() } returns Result.success(rawList)

        val viewModel = StockViewModel(repository)

        viewModel.uiState.test {
            assertEquals(StockUiState.Loading, awaitItem())
            testScheduler.runCurrent()

            // Initial success state (DESCENDING)
            val initialSuccess = awaitItem() as StockUiState.Success
            assertEquals(listOf("2454", "2330", "2317"), initialSuccess.stocks.map { it.code })

            // Act: Switch sort order to ASCENDING
            viewModel.updateSortOrder(StockUiState.SortOrder.ASCENDING)

            // Assert: Next state should be Success with ASCENDING order (2317 -> 2330 -> 2454)
            val ascendingSuccess = awaitItem() as StockUiState.Success
            assertEquals(StockUiState.SortOrder.ASCENDING, ascendingSuccess.currentSortOrder)
            assertEquals(listOf("2317", "2330", "2454"), ascendingSuccess.stocks.map { it.code })

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun init_loadStockData_failure_transitionsToErrorState() = runTest {
        // Arrange
        val errorMessage = "網路連線異常，請重新再試"
        coEvery { repository.fetchCombinedStockData() } returns Result.failure(RuntimeException(errorMessage))

        // Act & Assert
        val viewModel = StockViewModel(repository)

        viewModel.uiState.test {
            assertEquals(StockUiState.Loading, awaitItem())

            testScheduler.runCurrent()

            val errorState = awaitItem() as StockUiState.Error
            assertEquals(errorMessage, errorState.message)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
