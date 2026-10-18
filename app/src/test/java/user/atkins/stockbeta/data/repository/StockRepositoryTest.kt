package user.atkins.stockbeta.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import user.atkins.stockbeta.data.api.TwseApiService
import user.atkins.stockbeta.data.model.BwibbuResponse
import user.atkins.stockbeta.data.model.StockDayAllResponse
import user.atkins.stockbeta.data.model.StockDayAvgResponse

class StockRepositoryTest {

    private lateinit var apiService: TwseApiService
    private lateinit var repository: StockRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        apiService = mockk()
        repository = StockRepository(apiService, testDispatcher)
    }

    @Test
    fun fetchCombinedStockData_success_mergesDataCorrectlyByCode() = runTest(testDispatcher.scheduler) {
        // Arrange
        val code = "2330"
        val name = "台積電"

        val bwibbuList = listOf(
            BwibbuResponse(
                code = code,
                name = name,
                peRatio = "20.5",
                dividendYield = "2.1",
                pbRatio = "5.2"
            )
        )
        val avgList = listOf(
            StockDayAvgResponse(
                code = code,
                name = name,
                closingPrice = "600.00",
                monthlyAveragePrice = "580.00"
            )
        )
        val dayList = listOf(
            StockDayAllResponse(
                code = code,
                name = name,
                tradeVolume = "10000",
                tradeValue = "6000000",
                openingPrice = "590.00",
                highestPrice = "605.00",
                lowestPrice = "588.00",
                closingPrice = "600.00",
                change = "10.00",
                transaction = "5000"
            )
        )

        coEvery { apiService.getBwibbuAll() } returns bwibbuList
        coEvery { apiService.getStockDayAvgAll() } returns avgList
        coEvery { apiService.getStockDayAll() } returns dayList

        // Act
        val result = repository.fetchCombinedStockData()

        // Assert
        assertTrue(result.isSuccess)
        val stocks = result.getOrNull()
        assertEquals(1, stocks?.size)

        val stock = stocks!![0]
        assertEquals("2330", stock.code)
        assertEquals("台積電", stock.name)
        assertEquals("590.00", stock.openPrice)
        assertEquals("600.00", stock.closePrice)
        assertEquals("605.00", stock.highestPrice)
        assertEquals("588.00", stock.lowestPrice)
        assertEquals("10.00", stock.change)
        assertEquals("580.00", stock.monthlyAvgPrice)
        assertEquals("5000", stock.transaction)
        assertEquals("10000", stock.tradeVolume)
        assertEquals("6000000", stock.tradeValue)
        assertEquals("20.5", stock.peRatio)
        assertEquals("2.1", stock.dividendYield)
        assertEquals("5.2", stock.pbRatio)

        coVerify(exactly = 1) { apiService.getBwibbuAll() }
        coVerify(exactly = 1) { apiService.getStockDayAvgAll() }
        coVerify(exactly = 1) { apiService.getStockDayAll() }
    }

    @Test
    fun fetchCombinedStockData_businessLogic_isPriceAboveAvgAndIsChangePositive() = runTest(testDispatcher.scheduler) {
        // Arrange
        // Stock 1: close 105.0 > avg 100.0 (isPriceAboveAvg = true), change +5.0 > 0 (isChangePositive = true)
        // Stock 2: close 95.0 <= avg 100.0 (isPriceAboveAvg = false), change -5.0 <= 0 (isChangePositive = false)
        // Stock 3: close 100.0 == avg 100.0 (isPriceAboveAvg = false), change 0.0 <= 0 (isChangePositive = false)
        val bwibbuList = emptyList<BwibbuResponse>()
        val avgList = listOf(
            StockDayAvgResponse("0001", "Stock1", "105.0", "100.0"),
            StockDayAvgResponse("0002", "Stock2", "95.0", "100.0"),
            StockDayAvgResponse("0003", "Stock3", "100.0", "100.0")
        )
        val dayList = listOf(
            StockDayAllResponse("0001", "Stock1", null, null, null, null, null, "105.0", "+5.0", null),
            StockDayAllResponse("0002", "Stock2", null, null, null, null, null, "95.0", "-5.0", null),
            StockDayAllResponse("0003", "Stock3", null, null, null, null, null, "100.0", "0.00", null)
        )

        coEvery { apiService.getBwibbuAll() } returns bwibbuList
        coEvery { apiService.getStockDayAvgAll() } returns avgList
        coEvery { apiService.getStockDayAll() } returns dayList

        // Act
        val result = repository.fetchCombinedStockData()

        // Assert
        assertTrue(result.isSuccess)
        val stocks = result.getOrThrow()
        assertEquals(3, stocks.size)

        // Stock 1
        assertTrue(stocks[0].isPriceAboveAvg)
        assertTrue(stocks[0].isChangePositive)

        // Stock 2
        assertFalse(stocks[1].isPriceAboveAvg)
        assertFalse(stocks[1].isChangePositive)

        // Stock 3
        assertFalse(stocks[2].isPriceAboveAvg)
        assertFalse(stocks[2].isChangePositive)
    }

    @Test
    fun fetchCombinedStockData_dataCleaning_handlesCommasPlusSignsAndInvalidStrings() = runTest(testDispatcher.scheduler) {
        // Arrange
        // API returns numbers with commas ("1,234.00"), plus sign ("+50.00"), and invalid strings ("--")
        val bwibbuList = emptyList<BwibbuResponse>()
        val avgList = listOf(
            StockDayAvgResponse("0001", "Stock1", "1,234.00", "1,200.00"),
            StockDayAvgResponse("0002", "Stock2", "--", "--")
        )
        val dayList = listOf(
            StockDayAllResponse("0001", "Stock1", null, null, null, null, null, "1,234.00", "+50.00", null),
            StockDayAllResponse("0002", "Stock2", null, null, null, null, null, "--", "--", null)
        )

        coEvery { apiService.getBwibbuAll() } returns bwibbuList
        coEvery { apiService.getStockDayAvgAll() } returns avgList
        coEvery { apiService.getStockDayAll() } returns dayList

        // Act
        val result = repository.fetchCombinedStockData()

        // Assert
        assertTrue(result.isSuccess)
        val stocks = result.getOrThrow()
        assertEquals(2, stocks.size)

        // Stock 1: 1234.00 > 1200.00 -> isPriceAboveAvg = true, change 50.00 > 0 -> isChangePositive = true
        assertEquals("1,234.00", stocks[0].closePrice)
        assertEquals("1,200.00", stocks[0].monthlyAvgPrice)
        assertEquals("+50.00", stocks[0].change)
        assertTrue(stocks[0].isPriceAboveAvg)
        assertTrue(stocks[0].isChangePositive)

        // Stock 2: invalid strings "--" should fail parseToDouble() safely without crashing, default to 0.0
        assertEquals("--", stocks[1].closePrice)
        assertEquals("--", stocks[1].monthlyAvgPrice)
        assertEquals("--", stocks[1].change)
        assertFalse(stocks[1].isPriceAboveAvg)
        assertFalse(stocks[1].isChangePositive)
    }

    @Test
    fun fetchCombinedStockData_apiFailure_returnsFailureResult() = runTest(testDispatcher.scheduler) {
        // Arrange
        val exception = RuntimeException("Network error")
        coEvery { apiService.getBwibbuAll() } returns emptyList()
        coEvery { apiService.getStockDayAvgAll() } returns emptyList()
        coEvery { apiService.getStockDayAll() } throws exception

        // Act
        val result = repository.fetchCombinedStockData()

        // Assert
        assertTrue(result.isFailure)
        val actualException = result.exceptionOrNull()
        assertTrue(actualException is RuntimeException)
        assertEquals("Network error", actualException?.message)
    }
}
