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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Product
import com.example.ui.components.StockBadge
import com.example.ui.theme.KelontongAmberContainer
import com.example.ui.theme.KelontongGreenContainer
import com.example.ui.theme.KelontongGreenDark
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.StockRed
import com.example.ui.theme.StockRedContainer
import com.example.ui.theme.StockWarningContainer
import com.example.ui.theme.StockWarningOrange
import com.example.ui.util.CurrencyFormatter
import com.example.ui.viewmodel.PosViewModel
import com.example.ui.viewmodel.StockFilterTab

@Composable
fun StockInventoryScreen(
    viewModel: PosViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.filteredStockProducts.collectAsStateWithLifecycle()
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val stockFilterTab by viewModel.stockFilterTab.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val restockTarget by viewModel.restockTargetProduct.collectAsStateWithLifecycle()
    val adjustTarget by viewModel.adjustTargetProduct.collectAsStateWithLifecycle()
    val editingProduct by viewModel.editingProduct.collectAsStateWithLifecycle()
    val isAddDialogOpen by viewModel.isAddProductDialogOpen.collectAsStateWithLifecycle()

    val totalProductCount = allProducts.size
    val totalStockValue = allProducts.sumOf { it.costPrice * it.currentStock }
    val lowStockCount = allProducts.count { it.currentStock in 1..it.minStockAlert }
    val outOfStockCount = allProducts.count { it.currentStock <= 0 }

    var productToDelete by remember { mutableStateOf<Product?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top Summary Cards
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Manajemen Inventaris & Stok",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Stok berkurang otomatis saat kasir checkout, bertambah saat restock.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Total Aset
                    Surface(
                        color = KelontongGreenContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Nilai Modal Stok", fontSize = 11.sp, color = KelontongGreenDark)
                            Text(
                                text = CurrencyFormatter.formatRupiah(totalStockValue),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = KelontongGreenDark
                            )
                            Text("$totalProductCount Produk", fontSize = 10.sp, color = Slate700)
                        }
                    }

                    // Menipis & Habis
                    Surface(
                        color = if (outOfStockCount > 0) StockRedContainer else StockWarningContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Status Peringatan", fontSize = 11.sp, color = Slate700)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$lowStockCount Menipis",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StockWarningOrange
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• $outOfStockCount Habis",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StockRed
                                )
                            }
                            Text("Perlu kulakan segera", fontSize = 10.sp, color = Slate500)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Cari produk atau barcode...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Slate500)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = null)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Tabs: Semua, Menipis, Habis
            TabRow(
                selectedTabIndex = stockFilterTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = stockFilterTab == StockFilterTab.SEMUA,
                    onClick = { viewModel.setStockFilterTab(StockFilterTab.SEMUA) },
                    text = { Text("Semua ($totalProductCount)") }
                )
                Tab(
                    selected = stockFilterTab == StockFilterTab.MENIPIS,
                    onClick = { viewModel.setStockFilterTab(StockFilterTab.MENIPIS) },
                    text = { Text("Menipis ($lowStockCount)") }
                )
                Tab(
                    selected = stockFilterTab == StockFilterTab.HABIS,
                    onClick = { viewModel.setStockFilterTab(StockFilterTab.HABIS) },
                    text = { Text("Habis ($outOfStockCount)") }
                )
            }

            // Products List
            if (products.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Inventory,
                            contentDescription = null,
                            tint = Slate500,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tidak ada produk dalam daftar ini",
                            fontWeight = FontWeight.SemiBold,
                            color = Slate700
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("stock_inventory_list")
                ) {
                    items(products, key = { it.id }) { product ->
                        ProductInventoryItemCard(
                            product = product,
                            onRestock = { viewModel.openRestockDialog(product) },
                            onAdjust = { viewModel.openAdjustStockDialog(product) },
                            onEdit = { viewModel.openEditProductDialog(product) },
                            onDelete = { productToDelete = product }
                        )
                    }
                }
            }
        }

        // FAB: Tambah Barang Baru
        FloatingActionButton(
            onClick = { viewModel.openAddProductDialog() },
            containerColor = KelontongGreenDark,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("fab_add_product")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Tambah Produk", fontWeight = FontWeight.Bold)
            }
        }

        // Restock Dialog
        if (restockTarget != null) {
            RestockDialog(
                product = restockTarget!!,
                onDismiss = { viewModel.closeRestockDialog() },
                onConfirm = { addedQty, newCost, note ->
                    viewModel.submitRestock(restockTarget!!.id, addedQty, newCost, note)
                }
            )
        }

        // Adjust Stock Dialog
        if (adjustTarget != null) {
            AdjustStockDialog(
                product = adjustTarget!!,
                onDismiss = { viewModel.closeAdjustStockDialog() },
                onConfirm = { newStock, reason ->
                    viewModel.submitStockAdjustment(adjustTarget!!.id, newStock, reason)
                }
            )
        }

        // Add or Edit Product Dialog
        if (isAddDialogOpen) {
            ProductFormDialog(
                initialProduct = editingProduct,
                onDismiss = { viewModel.closeProductFormDialog() },
                onSave = { id, name, barcode, cat, unit, cost, sell, stock, minAlert ->
                    viewModel.saveProduct(id, name, barcode, cat, unit, cost, sell, stock, minAlert)
                }
            )
        }

        // Delete Confirm Dialog
        if (productToDelete != null) {
            AlertDialog(
                onDismissRequest = { productToDelete = null },
                title = { Text("Hapus Produk?") },
                text = { Text("Yakin ingin menghapus '${productToDelete?.name}'? Riwayat transaksi sebelumnya tetap tersimpan.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteProduct(productToDelete!!)
                            productToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StockRed)
                    ) {
                        Text("Hapus")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { productToDelete = null }) {
                        Text("Batal")
                    }
                }
            )
        }
    }
}

@Composable
fun ProductInventoryItemCard(
    product: Product,
    onRestock: () -> Unit,
    onAdjust: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("inventory_item_${product.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Slate100
                        ) {
                            Text(
                                text = product.category,
                                fontSize = 10.sp,
                                color = Slate700,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        if (product.barcode.isNotBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "#${product.barcode}",
                                fontSize = 10.sp,
                                color = Slate500
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = product.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Slate800
                    )
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Slate700, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = StockRed, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Pricing Info & Stock Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Jual: ${CurrencyFormatter.formatRupiah(product.sellingPrice)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = KelontongGreenDark
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "(Modal: ${CurrencyFormatter.formatRupiah(product.costPrice)})",
                            fontSize = 11.sp,
                            color = Slate500
                        )
                    }
                    val profitMargin = product.sellingPrice - product.costPrice
                    Text(
                        text = "Untung: +${CurrencyFormatter.formatRupiah(profitMargin)} / ${product.unit}",
                        fontSize = 11.sp,
                        color = Slate700,
                        fontWeight = FontWeight.Medium
                    )
                }

                StockBadge(
                    currentStock = product.currentStock,
                    minStockAlert = product.minStockAlert,
                    unit = product.unit
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: Restock (+) & Atur Stok
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onRestock,
                    colors = ButtonDefaults.buttonColors(containerColor = KelontongGreenDark),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 6.dp, horizontal = 12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_restock_${product.id}")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("+ Restock (Kulakan)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onAdjust,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 6.dp, horizontal = 12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Koreksi Stok", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun RestockDialog(
    product: Product,
    onDismiss: () -> Unit,
    onConfirm: (Int, Long, String) -> Unit
) {
    var addedQtyText by remember { mutableStateOf("10") }
    var newCostText by remember { mutableStateOf(product.costPrice.toString()) }
    var note by remember { mutableStateOf("Kulakan agen / pasar") }

    val addedQty = addedQtyText.toIntOrNull() ?: 0
    val newCost = newCostText.toLongOrNull() ?: product.costPrice

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Tambah Stok (Restock)",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = KelontongGreenDark
                )
                Text(
                    text = "Sisa stok saat ini: ${product.currentStock} ${product.unit}",
                    fontSize = 12.sp,
                    color = Slate500
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = addedQtyText,
                    onValueChange = { addedQtyText = it },
                    label = { Text("Jumlah Masuk (${product.unit})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_restock_qty")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = newCostText,
                    onValueChange = { newCostText = it },
                    label = { Text("Harga Modal Baru per ${product.unit} (Opsional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Catatan / Nama Supplier") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (addedQty > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = KelontongGreenContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Stok akhir akan menjadi: ${product.currentStock + addedQty} ${product.unit} (Tercatat otomatis di mutasi)",
                            fontSize = 11.sp,
                            color = KelontongGreenDark,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(addedQty, newCost, note) },
                enabled = addedQty > 0,
                colors = ButtonDefaults.buttonColors(containerColor = KelontongGreenDark),
                modifier = Modifier.testTag("btn_confirm_restock")
            ) {
                Text("Simpan Restock")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun AdjustStockDialog(
    product: Product,
    onDismiss: () -> Unit,
    onConfirm: (Int, String) -> Unit
) {
    var newStockText by remember { mutableStateOf(product.currentStock.toString()) }
    var reason by remember { mutableStateOf("Hasil opname / selisih fisik") }

    val newStock = newStockText.toIntOrNull() ?: 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Koreksi / Penyesuaian Stok", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(product.name, fontWeight = FontWeight.SemiBold, color = Slate800)
                Text("Stok tercatat sistem: ${product.currentStock} ${product.unit}", fontSize = 12.sp, color = Slate500)

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = newStockText,
                    onValueChange = { newStockText = it },
                    label = { Text("Stok Fisik Sebenarnya (${product.unit})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Alasan Penyesuaian") },
                    placeholder = { Text("Contoh: Barang rusak / kadaluarsa / selisih hitung") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(newStock, reason) },
                colors = ButtonDefaults.buttonColors(containerColor = KelontongGreenDark)
            ) {
                Text("Simpan Koreksi")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormDialog(
    initialProduct: Product?,
    onDismiss: () -> Unit,
    onSave: (Long, String, String, String, String, Long, Long, Int, Int) -> Unit
) {
    val isEdit = initialProduct != null

    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var barcode by remember { mutableStateOf(initialProduct?.barcode ?: "") }
    var category by remember { mutableStateOf(initialProduct?.category ?: "Sembako") }
    var unit by remember { mutableStateOf(initialProduct?.unit ?: "Pcs") }
    var costPriceText by remember { mutableStateOf(initialProduct?.costPrice?.toString() ?: "") }
    var sellingPriceText by remember { mutableStateOf(initialProduct?.sellingPrice?.toString() ?: "") }
    var stockText by remember { mutableStateOf(initialProduct?.currentStock?.toString() ?: "10") }
    var minAlertText by remember { mutableStateOf(initialProduct?.minStockAlert?.toString() ?: "5") }

    val categories = listOf("Sembako", "Makanan", "Minuman", "Bumbu", "Perlengkapan", "Lainnya")
    val units = listOf("Pcs", "Kg", "Bungkus", "Botol", "Liter", "Pouch", "Dus", "Renceng", "Sak", "Kaleng", "Tabung")

    var categoryExpanded by remember { mutableStateOf(false) }
    var unitExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isEdit) "Edit Produk" else "Tambah Produk Baru", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Barang *") },
                    placeholder = { Text("Contoh: Beras Rojolele 5kg") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_product_name")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = barcode,
                    onValueChange = { barcode = it },
                    label = { Text("Barcode / Kode SKU") },
                    placeholder = { Text("Scan atau ketik kode") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Kategori Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kategori") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Satuan Dropdown
                ExposedDropdownMenuBox(
                    expanded = unitExpanded,
                    onExpandedChange = { unitExpanded = !unitExpanded }
                ) {
                    OutlinedTextField(
                        value = unit,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Satuan") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = unitExpanded,
                        onDismissRequest = { unitExpanded = false }
                    ) {
                        units.forEach { u ->
                            DropdownMenuItem(
                                text = { Text(u) },
                                onClick = {
                                    unit = u
                                    unitExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = costPriceText,
                        onValueChange = { costPriceText = it },
                        label = { Text("Harga Modal (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_cost_price")
                    )

                    OutlinedTextField(
                        value = sellingPriceText,
                        onValueChange = { sellingPriceText = it },
                        label = { Text("Harga Jual (Rp) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_selling_price")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = stockText,
                        onValueChange = { stockText = it },
                        label = { Text("Stok Sekarang") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_current_stock")
                    )

                    OutlinedTextField(
                        value = minAlertText,
                        onValueChange = { minAlertText = it },
                        label = { Text("Batas Peringatan") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            val cost = costPriceText.toLongOrNull() ?: 0L
            val sell = sellingPriceText.toLongOrNull() ?: 0L
            val stock = stockText.toIntOrNull() ?: 0
            val minAlert = minAlertText.toIntOrNull() ?: 5

            Button(
                onClick = {
                    onSave(
                        initialProduct?.id ?: 0L,
                        name,
                        barcode,
                        category,
                        unit,
                        cost,
                        sell,
                        stock,
                        minAlert
                    )
                },
                enabled = name.isNotBlank() && sell > 0,
                colors = ButtonDefaults.buttonColors(containerColor = KelontongGreenDark),
                modifier = Modifier.testTag("btn_save_product")
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
