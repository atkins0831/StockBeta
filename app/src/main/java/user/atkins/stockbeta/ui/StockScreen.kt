package user.atkins.stockbeta.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import user.atkins.stockbeta.model.StockUiModel
import user.atkins.stockbeta.ui.componts.SortBottomSheet
import user.atkins.stockbeta.ui.componts.StockDetailDialog
import user.atkins.stockbeta.ui.componts.StockItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockScreen(viewModel: StockViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = StockViewModel.Factory)) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedStockForDialog by remember { mutableStateOf<StockUiModel?>(null) }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("臺灣證券交易所上市個股") },
                actions = {
                    IconButton(onClick = { showBottomSheet = true }) {
                        Icon(imageVector = Icons.Default.FilterList, contentDescription = "Filter")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is StockUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is StockUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadStockData() }) {
                            Text("重試")
                        }
                    }
                }

                is StockUiState.Success -> {
                    if (isLandscape) {
                        // 橫向佈局：雙欄 Grid
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.stocks, key = { it.code }) { stock ->
                                StockItemCard(
                                    stock = stock,
                                    onClick = { selectedStockForDialog = stock }
                                )
                            }
                        }
                    } else {
                        // 直向佈局：單欄 List + 動畫
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.stocks, key = { it.code }) { stock ->
                                StockItemCard(
                                    stock = stock,
                                    onClick = { selectedStockForDialog = stock },
                                    modifier = Modifier.animateItem() // 排序切換時的位移動畫
                                )
                            }
                        }
                    }

                    // 排序 BottomSheet
                    if (showBottomSheet) {
                        SortBottomSheet(
                            currentSortOrder = state.currentSortOrder,
                            onSortSelected = { newOrder ->
                                viewModel.updateSortOrder(newOrder)
                                showBottomSheet = false
                            },
                            onDismiss = { showBottomSheet = false }
                        )
                    }

                    // Alert Dialog 彈窗
                    selectedStockForDialog?.let { stock ->
                        StockDetailDialog(
                            stock = stock,
                            onDismiss = { selectedStockForDialog = null }
                        )
                    }
                }
            }
        }
    }
}