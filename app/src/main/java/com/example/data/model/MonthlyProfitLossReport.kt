package com.example.data.model

data class MonthlyProfitLossReport(
    val yearMonthKey: String, // format: "yyyy-MM", contoh: "2026-10"
    val displayMonthYear: String, // format: "MMMM yyyy", contoh: "Oktober 2026"
    val totalRevenue: Long, // Total Penjualan (Omzet) dari transaksi SELESAI
    val totalCostOfGoodsSold: Long, // Total Modal Barang Terjual (HPP)
    val totalDiscount: Long, // Total Diskon yang diberikan
    val netProfit: Long, // Laba/Rugi Bersih = totalRevenue - totalCostOfGoodsSold
    val profitMarginPercent: Double, // Persentase margin laba
    val isProfit: Boolean, // true jika laba (>= 0), false jika rugi (< 0)
    val transactionCount: Int, // Total transaksi sukses
    val averageTransactionValue: Long, // Rata-rata nominal per transaksi
    val cashRevenue: Long, // Total omzet tunai
    val qrisRevenue: Long, // Total omzet QRIS
    val transferRevenue: Long, // Total omzet transfer bank
    val kasbonRevenue: Long, // Total omzet kasbon
    val transactions: List<TransactionRecord>,
    val itemSoldBreakdown: List<MonthlyItemSoldSummary> = emptyList()
)

data class MonthlyItemSoldSummary(
    val productId: Long,
    val productName: String,
    val unit: String,
    val totalQuantitySold: Int,
    val totalRevenue: Long,
    val totalCost: Long,
    val profit: Long
)
