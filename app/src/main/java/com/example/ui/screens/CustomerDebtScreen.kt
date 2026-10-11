package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CustomerDebt
import com.example.ui.theme.KelontongAmber
import com.example.ui.theme.KelontongAmberContainer
import com.example.ui.theme.KelontongGreenContainer
import com.example.ui.theme.KelontongGreenDark
import com.example.ui.theme.KelontongOnAmberContainer
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.StockSafeGreen
import com.example.ui.util.CurrencyFormatter
import com.example.ui.viewmodel.PosViewModel

@Composable
fun CustomerDebtScreen(
    viewModel: PosViewModel,
    modifier: Modifier = Modifier
) {
    val debts by viewModel.allDebts.collectAsStateWithLifecycle()
    val unsettledDebts by viewModel.unsettledDebts.collectAsStateWithLifecycle()
    val totalUnsettledAmount = unsettledDebts.sumOf { it.totalDebt - it.paidAmount }

    var debtToSettle by remember { mutableStateOf<CustomerDebt?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Buku Kasbon / Hutang Pelanggan",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Catatan bon belanja pelanggan toko. Transaksi kasbon tetap mengurangi stok otomatis.",
            style = MaterialTheme.typography.bodySmall,
            color = Slate500
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Total Kasbon Banner
        Surface(
            color = KelontongAmberContainer,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(16.dp)
            ) {
                Column {
                    Text("Total Piutang Belum Lunas", fontSize = 12.sp, color = KelontongOnAmberContainer)
                    Text(
                        text = CurrencyFormatter.formatRupiah(totalUnsettledAmount),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = KelontongOnAmberContainer
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = KelontongAmber
                ) {
                    Text(
                        text = "${unsettledDebts.size} Pelanggan",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (debts.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null,
                        tint = Slate500,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tidak ada catatan kasbon",
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
                    .testTag("debt_list")
            ) {
                items(debts, key = { it.id }) { debt ->
                    DebtItemCard(
                        debt = debt,
                        onSettle = { debtToSettle = debt }
                    )
                }
            }
        }
    }

    // Settle Dialog
    if (debtToSettle != null) {
        val currentDebt = debtToSettle!!
        val remaining = currentDebt.totalDebt - currentDebt.paidAmount
        var payAmountText by remember { mutableStateOf(remaining.toString()) }

        AlertDialog(
            onDismissRequest = { debtToSettle = null },
            title = { Text("Bayar / Lunasi Kasbon", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.imePadding()) {
                    Text("Pelanggan: ${currentDebt.customerName}", fontWeight = FontWeight.SemiBold, color = Slate800)
                    Text("Total Bon: ${CurrencyFormatter.formatRupiah(currentDebt.totalDebt)}", fontSize = 12.sp, color = Slate500)
                    Text("Sisa Belum Dibayar: ${CurrencyFormatter.formatRupiah(remaining)}", fontWeight = FontWeight.Bold, color = KelontongAmber, fontSize = 14.sp)

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = payAmountText,
                        onValueChange = { payAmountText = it },
                        label = { Text("Nominal Bayar (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_debt_pay_amount")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { payAmountText = remaining.toString() },
                            colors = ButtonDefaults.buttonColors(containerColor = Slate100),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Lunasi Semua", color = Slate700, fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                val pay = payAmountText.toLongOrNull() ?: 0L
                Button(
                    onClick = {
                        viewModel.settleDebt(currentDebt, pay)
                        debtToSettle = null
                    },
                    enabled = pay > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = KelontongGreenDark),
                    modifier = Modifier.testTag("btn_confirm_debt_pay")
                ) {
                    Text("Simpan Pembayaran")
                }
            },
            dismissButton = {
                TextButton(onClick = { debtToSettle = null }) { Text("Batal") }
            }
        )
    }
}

@Composable
fun DebtItemCard(
    debt: CustomerDebt,
    onSettle: () -> Unit
) {
    val remaining = debt.totalDebt - debt.paidAmount
    val isSettled = debt.isSettled || remaining <= 0

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSettled) Slate100 else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSettled) 0.dp else 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = debt.customerName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Slate800
                    )
                    Text(
                        text = "Ref: ${debt.invoiceNumber} • ${CurrencyFormatter.formatDateOnly(debt.createdAt)}",
                        fontSize = 11.sp,
                        color = Slate500
                    )
                }

                Surface(
                    color = if (isSettled) KelontongGreenContainer else KelontongAmberContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (isSettled) "LUNAS" else "BELUM LUNAS",
                        color = if (isSettled) StockSafeGreen else KelontongOnAmberContainer,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text("Total Kasbon: ${CurrencyFormatter.formatRupiah(debt.totalDebt)}", fontSize = 12.sp, color = Slate700)
                    if (debt.paidAmount > 0) {
                        Text("Sudah Dibayar: ${CurrencyFormatter.formatRupiah(debt.paidAmount)}", fontSize = 11.sp, color = StockSafeGreen)
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Sisa Tagihan", fontSize = 11.sp, color = Slate500)
                    Text(
                        text = CurrencyFormatter.formatRupiah(remaining),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (isSettled) Slate500 else KelontongAmber
                    )
                }
            }

            if (!isSettled) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onSettle,
                    colors = ButtonDefaults.buttonColors(containerColor = KelontongGreenDark),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Bayar / Lunasi Kasbon", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
