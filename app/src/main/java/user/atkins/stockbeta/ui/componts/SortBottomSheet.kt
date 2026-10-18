package user.atkins.stockbeta.ui.componts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import user.atkins.stockbeta.ui.StockUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortBottomSheet(
    currentSortOrder: StockUiState.SortOrder,
    onSortSelected: (StockUiState.SortOrder) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, top = 8.dp)
        ) {
            Text(
                text = "排序方式",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            )

            HorizontalDivider()

            SortOptionRow(
                text = "依股票代號降序",
                isSelected = currentSortOrder == StockUiState.SortOrder.DESCENDING,
                onClick = { onSortSelected(StockUiState.SortOrder.DESCENDING) }
            )

            SortOptionRow(
                text = "依股票代號升序",
                isSelected = currentSortOrder == StockUiState.SortOrder.ASCENDING,
                onClick = { onSortSelected(StockUiState.SortOrder.ASCENDING) }
            )
        }
    }
}

@Composable
private fun SortOptionRow(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
        RadioButton(
            selected = isSelected,
            onClick = null // 由外層 Row 的 clickable 統一處理
        )
    }
}