package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.theme.KelontongGreenContainer
import com.example.ui.theme.KelontongGreenDark
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.StockSafeGreen
import com.example.ui.viewmodel.PosViewModel

@Composable
fun SettingsScreen(
    viewModel: PosViewModel,
    modifier: Modifier = Modifier
) {
    val storeName by viewModel.storeName.collectAsStateWithLifecycle()
    val storeAddress by viewModel.storeAddress.collectAsStateWithLifecycle()
    val storePhone by viewModel.storePhone.collectAsStateWithLifecycle()
    val receiptFooter by viewModel.receiptFooter.collectAsStateWithLifecycle()

    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val allMutations by viewModel.allStockMutations.collectAsStateWithLifecycle()

    var isEditStoreOpen by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Hero Store Banner
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.grocery_banner_1791736011822),
                contentDescription = "Toko Kelontong",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Store Profile Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = KelontongGreenContainer,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Store,
                                    contentDescription = null,
                                    tint = KelontongGreenDark
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = storeName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Slate800
                            )
                            Text("Profil Toko & Struk", fontSize = 12.sp, color = Slate500)
                        }
                    }

                    Button(
                        onClick = { isEditStoreOpen = true },
                        colors = ButtonDefaults.buttonColors(containerColor = KelontongGreenDark),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_edit_store_profile")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ubah", fontSize = 12.sp)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Slate200)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Slate500, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(storeAddress, fontSize = 13.sp, color = Slate700)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = Slate500, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(storePhone, fontSize = 13.sp, color = Slate700)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Receipt, contentDescription = null, tint = Slate500, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("\"$receiptFooter\"", fontSize = 12.sp, color = Slate500)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // System Capabilities Checklist
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Sistem Kasir & Stok Otomatis Aktif",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                listOf(
                    "Pengurangan stok real-time saat transaksi berhasil",
                    "Audit trail & mutasi otomatis setiap ada perubahan stok",
                    "Peringatan stok menipis dan habis",
                    "Fitur restock (kulakan) dengan update modal otomatis",
                    "Pembatalan / retur transaksi dengan pemulihan stok otomatis",
                    "Pencatatan kasbon pelanggan dan pelunasan",
                    "Cetak dan bagikan struk via WhatsApp",
                    "Database offline-first Room (tidak butuh internet)"
                ).forEach { feature ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = StockSafeGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = feature, fontSize = 12.sp, color = Slate700)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Database Statistics
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Statistik Data Aplikasi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Text("Total Item Produk", fontSize = 13.sp, color = Slate500)
                    Text("${allProducts.size} item", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Text("Total Transaksi Tersimpan", fontSize = 13.sp, color = Slate500)
                    Text("${allTransactions.size} transaksi", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Text("Total Mutasi Stok Tercatat", fontSize = 13.sp, color = Slate500)
                    Text("${allMutations.size} pergerakan", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    if (isEditStoreOpen) {
        var tempName by remember { mutableStateOf(storeName) }
        var tempAddress by remember { mutableStateOf(storeAddress) }
        var tempPhone by remember { mutableStateOf(storePhone) }
        var tempFooter by remember { mutableStateOf(receiptFooter) }

        AlertDialog(
            onDismissRequest = { isEditStoreOpen = false },
            title = { Text("Ubah Informasi Toko", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        label = { Text("Nama Toko") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tempAddress,
                        onValueChange = { tempAddress = it },
                        label = { Text("Alamat Toko") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tempPhone,
                        onValueChange = { tempPhone = it },
                        label = { Text("Nomor HP / WhatsApp") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tempFooter,
                        onValueChange = { tempFooter = it },
                        label = { Text("Pesan Kaki Struk") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateStoreInfo(tempName, tempAddress, tempPhone, tempFooter)
                        isEditStoreOpen = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KelontongGreenDark)
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { isEditStoreOpen = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
