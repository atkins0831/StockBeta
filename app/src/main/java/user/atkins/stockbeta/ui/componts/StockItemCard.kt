package user.atkins.stockbeta.ui.componts

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import user.atkins.stockbeta.model.StockUiModel
import user.atkins.stockbeta.ui.theme.StockGreen
import user.atkins.stockbeta.ui.theme.StockRed

@Composable
fun StockItemCard(
    stock: StockUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "CardScale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: 股票代號與名稱
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stock.code,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                )
                Text(
                    text = stock.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )
            }

            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

            // 格狀數據對照 (開盤 / 收盤)
            Row(modifier = Modifier.fillMaxWidth()) {
                GridLabelValue(
                    label = "開盤價",
                    value = stock.openPrice,
                    modifier = Modifier.weight(1f)
                )
                GridLabelValue(
                    label = "收盤價",
                    value = stock.closePrice,
                    valueColor = if (stock.isPriceAboveAvg) StockRed else StockGreen, // 高於月均價紅字，低於綠字
                    modifier = Modifier.weight(1f)
                )
            }

            // (最高 / 最低)
            Row(modifier = Modifier.fillMaxWidth()) {
                GridLabelValue(
                    label = "最高價",
                    value = stock.highestPrice,
                    modifier = Modifier.weight(1f)
                )
                GridLabelValue(
                    label = "最低價",
                    value = stock.lowestPrice,
                    modifier = Modifier.weight(1f)
                )
            }

            // (漲跌價差 / 月平均價)
            Row(modifier = Modifier.fillMaxWidth()) {
                GridLabelValue(
                    label = "漲跌價差",
                    value = stock.change,
                    valueColor = if (stock.isChangePositive) StockRed else StockGreen, // 正為紅，負為綠
                    modifier = Modifier.weight(1f)
                )
                GridLabelValue(
                    label = "月平均價",
                    value = stock.monthlyAvgPrice,
                    modifier = Modifier.weight(1f)
                )
            }

            // (成交筆數 / 成交股數)
            Row(modifier = Modifier.fillMaxWidth()) {
                GridLabelValue(
                    label = "成交筆數",
                    value = stock.transaction,
                    modifier = Modifier.weight(1f)
                )
                GridLabelValue(
                    label = "成交股數",
                    value = stock.tradeVolume,
                    modifier = Modifier.weight(1f)
                )
            }

            // 成交金額
            GridLabelValue(
                label = "成交金額",
                value = stock.tradeValue,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun GridLabelValue(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = modifier.padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(72.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = valueColor
        )
    }
}