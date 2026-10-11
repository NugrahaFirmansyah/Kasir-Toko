package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.KelontongGreenContainer
import com.example.ui.theme.KelontongGreenDark
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate700
import com.example.ui.theme.StockRed
import com.example.ui.theme.StockRedContainer
import com.example.ui.theme.StockSafeContainer
import com.example.ui.theme.StockSafeGreen
import com.example.ui.theme.StockWarningContainer
import com.example.ui.theme.StockWarningOrange

@Composable
fun StockBadge(
    currentStock: Int,
    minStockAlert: Int,
    unit: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label, showWarningIcon) = when {
        currentStock <= 0 -> {
            Quadruple(StockRedContainer, StockRed, "Stok Habis (0)", true)
        }
        currentStock <= minStockAlert -> {
            Quadruple(
                StockWarningContainer,
                StockWarningOrange,
                "Sisa $currentStock $unit (Menipis)",
                true
            )
        }
        else -> {
            Quadruple(
                StockSafeContainer,
                StockSafeGreen,
                "Stok: $currentStock $unit",
                false
            )
        }
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            if (showWarningIcon) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Peringatan Stok",
                    tint = textColor,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            } else {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(textColor, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun QuantityStepper(
    quantity: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    modifier: Modifier = Modifier,
    enabledIncrease: Boolean = true,
    testTagPrefix: String = "stepper"
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Slate200.copy(alpha = 0.6f),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            IconButton(
                onClick = onDecrease,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("${testTagPrefix}_decrease")
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Kurang",
                    tint = Slate700,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = "$quantity",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .testTag("${testTagPrefix}_qty_text")
            )

            IconButton(
                onClick = onIncrease,
                enabled = enabledIncrease,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("${testTagPrefix}_increase")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah",
                    tint = if (enabledIncrease) KelontongGreenDark else Slate200,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun MetricStatCard(
    title: String,
    value: String,
    subvalue: String? = null,
    icon: @Composable () -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = KelontongGreenContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    icon()
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            androidx.compose.foundation.layout.Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subvalue != null) {
                    Text(
                        text = subvalue,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
