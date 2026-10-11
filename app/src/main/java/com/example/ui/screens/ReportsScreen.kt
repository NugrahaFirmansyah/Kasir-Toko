package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MutationType
import com.example.data.model.StockMutation
import com.example.data.model.TransactionStatus
import com.example.ui.theme.KelontongAmber
import com.example.ui.theme.KelontongAmberContainer
import com.example.ui.theme.KelontongGreenContainer
import com.example.ui.theme.KelontongGreenDark
import com.example.ui.theme.KelontongOnAmberContainer
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.StockRed
import com.example.ui.theme.StockRedContainer
import com.example.ui.theme.StockSafeContainer
import com.example.ui.theme.StockSafeGreen
import com.example.ui.util.CurrencyFormatter
import com.example.ui.viewmodel.PosViewModel

@Composable
fun ReportsScreen(
    viewModel: PosViewModel,
    modifier: Modifier = Modifier
) {
    var selectedReportTab by remember { mutableIntStateOf(0) }
    val transactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val mutations by viewModel.allStockMutations.collectAsStateWithLifecycle()

    val validTrx = transactions.filter { it.status == TransactionStatus.SELESAI }
    val totalRevenue = validTrx.sumOf { it.finalAmount }
    val totalCost = validTrx.sumOf { it.totalCostAmount }
    val netProfit = (totalRevenue - totalCost).coerceAtLeast(0L)
    val totalTrxCount = validTrx.size

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedReportTab,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = selectedReportTab == 0,
                onClick = { selectedReportTab = 0 },
                text = { Text("Laba & Penjualan") },
                icon = { Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedReportTab == 1,
                onClick = { selectedReportTab = 1 },
                text = { Text("Audit Mutasi Stok") },
                icon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        when (selectedReportTab) {
            0 -> FinancialSummaryContent(
                totalRevenue = totalRevenue,
                totalCost = totalCost,
                netProfit = netProfit,
                totalTrxCount = totalTrxCount,
                mutations = mutations
            )
            1 -> StockMutationsAuditContent(mutations = mutations)
        }
    }
}

@Composable
fun FinancialSummaryContent(
    totalRevenue: Long,
    totalCost: Long,
    netProfit: Long,
    totalTrxCount: Int,
    mutations: List<StockMutation>
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "Ringkasan Kinerja Keuangan Toko",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Kalkulasi otomatis dari penjualan dan modal barang",
                style = MaterialTheme.typography.bodySmall,
                color = Slate500
            )
        }

        // Net Profit Highlight Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = KelontongGreenDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Laba Bersih (Keuntungan)",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp
                        )
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = CurrencyFormatter.formatRupiah(netProfit),
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Margin Keuntungan: ${(if (totalRevenue > 0) (netProfit * 100 / totalRevenue) else 0)}%",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Omzet vs Modal
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Paid, contentDescription = null, tint = KelontongGreenDark, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Total Omzet", fontSize = 11.sp, color = Slate500)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = CurrencyFormatter.formatRupiah(totalRevenue),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Slate800
                        )
                    }
                }

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = KelontongAmber, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Total Modal (HPP)", fontSize = 11.sp, color = Slate500)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = CurrencyFormatter.formatRupiah(totalCost),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Slate800
                        )
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Total Transaksi Sukses", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                    Text("$totalTrxCount Transaksi", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = KelontongGreenDark)
                }
            }
        }

        // Top Sold Items by Mutation Analysis
        item {
            Text(
                text = "Produk Paling Sering Terjual",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        val topSellingMap = mutations
            .filter { it.type == MutationType.PENJUALAN }
            .groupBy { it.productName }
            .mapValues { entry -> -entry.value.sumOf { it.changeQuantity } }
            .toList()
            .sortedByDescending { it.second }
            .take(5)

        if (topSellingMap.isEmpty()) {
            item {
                Surface(
                    color = Slate100,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Belum ada produk terjual untuk dianalisis",
                        fontSize = 12.sp,
                        color = Slate500,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(topSellingMap) { (productName, qty) ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(productName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Surface(
                            color = KelontongGreenContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Terjual: $qty",
                                color = KelontongGreenDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(70.dp))
        }
    }
}

@Composable
fun StockMutationsAuditContent(
    mutations: List<StockMutation>
) {
    var selectedFilter by remember { mutableStateOf("Semua") }
    val filterOptions = listOf("Semua", "Penjualan", "Restock", "Penyesuaian", "Batal Transaksi")

    val filtered = when (selectedFilter) {
        "Penjualan" -> mutations.filter { it.type == MutationType.PENJUALAN }
        "Restock" -> mutations.filter { it.type == MutationType.RESTOCK }
        "Penyesuaian" -> mutations.filter { it.type == MutationType.PENYESUAIAN }
        "Batal Transaksi" -> mutations.filter { it.type == MutationType.BATAL_TRANSAKSI }
        else -> mutations
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Filter Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filterOptions) { opt ->
                val isSelected = selectedFilter == opt
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) KelontongGreenDark else Slate100,
                    modifier = Modifier.clickable { selectedFilter = opt }
                ) {
                    Text(
                        text = opt,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else Slate700,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        if (filtered.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text("Tidak ada riwayat mutasi stok", color = Slate500, fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize().testTag("stock_mutation_list")
            ) {
                items(filtered, key = { it.id }) { mutation ->
                    StockMutationItemCard(mutation = mutation)
                }
            }
        }
    }
}

@Composable
fun StockMutationItemCard(mutation: StockMutation) {
    val (typeColor, typeBg, typeLabel) = when (mutation.type) {
        MutationType.PENJUALAN -> Triple(StockRed, StockRedContainer, "Penjualan Kasir")
        MutationType.RESTOCK -> Triple(StockSafeGreen, StockSafeContainer, "Restock (Kulakan)")
        MutationType.PENYESUAIAN -> Triple(KelontongAmber, KelontongAmberContainer, "Penyesuaian Opname")
        MutationType.BATAL_TRANSAKSI -> Triple(Color(0xFF0284C7), Color(0xFFE0F2FE), "Retur / Batal Transaksi")
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = typeBg, shape = RoundedCornerShape(4.dp)) {
                        Text(
                            text = typeLabel,
                            color = typeColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = CurrencyFormatter.formatDate(mutation.timestamp),
                        fontSize = 10.sp,
                        color = Slate500
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = mutation.productName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Slate800
                )

                if (mutation.note.isNotBlank()) {
                    Text(
                        text = mutation.note,
                        fontSize = 11.sp,
                        color = Slate500
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                val sign = if (mutation.changeQuantity > 0) "+${mutation.changeQuantity}" else "${mutation.changeQuantity}"
                Text(
                    text = sign,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (mutation.changeQuantity < 0) StockRed else StockSafeGreen
                )
                Text(
                    text = "${mutation.previousStock} → ${mutation.resultingStock}",
                    fontSize = 11.sp,
                    color = Slate500,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
