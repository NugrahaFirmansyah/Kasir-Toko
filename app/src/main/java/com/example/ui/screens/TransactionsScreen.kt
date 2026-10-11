package com.example.ui.screens

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PaymentMethod
import com.example.data.model.TransactionRecord
import com.example.data.model.TransactionStatus
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
fun TransactionsScreen(
    viewModel: PosViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val transactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val detailTrx by viewModel.viewingTransactionDetail.collectAsStateWithLifecycle()
    val detailItems by viewModel.transactionItemsForDetail.collectAsStateWithLifecycle()

    var showVoidConfirm by remember { mutableStateOf(false) }
    var voidReason by remember { mutableStateOf("Permintaan pelanggan / salah input") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text(
                    text = "Riwayat Transaksi Penjualan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Total ${transactions.size} transaksi tercatat",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (transactions.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = Slate500,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Belum ada transaksi penjualan",
                        fontWeight = FontWeight.SemiBold,
                        color = Slate700
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("transactions_list")
            ) {
                items(transactions, key = { it.id }) { trx ->
                    TransactionItemCard(
                        transaction = trx,
                        onClick = { viewModel.openTransactionDetail(trx) }
                    )
                }
            }
        }
    }

    // Detail Transaksi Modal
    if (detailTrx != null) {
        val currentTrx = detailTrx!!
        AlertDialog(
            onDismissRequest = { viewModel.closeTransactionDetail() },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Detail Transaksi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    if (currentTrx.status == TransactionStatus.DIBATALKAN) {
                        Surface(
                            color = StockRedContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "DIBATALKAN",
                                color = StockRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Surface(
                            color = StockSafeContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "SELESAI",
                                color = StockSafeGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "No. Invoice: ${currentTrx.invoiceNumber}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Waktu: ${CurrencyFormatter.formatDate(currentTrx.timestamp)}",
                        fontSize = 12.sp,
                        color = Slate500
                    )
                    if (currentTrx.customerName.isNotBlank()) {
                        Text(
                            text = "Pelanggan: ${currentTrx.customerName}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = "Metode: ${currentTrx.paymentMethod.name}",
                        fontSize = 12.sp,
                        color = KelontongGreenDark,
                        fontWeight = FontWeight.SemiBold
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    Text(
                        text = "Daftar Barang:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    detailItems.forEach { item ->
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.productName, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text(
                                    "${item.quantity} ${item.unit} x ${CurrencyFormatter.formatRupiah(item.sellingPrice)}",
                                    fontSize = 11.sp,
                                    color = Slate500
                                )
                            }
                            Text(
                                text = CurrencyFormatter.formatRupiah(item.subtotal),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Subtotal", fontSize = 12.sp, color = Slate500)
                        Text(CurrencyFormatter.formatRupiah(currentTrx.subtotalAmount), fontSize = 12.sp)
                    }

                    if (currentTrx.discountAmount > 0) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
                        ) {
                            Text("Diskon", fontSize = 12.sp, color = StockRed)
                            Text("-${CurrencyFormatter.formatRupiah(currentTrx.discountAmount)}", fontSize = 12.sp, color = StockRed)
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        Text("Total Pembayaran", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            CurrencyFormatter.formatRupiah(currentTrx.finalAmount),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = KelontongGreenDark
                        )
                    }

                    val estimatedProfit = currentTrx.finalAmount - currentTrx.totalCostAmount
                    Surface(
                        color = KelontongGreenContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text("Perkiraan Laba Bersih:", fontSize = 11.sp, color = KelontongGreenDark)
                            Text(
                                "+${CurrencyFormatter.formatRupiah(estimatedProfit)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = KelontongGreenDark
                            )
                        }
                    }

                    if (currentTrx.paymentMethod == PaymentMethod.QRIS || currentTrx.paymentMethod == PaymentMethod.TRANSFER) {
                        Surface(
                            color = StockSafeContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = StockSafeGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Bukti Pembayaran Terverifikasi Sesuai",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StockSafeGreen
                                    )
                                }
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                ) {
                                    Text("Nominal Bukti:", fontSize = 11.sp, color = Slate700)
                                    Text(
                                        CurrencyFormatter.formatRupiah(currentTrx.paymentProofNominal),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StockSafeGreen
                                    )
                                }
                                if (currentTrx.paymentProofRef.isNotBlank()) {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
                                    ) {
                                        Text("No. Referensi:", fontSize = 11.sp, color = Slate700)
                                        Text(currentTrx.paymentProofRef, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            viewModel.shareReceipt(context, currentTrx, detailItems)
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Bagikan")
                    }

                    if (currentTrx.status == TransactionStatus.SELESAI) {
                        Button(
                            onClick = { showVoidConfirm = true },
                            colors = ButtonDefaults.buttonColors(containerColor = StockRed)
                        ) {
                            Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Batal (Retur Stok)")
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeTransactionDetail() }) {
                    Text("Tutup")
                }
            }
        )
    }

    // Void Confirm Dialog
    if (showVoidConfirm && detailTrx != null) {
        AlertDialog(
            onDismissRequest = { showVoidConfirm = false },
            title = { Text("Batalkan Transaksi & Kembalikan Stok?") },
            text = {
                Column(modifier = Modifier.imePadding()) {
                    Text(
                        "Perhatian: Membatalkan transaksi ini akan SECARA OTOMATIS MENGEMBALIKAN stok seluruh barang yang ada di struk ini kembali ke gudang/rak toko."
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = voidReason,
                        onValueChange = { voidReason = it },
                        label = { Text("Alasan Pembatalan") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showVoidConfirm = false
                        viewModel.voidTransaction(detailTrx!!.id, voidReason)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StockRed)
                ) {
                    Text("Ya, Batalkan & Pulihkan Stok")
                }
            },
            dismissButton = {
                TextButton(onClick = { showVoidConfirm = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun TransactionItemCard(
    transaction: TransactionRecord,
    onClick: () -> Unit
) {
    val isVoided = transaction.status == TransactionStatus.DIBATALKAN

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isVoided) Slate100 else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isVoided) 0.dp else 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("trx_card_${transaction.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(14.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = transaction.invoiceNumber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isVoided) Slate500 else Slate800
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    val (badgeBg, badgeText, label) = when {
                        isVoided -> Triple(StockRedContainer, StockRed, "Batal")
                        transaction.paymentMethod == PaymentMethod.KASBON ->
                            Triple(KelontongAmberContainer, KelontongOnAmberContainer, "Kasbon")
                        else -> Triple(KelontongGreenContainer, KelontongGreenDark, transaction.paymentMethod.name)
                    }
                    Surface(color = badgeBg, shape = RoundedCornerShape(4.dp)) {
                        Text(
                            text = label,
                            color = badgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = CurrencyFormatter.formatDate(transaction.timestamp),
                    fontSize = 11.sp,
                    color = Slate500
                )

                if (transaction.customerName.isNotBlank()) {
                    Text(
                        text = "Pelanggan: ${transaction.customerName}",
                        fontSize = 12.sp,
                        color = Slate700,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyFormatter.formatRupiah(transaction.finalAmount),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (isVoided) Slate500 else KelontongGreenDark
                )
                Text(
                    text = if (isVoided) "Stok dipulihkan" else "Lihat detail >",
                    fontSize = 11.sp,
                    color = Slate500
                )
            }
        }
    }
}
