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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MonthlyItemSoldSummary
import com.example.data.model.MonthlyProfitLossReport
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
import java.util.Locale

@Composable
fun ReportsScreen(
    viewModel: PosViewModel,
    modifier: Modifier = Modifier
) {
    var selectedReportTab by remember { mutableIntStateOf(0) }
    val mutations by viewModel.allStockMutations.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedReportTab,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = selectedReportTab == 0,
                onClick = { selectedReportTab = 0 },
                text = { Text("Laba Rugi Bulanan") },
                icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_monthly_report")
            )
            Tab(
                selected = selectedReportTab == 1,
                onClick = { selectedReportTab = 1 },
                text = { Text("Ringkasan Umum") },
                icon = { Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_general_report")
            )
            Tab(
                selected = selectedReportTab == 2,
                onClick = { selectedReportTab = 2 },
                text = { Text("Audit Mutasi Stok") },
                icon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_mutation_report")
            )
        }

        when (selectedReportTab) {
            0 -> MonthlyProfitLossContent(viewModel = viewModel)
            1 -> OverallFinancialContent(viewModel = viewModel)
            2 -> StockMutationsAuditContent(mutations = mutations)
        }
    }
}

/**
 * TAB 1: LAPORAN LABA RUGI BULANAN
 * Menghitung Total Penjualan dikurangi Total Modal Barang Terjual menggunakan data transaksi tersimpan
 */
@Composable
fun MonthlyProfitLossContent(
    viewModel: PosViewModel
) {
    val context = LocalContext.current
    val monthlyReport by viewModel.monthlyProfitLossReport.collectAsStateWithLifecycle()
    val selectedMonthKey by viewModel.selectedReportMonth.collectAsStateWithLifecycle()
    val availableMonths by viewModel.availableReportMonths.collectAsStateWithLifecycle()
    val allMonthlyReports by viewModel.allMonthlyReports.collectAsStateWithLifecycle()

    val currentMonthKey = remember { CurrencyFormatter.formatYearMonthKey(System.currentTimeMillis()) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("monthly_profit_loss_screen")
    ) {
        // Header & Selector Bulan
        item {
            Column {
                Text(
                    text = "Laporan Laba Rugi Bulanan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Perhitungan laba bersih otomatis: Penjualan dikurangi modal barang terjual (HPP)",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Selector Tab Bulan
                Text(
                    text = "Pilih Periode Bulan:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate700
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(availableMonths) { (key, display) ->
                        val isSelected = selectedMonthKey == key
                        val isCurrent = key == currentMonthKey

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) KelontongGreenDark else Slate100,
                            modifier = Modifier
                                .clickable { viewModel.selectReportMonth(key) }
                                .testTag("select_month_$key")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = display,
                                    color = if (isSelected) Color.White else Slate700,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                                if (isCurrent) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = if (isSelected) Color.White.copy(alpha = 0.25f) else KelontongGreenContainer,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "Bulan Ini",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else KelontongGreenDark,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Hero Card: Laba / Rugi Bersih Utama
        item {
            val isProfit = monthlyReport.isProfit
            val heroBgColor = if (isProfit) KelontongGreenDark else StockRed
            val statusLabel = if (isProfit) "LABA BERSIH BULANAN" else "RUGI BERSIH BULANAN"
            val prefix = if (isProfit) "+" else ""

            Card(
                colors = CardDefaults.cardColors(containerColor = heroBgColor),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth().testTag("hero_net_profit_card")
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = statusLabel,
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Periode: ${monthlyReport.displayMonthYear}",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isProfit) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "$prefix${CurrencyFormatter.formatRupiah(monthlyReport.netProfit)}",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isProfit) "Keuntungan bersih setelah dikurangi seluruh modal barang" else "Modal barang terjual melebihi total penjualan",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            modifier = Modifier.weight(1f)
                        )

                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Margin: ${String.format(Locale.US, "%.1f", monthlyReport.profitMarginPercent)}%",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Kalkulasi Rinci Laba Rugi (Rumus Penjualan - Modal)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = null,
                            tint = KelontongGreenDark,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Rincian Rumus Laba Rugi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Text(
                        text = "Formula: Total Penjualan (Omzet) - Total Modal Barang Terjual (HPP)",
                        fontSize = 11.sp,
                        color = Slate500,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    // 1. Total Penjualan
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text("1. Total Penjualan (Omzet)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Total pendapatan kotor transaksi selesai", fontSize = 11.sp, color = Slate500)
                        }
                        Text(
                            text = "+${CurrencyFormatter.formatRupiah(monthlyReport.totalRevenue)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = KelontongGreenDark
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. Total Modal Barang Terjual
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text("2. Modal Barang Terjual (HPP)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Biaya kulakan dari barang yang berhasil dijual", fontSize = 11.sp, color = Slate500)
                        }
                        Text(
                            text = "-${CurrencyFormatter.formatRupiah(monthlyReport.totalCostOfGoodsSold)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = StockRed
                        )
                    }

                    if (monthlyReport.totalDiscount > 0) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text("3. Diskon yang Diberikan", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                Text("Potongan harga kasir kepada pelanggan", fontSize = 11.sp, color = Slate500)
                            }
                            Text(
                                text = "-${CurrencyFormatter.formatRupiah(monthlyReport.totalDiscount)}",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = StockRed
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Slate200)

                    // HASIL AKHIR: Laba Bersih
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = if (monthlyReport.isProfit) "HASIL: LABA BERSIH" else "HASIL: RUGI BERSIH",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = if (monthlyReport.isProfit) KelontongGreenDark else StockRed
                            )
                            Text(
                                text = "Penjualan dikurangi modal barang",
                                fontSize = 11.sp,
                                color = Slate500
                            )
                        }
                        val prefix = if (monthlyReport.isProfit) "+" else ""
                        Text(
                            text = "$prefix${CurrencyFormatter.formatRupiah(monthlyReport.netProfit)}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = if (monthlyReport.isProfit) KelontongGreenDark else StockRed
                        )
                    }
                }
            }
        }

        // 4 Kartu Metrik Ringkas
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MetricMiniCard(
                        title = "Total Penjualan",
                        value = CurrencyFormatter.formatRupiah(monthlyReport.totalRevenue),
                        icon = Icons.Default.Paid,
                        tint = KelontongGreenDark,
                        bgColor = KelontongGreenContainer,
                        modifier = Modifier.weight(1f)
                    )
                    MetricMiniCard(
                        title = "Modal Barang (HPP)",
                        value = CurrencyFormatter.formatRupiah(monthlyReport.totalCostOfGoodsSold),
                        icon = Icons.Default.ShoppingCart,
                        tint = StockRed,
                        bgColor = StockRedContainer,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MetricMiniCard(
                        title = "Transaksi Selesai",
                        value = "${monthlyReport.transactionCount} Transaksi",
                        icon = Icons.Default.ReceiptLong,
                        tint = Slate700,
                        bgColor = Slate100,
                        modifier = Modifier.weight(1f)
                    )
                    MetricMiniCard(
                        title = "Rata-rata per Transaksi",
                        value = CurrencyFormatter.formatRupiah(monthlyReport.averageTransactionValue),
                        icon = Icons.Default.Assessment,
                        tint = KelontongAmber,
                        bgColor = KelontongAmberContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Rincian Metode Pembayaran
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Rincian Berdasarkan Metode Bayar (${monthlyReport.displayMonthYear})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    PaymentMethodRow(name = "Tunai (Cash)", amount = monthlyReport.cashRevenue, color = KelontongGreenDark)
                    PaymentMethodRow(name = "QRIS", amount = monthlyReport.qrisRevenue, color = Color(0xFF0284C7))
                    PaymentMethodRow(name = "Transfer Bank", amount = monthlyReport.transferRevenue, color = Color(0xFF7C3AED))
                    PaymentMethodRow(name = "Kasbon Pelanggan", amount = monthlyReport.kasbonRevenue, color = KelontongAmber)
                }
            }
        }

        // Rincian Produk Terjual di Bulan Tersebut (Item Sold Breakdown)
        item {
            Text(
                text = "Rincian Barang Terjual & Keuntungan Produk (${monthlyReport.displayMonthYear})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (monthlyReport.itemSoldBreakdown.isEmpty()) {
            item {
                Surface(
                    color = Slate100,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Belum ada transaksi barang terjual di ${monthlyReport.displayMonthYear}",
                            fontSize = 13.sp,
                            color = Slate500,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(monthlyReport.itemSoldBreakdown) { itemSummary ->
                ProductSoldReportCard(item = itemSummary)
            }
        }

        // Riwayat / Perbandingan Laba Rugi Semua Bulan
        if (allMonthlyReports.size > 1) {
            item {
                Text(
                    text = "Riwayat Laba Rugi Antar Bulan (Tren Kinerja)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            items(allMonthlyReports) { report ->
                MonthlyComparisonCard(
                    report = report,
                    isSelected = report.yearMonthKey == selectedMonthKey,
                    onSelect = { viewModel.selectReportMonth(report.yearMonthKey) }
                )
            }
        }

        // Tombol Bagikan Laporan Bulanan
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { viewModel.shareMonthlyReport(context, monthlyReport) },
                colors = ButtonDefaults.buttonColors(containerColor = KelontongGreenDark),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_share_monthly_report")
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Bagikan Laporan Laba Rugi (${monthlyReport.displayMonthYear})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
fun MetricMiniCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = bgColor,
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = title, fontSize = 11.sp, color = Slate500)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Slate800
            )
        }
    }
}

@Composable
fun PaymentMethodRow(name: String, amount: Long, color: Color) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = name, fontSize = 12.sp, color = Slate700)
        }
        Text(
            text = CurrencyFormatter.formatRupiah(amount),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Slate800
        )
    }
}

@Composable
fun ProductSoldReportCard(item: MonthlyItemSoldSummary) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = item.productName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Slate800,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = KelontongGreenContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "Terjual: ${item.totalQuantitySold} ${item.unit}",
                        color = KelontongGreenDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text("Total Penjualan", fontSize = 10.sp, color = Slate500)
                    Text(
                        CurrencyFormatter.formatRupiah(item.totalRevenue),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Slate800
                    )
                }
                Column {
                    Text("Modal Barang (HPP)", fontSize = 10.sp, color = Slate500)
                    Text(
                        CurrencyFormatter.formatRupiah(item.totalCost),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = StockRed
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Laba Bersih Produk", fontSize = 10.sp, color = Slate500)
                    val prefix = if (item.profit >= 0) "+" else ""
                    Text(
                        text = "$prefix${CurrencyFormatter.formatRupiah(item.profit)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.profit >= 0) KelontongGreenDark else StockRed
                    )
                }
            }
        }
    }
}

@Composable
fun MonthlyComparisonCard(
    report: MonthlyProfitLossReport,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) KelontongGreenContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(14.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = report.displayMonthYear,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Slate800
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(color = KelontongGreenDark, shape = RoundedCornerShape(4.dp)) {
                            Text(
                                text = "Aktif",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Text(
                    text = "Omzet: ${CurrencyFormatter.formatRupiah(report.totalRevenue)} | Modal: ${CurrencyFormatter.formatRupiah(report.totalCostOfGoodsSold)}",
                    fontSize = 11.sp,
                    color = Slate500
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                val prefix = if (report.netProfit >= 0) "+" else ""
                Text(
                    text = "$prefix${CurrencyFormatter.formatRupiah(report.netProfit)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (report.isProfit) KelontongGreenDark else StockRed
                )
                Text(
                    text = "${report.transactionCount} Transaksi",
                    fontSize = 10.sp,
                    color = Slate500
                )
            }
        }
    }
}

/**
 * TAB 2: RINGKASAN KEUANGAN UMUM (ALL-TIME)
 */
@Composable
fun OverallFinancialContent(
    viewModel: PosViewModel
) {
    val transactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val mutations by viewModel.allStockMutations.collectAsStateWithLifecycle()

    val validTrx = transactions.filter { it.status == TransactionStatus.SELESAI }
    val totalRevenue = validTrx.sumOf { it.finalAmount }
    val totalCost = validTrx.sumOf { it.totalCostAmount }
    val netProfit = totalRevenue - totalCost
    val totalTrxCount = validTrx.size

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "Ringkasan Keuangan Keseluruhan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Total akumulasi omzet, modal, dan laba sejak toko beroperasi",
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
                            text = "Total Akumulasi Laba Bersih",
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
                        text = "Margin Keuntungan Akumulasi: ${(if (totalRevenue > 0) (netProfit * 100 / totalRevenue) else 0)}%",
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
                    Text("Total Seluruh Transaksi Sukses", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                    Text("$totalTrxCount Transaksi", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = KelontongGreenDark)
                }
            }
        }

        // Top Sold Items by Mutation Analysis
        item {
            Text(
                text = "Produk Paling Laris (Semua Waktu)",
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

/**
 * TAB 3: AUDIT MUTASI STOK BARANG
 */
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
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("stock_mutation_list")
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
