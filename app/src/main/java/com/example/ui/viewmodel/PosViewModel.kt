package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.CustomerDebt
import com.example.data.model.PaymentMethod
import com.example.data.model.Product
import com.example.data.model.StockMutation
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionRecord
import com.example.data.model.MonthlyItemSoldSummary
import com.example.data.model.MonthlyProfitLossReport
import com.example.data.model.TransactionStatus
import com.example.data.repository.PosRepository
import com.example.ui.util.CurrencyFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CartItem(
    val product: Product,
    val quantity: Int
) {
    val subtotal: Long get() = product.sellingPrice * quantity
    val totalCost: Long get() = product.costPrice * quantity
}

enum class StockFilterTab {
    SEMUA,
    MENIPIS,
    HABIS
}

data class CompletedReceipt(
    val transaction: TransactionRecord,
    val items: List<TransactionItem>
)

class PosViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = PosRepository(database)

    // Store settings (can be customized by user)
    private val _storeName = MutableStateFlow("Toko Kelontong Berkah Jaya")
    val storeName: StateFlow<String> = _storeName.asStateFlow()

    private val _storeAddress = MutableStateFlow("Jl. Melati No. 12, RT 03/RW 02")
    val storeAddress: StateFlow<String> = _storeAddress.asStateFlow()

    private val _storePhone = MutableStateFlow("0812-3456-7890")
    val storePhone: StateFlow<String> = _storePhone.asStateFlow()

    private val _receiptFooter = MutableStateFlow("Terima kasih telah berbelanja di warung kami!")
    val receiptFooter: StateFlow<String> = _receiptFooter.asStateFlow()

    // Filter & Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Semua")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _stockFilterTab = MutableStateFlow(StockFilterTab.SEMUA)
    val stockFilterTab: StateFlow<StockFilterTab> = _stockFilterTab.asStateFlow()

    // Master Raw Flows
    val allProducts: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockCount: StateFlow<Int> = repository.lowStockCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val allStockMutations: StateFlow<List<StockMutation>> = repository.allStockMutations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<TransactionRecord>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactionItems: StateFlow<List<TransactionItem>> = repository.allTransactionItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filter Periode Bulan untuk Laporan Laba Rugi
    private val _selectedReportMonth = MutableStateFlow(
        CurrencyFormatter.formatYearMonthKey(System.currentTimeMillis())
    )
    val selectedReportMonth: StateFlow<String> = _selectedReportMonth.asStateFlow()

    fun selectReportMonth(yearMonthKey: String) {
        _selectedReportMonth.value = yearMonthKey
    }

    // Daftar bulan yang tersedia dalam database transaksi
    val availableReportMonths: StateFlow<List<Pair<String, String>>> = allTransactions.map { trxs ->
        val currentKey = CurrencyFormatter.formatYearMonthKey(System.currentTimeMillis())
        val keys = trxs.map { CurrencyFormatter.formatYearMonthKey(it.timestamp) }
            .plus(currentKey)
            .distinct()
            .sortedDescending()

        keys.map { key ->
            val sampleTimestamp = trxs.firstOrNull { CurrencyFormatter.formatYearMonthKey(it.timestamp) == key }?.timestamp
                ?: System.currentTimeMillis()
            key to CurrencyFormatter.formatMonthYear(sampleTimestamp)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        listOf(
            CurrencyFormatter.formatYearMonthKey(System.currentTimeMillis()) to CurrencyFormatter.formatMonthYear(System.currentTimeMillis())
        )
    )

    // Laporan Laba Rugi Bulanan Terpilih (Total Penjualan - Modal Barang Terjual)
    val monthlyProfitLossReport: StateFlow<MonthlyProfitLossReport> = combine(
        allTransactions,
        allTransactionItems,
        _selectedReportMonth
    ) { trxs, items, selectedMonthKey ->
        calculateMonthlyReport(trxs, items, selectedMonthKey)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        calculateMonthlyReport(emptyList(), emptyList(), CurrencyFormatter.formatYearMonthKey(System.currentTimeMillis()))
    )

    // Rekapitulasi Riwayat Laba Rugi Setiap Bulan
    val allMonthlyReports: StateFlow<List<MonthlyProfitLossReport>> = combine(
        allTransactions,
        allTransactionItems,
        availableReportMonths
    ) { trxs, items, months ->
        months.map { (key, _) ->
            calculateMonthlyReport(trxs, items, key)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDebts: StateFlow<List<CustomerDebt>> = repository.allDebts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unsettledDebts: StateFlow<List<CustomerDebt>> = repository.unsettledDebts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unsettledDebtCount: StateFlow<Int> = repository.unsettledDebtCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Filtered Products for POS Cashier Screen
    val filteredPosProducts: StateFlow<List<Product>> = combine(
        allProducts,
        _searchQuery,
        _selectedCategory
    ) { products, query, category ->
        products.filter { p ->
            val matchQuery = query.isBlank() ||
                    p.name.contains(query, ignoreCase = true) ||
                    p.barcode.contains(query, ignoreCase = true) ||
                    p.category.contains(query, ignoreCase = true)
            val matchCategory = category == "Semua" || p.category.equals(category, ignoreCase = true)
            matchQuery && matchCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Products for Inventory Screen
    val filteredStockProducts: StateFlow<List<Product>> = combine(
        allProducts,
        _searchQuery,
        _stockFilterTab
    ) { products, query, tab ->
        products.filter { p ->
            val matchQuery = query.isBlank() ||
                    p.name.contains(query, ignoreCase = true) ||
                    p.barcode.contains(query, ignoreCase = true) ||
                    p.category.contains(query, ignoreCase = true)
            val matchTab = when (tab) {
                StockFilterTab.SEMUA -> true
                StockFilterTab.MENIPIS -> p.currentStock in 1..p.minStockAlert
                StockFilterTab.HABIS -> p.currentStock <= 0
            }
            matchQuery && matchTab
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cart State
    private val _cart = MutableStateFlow<Map<Long, CartItem>>(emptyMap())
    val cart: StateFlow<Map<Long, CartItem>> = _cart.asStateFlow()

    private val _discountAmount = MutableStateFlow(0L)
    val discountAmount: StateFlow<Long> = _discountAmount.asStateFlow()

    val cartItemsList: StateFlow<List<CartItem>> = _cart.combine(_cart) { map, _ ->
        map.values.toList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartTotalQuantity: StateFlow<Int> = _cart.combine(_cart) { map, _ ->
        map.values.sumOf { it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val cartSubtotal: StateFlow<Long> = _cart.combine(_cart) { map, _ ->
        map.values.sumOf { it.subtotal }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val cartFinalAmount: StateFlow<Long> = combine(cartSubtotal, _discountAmount) { subtotal, disc ->
        (subtotal - disc).coerceAtLeast(0L)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    // UI Dialog & Sheet States
    private val _isCartSheetVisible = MutableStateFlow(false)
    val isCartSheetVisible: StateFlow<Boolean> = _isCartSheetVisible.asStateFlow()

    private val _isCheckoutDialogVisible = MutableStateFlow(false)
    val isCheckoutDialogVisible: StateFlow<Boolean> = _isCheckoutDialogVisible.asStateFlow()

    private val _activeReceipt = MutableStateFlow<CompletedReceipt?>(null)
    val activeReceipt: StateFlow<CompletedReceipt?> = _activeReceipt.asStateFlow()

    private val _restockTargetProduct = MutableStateFlow<Product?>(null)
    val restockTargetProduct: StateFlow<Product?> = _restockTargetProduct.asStateFlow()

    private val _adjustTargetProduct = MutableStateFlow<Product?>(null)
    val adjustTargetProduct: StateFlow<Product?> = _adjustTargetProduct.asStateFlow()

    private val _editingProduct = MutableStateFlow<Product?>(null)
    val editingProduct: StateFlow<Product?> = _editingProduct.asStateFlow()

    private val _isAddProductDialogOpen = MutableStateFlow(false)
    val isAddProductDialogOpen: StateFlow<Boolean> = _isAddProductDialogOpen.asStateFlow()

    private val _viewingTransactionDetail = MutableStateFlow<TransactionRecord?>(null)
    val viewingTransactionDetail: StateFlow<TransactionRecord?> = _viewingTransactionDetail.asStateFlow()

    private val _transactionItemsForDetail = MutableStateFlow<List<TransactionItem>>(emptyList())
    val transactionItemsForDetail: StateFlow<List<TransactionItem>> = _transactionItemsForDetail.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setStockFilterTab(tab: StockFilterTab) {
        _stockFilterTab.value = tab
    }

    fun updateStoreInfo(name: String, address: String, phone: String, footer: String) {
        _storeName.value = name
        _storeAddress.value = address
        _storePhone.value = phone
        _receiptFooter.value = footer
        _userMessage.value = "Informasi toko berhasil disimpan!"
    }

    // Cart Operations
    fun addToCart(product: Product) {
        val currentMap = _cart.value.toMutableMap()
        val currentItem = currentMap[product.id]
        val currentQty = currentItem?.quantity ?: 0

        if (product.currentStock <= 0) {
            _userMessage.value = "Stok ${product.name} habis!"
            return
        }

        if (currentQty >= product.currentStock) {
            _userMessage.value = "Maksimal stok tercapai (${product.currentStock} ${product.unit})"
            return
        }

        val newQty = currentQty + 1
        currentMap[product.id] = CartItem(product = product, quantity = newQty)
        _cart.value = currentMap
    }

    fun decreaseCartItem(product: Product) {
        val currentMap = _cart.value.toMutableMap()
        val currentItem = currentMap[product.id] ?: return
        if (currentItem.quantity > 1) {
            currentMap[product.id] = currentItem.copy(quantity = currentItem.quantity - 1)
        } else {
            currentMap.remove(product.id)
        }
        _cart.value = currentMap
    }

    fun setCartItemQuantity(product: Product, quantity: Int) {
        val currentMap = _cart.value.toMutableMap()
        if (quantity <= 0) {
            currentMap.remove(product.id)
        } else {
            val validQty = quantity.coerceAtMost(product.currentStock)
            if (quantity > product.currentStock) {
                _userMessage.value = "Stok hanya tersedia ${product.currentStock} ${product.unit}"
            }
            currentMap[product.id] = CartItem(product = product, quantity = validQty)
        }
        _cart.value = currentMap
    }

    fun removeCartItem(productId: Long) {
        val currentMap = _cart.value.toMutableMap()
        currentMap.remove(productId)
        _cart.value = currentMap
    }

    fun clearCart() {
        _cart.value = emptyMap()
        _discountAmount.value = 0L
    }

    fun setDiscount(amount: Long) {
        _discountAmount.value = amount.coerceAtLeast(0L)
    }

    fun openCartSheet() {
        _isCartSheetVisible.value = true
    }

    fun closeCartSheet() {
        _isCartSheetVisible.value = false
    }

    fun openCheckoutDialog() {
        if (_cart.value.isEmpty()) {
            _userMessage.value = "Keranjang belanja masih kosong"
            return
        }
        _isCartSheetVisible.value = false
        _isCheckoutDialogVisible.value = true
    }

    fun closeCheckoutDialog() {
        _isCheckoutDialogVisible.value = false
    }

    fun closeReceiptDialog() {
        _activeReceipt.value = null
    }

    /**
     * PROSES CHECKOUT KASIR:
     * Validasi bukti pembayaran sesuai jumlah nominal total tagihan.
     * Jika tidak sesuai -> Pembayaran Gagal.
     * Jika sesuai -> Pembayaran lanjut, selesai & stok otomatis dipotong.
     */
    fun processCheckout(
        paymentMethod: PaymentMethod,
        paidAmount: Long,
        customerName: String,
        note: String,
        paymentProofNominal: Long = 0L,
        paymentProofRef: String = ""
    ): Boolean {
        val itemsInCart = _cart.value.values.toList()
        if (itemsInCart.isEmpty()) {
            _userMessage.value = "Keranjang belanja masih kosong"
            return false
        }

        val subtotal = itemsInCart.sumOf { it.subtotal }
        val discount = _discountAmount.value
        val finalAmount = (subtotal - discount).coerceAtLeast(0L)
        val totalCost = itemsInCart.sumOf { it.totalCost }

        // VALIDASI KESESUAIAN BUKTI PEMBAYARAN
        if (paymentMethod == PaymentMethod.QRIS || paymentMethod == PaymentMethod.TRANSFER) {
            if (paymentProofNominal != finalAmount) {
                val selisih = kotlin.math.abs(paymentProofNominal - finalAmount)
                val statusKet = if (paymentProofNominal < finalAmount) "Kurang" else "Lebih"
                _userMessage.value = "Pembayaran GAGAL: Nominal bukti transfer (${CurrencyFormatter.formatRupiah(paymentProofNominal)}) TIDAK SESUAI total tagihan (${CurrencyFormatter.formatRupiah(finalAmount)}). $statusKet ${CurrencyFormatter.formatRupiah(selisih)}!"
                return false
            }
        } else if (paymentMethod == PaymentMethod.TUNAI) {
            if (paidAmount < finalAmount) {
                val kurang = finalAmount - paidAmount
                _userMessage.value = "Pembayaran GAGAL: Uang tunai kurang ${CurrencyFormatter.formatRupiah(kurang)} dari total tagihan."
                return false
            }
        } else if (paymentMethod == PaymentMethod.KASBON) {
            if (customerName.isBlank()) {
                _userMessage.value = "Pembayaran GAGAL: Nama pelanggan wajib diisi untuk catatan kasbon."
                return false
            }
        }

        val actualPaid = when (paymentMethod) {
            PaymentMethod.KASBON -> 0L
            PaymentMethod.QRIS, PaymentMethod.TRANSFER -> paymentProofNominal
            PaymentMethod.TUNAI -> paidAmount
        }
        val change = (actualPaid - finalAmount).coerceAtLeast(0L)
        val invoice = CurrencyFormatter.generateInvoiceNumber()

        val trx = TransactionRecord(
            invoiceNumber = invoice,
            subtotalAmount = subtotal,
            discountAmount = discount,
            finalAmount = finalAmount,
            paidAmount = actualPaid,
            changeAmount = change,
            paymentMethod = paymentMethod,
            customerName = customerName.trim(),
            cashierName = "Kasir",
            note = note.trim(),
            totalCostAmount = totalCost,
            status = TransactionStatus.SELESAI,
            paymentProofNominal = if (paymentMethod == PaymentMethod.QRIS || paymentMethod == PaymentMethod.TRANSFER) paymentProofNominal else 0L,
            paymentProofRef = paymentProofRef.trim(),
            isPaymentProofValid = true,
            timestamp = System.currentTimeMillis()
        )

        val trxItems = itemsInCart.map {
            TransactionItem(
                transactionId = 0L,
                productId = it.product.id,
                productName = it.product.name,
                unit = it.product.unit,
                costPrice = it.product.costPrice,
                sellingPrice = it.product.sellingPrice,
                quantity = it.quantity,
                subtotal = it.subtotal
            )
        }

        viewModelScope.launch {
            try {
                val trxId = repository.processCheckout(
                    transaction = trx,
                    items = trxItems,
                    debtCustomerName = if (paymentMethod == PaymentMethod.KASBON) customerName else null
                )

                val completedTrx = trx.copy(id = trxId)
                val completedItems = trxItems.map { it.copy(transactionId = trxId) }

                _activeReceipt.value = CompletedReceipt(
                    transaction = completedTrx,
                    items = completedItems
                )

                // Bersihkan keranjang
                clearCart()
                _isCheckoutDialogVisible.value = false
                _userMessage.value = "Pembayaran SUKSES! Bukti pembayaran sesuai tagihan & stok otomatis dipotong."
            } catch (e: Exception) {
                _userMessage.value = "Gagal memproses transaksi: ${e.localizedMessage}"
            }
        }
        return true
    }

    // Restock Dialog & Action
    fun openRestockDialog(product: Product) {
        _restockTargetProduct.value = product
    }

    fun closeRestockDialog() {
        _restockTargetProduct.value = null
    }

    fun submitRestock(productId: Long, addedQty: Int, newCostPrice: Long, note: String) {
        if (addedQty <= 0) {
            _userMessage.value = "Jumlah stok harus lebih dari 0"
            return
        }
        viewModelScope.launch {
            try {
                repository.restockProduct(productId, addedQty, newCostPrice, note)
                _restockTargetProduct.value = null
                _userMessage.value = "Berhasil menambah $addedQty stok! Tercatat di histori mutasi."
            } catch (e: Exception) {
                _userMessage.value = "Gagal menambah stok: ${e.localizedMessage}"
            }
        }
    }

    // Adjust Stock Dialog & Action
    fun openAdjustStockDialog(product: Product) {
        _adjustTargetProduct.value = product
    }

    fun closeAdjustStockDialog() {
        _adjustTargetProduct.value = null
    }

    fun submitStockAdjustment(productId: Long, newStock: Int, reason: String) {
        if (newStock < 0) {
            _userMessage.value = "Stok tidak boleh negatif"
            return
        }
        viewModelScope.launch {
            try {
                repository.adjustStock(productId, newStock, reason)
                _adjustTargetProduct.value = null
                _userMessage.value = "Stok berhasil disesuaikan! Alasan tercatat di mutasi."
            } catch (e: Exception) {
                _userMessage.value = "Gagal menyesuaikan stok: ${e.localizedMessage}"
            }
        }
    }

    // Add / Edit Product
    fun openAddProductDialog() {
        _editingProduct.value = null
        _isAddProductDialogOpen.value = true
    }

    fun openEditProductDialog(product: Product) {
        _editingProduct.value = product
        _isAddProductDialogOpen.value = true
    }

    fun closeProductFormDialog() {
        _editingProduct.value = null
        _isAddProductDialogOpen.value = false
    }

    fun saveProduct(
        id: Long = 0,
        name: String,
        barcode: String,
        category: String,
        unit: String,
        costPrice: Long,
        sellingPrice: Long,
        stock: Int,
        minAlert: Int,
        imageResName: String = ""
    ) {
        if (name.isBlank()) {
            _userMessage.value = "Nama barang tidak boleh kosong"
            return
        }
        viewModelScope.launch {
            try {
                val isNew = id == 0L
                val product = Product(
                    id = id,
                    name = name.trim(),
                    barcode = barcode.trim(),
                    category = category.trim(),
                    unit = unit.trim(),
                    costPrice = costPrice,
                    sellingPrice = sellingPrice,
                    currentStock = stock,
                    minStockAlert = minAlert,
                    imageResName = imageResName.trim(),
                    updatedAt = System.currentTimeMillis()
                )

                if (isNew) {
                    val newId = repository.insertProduct(product)
                    // Catat mutasi awal
                    database.productDao().insertStockMutation(
                        StockMutation(
                            productId = newId,
                            productName = product.name,
                            type = com.example.data.model.MutationType.RESTOCK,
                            changeQuantity = product.currentStock,
                            previousStock = 0,
                            resultingStock = product.currentStock,
                            referenceId = "NEW",
                            note = "Stok Masuk Produk Baru"
                        )
                    )
                    _userMessage.value = "Produk ${product.name} berhasil ditambahkan!"
                } else {
                    val existing = repository.getProductById(id)
                    repository.updateProduct(product)
                    if (existing != null && existing.currentStock != product.currentStock) {
                        val diff = product.currentStock - existing.currentStock
                        database.productDao().insertStockMutation(
                            StockMutation(
                                productId = id,
                                productName = product.name,
                                type = com.example.data.model.MutationType.PENYESUAIAN,
                                changeQuantity = diff,
                                previousStock = existing.currentStock,
                                resultingStock = product.currentStock,
                                referenceId = "EDIT",
                                note = "Penyesuaian saat edit produk"
                            )
                        )
                    }
                    _userMessage.value = "Produk ${product.name} berhasil diperbarui!"
                }
                closeProductFormDialog()
            } catch (e: Exception) {
                _userMessage.value = "Gagal menyimpan produk: ${e.localizedMessage}"
            }
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            try {
                repository.deleteProduct(product)
                _userMessage.value = "Produk ${product.name} telah dihapus"
            } catch (e: Exception) {
                _userMessage.value = "Gagal menghapus produk: ${e.localizedMessage}"
            }
        }
    }

    // Detail Transaksi & Batal / Void Transaksi
    fun openTransactionDetail(transaction: TransactionRecord) {
        _viewingTransactionDetail.value = transaction
        viewModelScope.launch {
            database.transactionDao().getItemsForTransaction(transaction.id).collect { items ->
                _transactionItemsForDetail.value = items
            }
        }
    }

    fun closeTransactionDetail() {
        _viewingTransactionDetail.value = null
        _transactionItemsForDetail.value = emptyList()
    }

    fun voidTransaction(transactionId: Long, reason: String) {
        viewModelScope.launch {
            try {
                repository.voidTransaction(transactionId, reason)
                closeTransactionDetail()
                _userMessage.value = "Transaksi dibatalkan. Stok seluruh barang otomatis dikembalikan!"
            } catch (e: Exception) {
                _userMessage.value = "Gagal membatalkan transaksi: ${e.localizedMessage}"
            }
        }
    }

    // Pelunasan Kasbon
    fun settleDebt(debt: CustomerDebt, payAmount: Long) {
        viewModelScope.launch {
            try {
                val newPaid = debt.paidAmount + payAmount
                val isFull = newPaid >= debt.totalDebt
                repository.settleDebt(debt.id, newPaid, isFull)
                _userMessage.value = if (isFull) {
                    "Kasbon ${debt.customerName} telah LUNAS!"
                } else {
                    "Pembayaran kasbon ${CurrencyFormatter.formatRupiah(payAmount)} tercatat."
                }
            } catch (e: Exception) {
                _userMessage.value = "Gagal memperbarui kasbon: ${e.localizedMessage}"
            }
        }
    }

    // Share Struk via WhatsApp / System Share Intent
    fun shareReceipt(context: Context, trx: TransactionRecord, items: List<TransactionItem>) {
        val sb = StringBuilder()
        sb.append("================================\n")
        sb.append("      ${_storeName.value}\n")
        sb.append("   ${_storeAddress.value}\n")
        sb.append("      Telp: ${_storePhone.value}\n")
        sb.append("================================\n")
        sb.append("No. Struk : ${trx.invoiceNumber}\n")
        sb.append("Tanggal   : ${CurrencyFormatter.formatDate(trx.timestamp)}\n")
        if (trx.customerName.isNotBlank()) {
            sb.append("Pelanggan : ${trx.customerName}\n")
        }
        sb.append("Kasir     : ${trx.cashierName}\n")
        sb.append("Metode    : ${trx.paymentMethod.name}\n")
        sb.append("--------------------------------\n")
        for (item in items) {
            sb.append("${item.productName}\n")
            sb.append("  ${item.quantity} x ${CurrencyFormatter.formatRupiah(item.sellingPrice)} = ${CurrencyFormatter.formatRupiah(item.subtotal)}\n")
        }
        sb.append("--------------------------------\n")
        sb.append("Subtotal  : ${CurrencyFormatter.formatRupiah(trx.subtotalAmount)}\n")
        if (trx.discountAmount > 0) {
            sb.append("Diskon    : -${CurrencyFormatter.formatRupiah(trx.discountAmount)}\n")
        }
        sb.append("TOTAL     : ${CurrencyFormatter.formatRupiah(trx.finalAmount)}\n")
        if (trx.paymentMethod != PaymentMethod.KASBON) {
            sb.append("Bayar     : ${CurrencyFormatter.formatRupiah(trx.paidAmount)}\n")
            sb.append("Kembalian : ${CurrencyFormatter.formatRupiah(trx.changeAmount)}\n")
            if (trx.paymentMethod == PaymentMethod.QRIS || trx.paymentMethod == PaymentMethod.TRANSFER) {
                sb.append("Bukti     : TERVERIFIKASI SESUAI (100%)\n")
                if (trx.paymentProofRef.isNotBlank()) {
                    sb.append("No. Ref   : ${trx.paymentProofRef}\n")
                }
            }
        } else {
            sb.append("Status    : KASBON / BON TOKO\n")
        }
        sb.append("================================\n")
        sb.append("${_receiptFooter.value}\n")

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, sb.toString())
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Bagikan Struk Toko")
        context.startActivity(shareIntent)
    }

    private fun calculateMonthlyReport(
        allTrx: List<TransactionRecord>,
        allItems: List<TransactionItem>,
        targetYearMonthKey: String
    ): MonthlyProfitLossReport {
        val monthTrx = allTrx.filter {
            it.status == TransactionStatus.SELESAI &&
                    CurrencyFormatter.formatYearMonthKey(it.timestamp) == targetYearMonthKey
        }

        val totalRevenue = monthTrx.sumOf { it.finalAmount }
        val totalCost = monthTrx.sumOf { it.totalCostAmount }
        val totalDiscount = monthTrx.sumOf { it.discountAmount }
        val netProfit = totalRevenue - totalCost
        val isProfit = netProfit >= 0
        val marginPercent = if (totalRevenue > 0) (netProfit.toDouble() * 100.0 / totalRevenue.toDouble()) else 0.0
        val trxCount = monthTrx.size
        val avgBasket = if (trxCount > 0) totalRevenue / trxCount else 0L

        val cashRev = monthTrx.filter { it.paymentMethod == PaymentMethod.TUNAI }.sumOf { it.finalAmount }
        val qrisRev = monthTrx.filter { it.paymentMethod == PaymentMethod.QRIS }.sumOf { it.finalAmount }
        val transferRev = monthTrx.filter { it.paymentMethod == PaymentMethod.TRANSFER }.sumOf { it.finalAmount }
        val kasbonRev = monthTrx.filter { it.paymentMethod == PaymentMethod.KASBON }.sumOf { it.finalAmount }

        val trxIds = monthTrx.map { it.id }.toSet()
        val relevantItems = allItems.filter { it.transactionId in trxIds }
        val itemBreakdown = relevantItems
            .groupBy { it.productId }
            .map { (prodId, itemList) ->
                val sample = itemList.first()
                val qtySold = itemList.sumOf { it.quantity }
                val itemRev = itemList.sumOf { it.subtotal }
                val itemCost = itemList.sumOf { it.costPrice * it.quantity }
                MonthlyItemSoldSummary(
                    productId = prodId,
                    productName = sample.productName,
                    unit = sample.unit,
                    totalQuantitySold = qtySold,
                    totalRevenue = itemRev,
                    totalCost = itemCost,
                    profit = itemRev - itemCost
                )
            }
            .sortedByDescending { it.totalRevenue }

        val displayMonth = if (monthTrx.isNotEmpty()) {
            CurrencyFormatter.formatMonthYear(monthTrx.first().timestamp)
        } else {
            try {
                val parts = targetYearMonthKey.split("-")
                val year = parts.getOrNull(0)?.toIntOrNull() ?: 2026
                val month = (parts.getOrNull(1)?.toIntOrNull() ?: 10) - 1
                val cal = java.util.Calendar.getInstance()
                cal.set(java.util.Calendar.YEAR, year)
                cal.set(java.util.Calendar.MONTH, month)
                CurrencyFormatter.formatMonthYear(cal.timeInMillis)
            } catch (e: Exception) {
                targetYearMonthKey
            }
        }

        return MonthlyProfitLossReport(
            yearMonthKey = targetYearMonthKey,
            displayMonthYear = displayMonth,
            totalRevenue = totalRevenue,
            totalCostOfGoodsSold = totalCost,
            totalDiscount = totalDiscount,
            netProfit = netProfit,
            profitMarginPercent = marginPercent,
            isProfit = isProfit,
            transactionCount = trxCount,
            averageTransactionValue = avgBasket,
            cashRevenue = cashRev,
            qrisRevenue = qrisRev,
            transferRevenue = transferRev,
            kasbonRevenue = kasbonRev,
            transactions = monthTrx,
            itemSoldBreakdown = itemBreakdown
        )
    }

    fun shareMonthlyReport(context: Context, report: MonthlyProfitLossReport) {
        val sb = StringBuilder()
        sb.append("================================\n")
        sb.append("   LAPORAN LABA RUGI BULANAN\n")
        sb.append("      ${_storeName.value}\n")
        sb.append("================================\n")
        sb.append("Periode     : ${report.displayMonthYear}\n")
        sb.append("Dicetak     : ${CurrencyFormatter.formatDate(System.currentTimeMillis())}\n")
        sb.append("--------------------------------\n")
        sb.append("1. Total Penjualan (Omzet)  : ${CurrencyFormatter.formatRupiah(report.totalRevenue)}\n")
        sb.append("2. Modal Terjual (HPP)      : ${CurrencyFormatter.formatRupiah(report.totalCostOfGoodsSold)}\n")
        if (report.totalDiscount > 0) {
            sb.append("3. Diskon Diberikan         : -${CurrencyFormatter.formatRupiah(report.totalDiscount)}\n")
        }
        sb.append("--------------------------------\n")
        val statusText = if (report.netProfit >= 0) "LABA BERSIH" else "RUGI BERSIH"
        val prefix = if (report.netProfit >= 0) "+" else ""
        sb.append("HASIL: $statusText\n")
        sb.append("Total Laba/Rugi: $prefix${CurrencyFormatter.formatRupiah(report.netProfit)}\n")
        sb.append("Margin Laba    : ${String.format(java.util.Locale.US, "%.1f", report.profitMarginPercent)}%\n")
        sb.append("--------------------------------\n")
        sb.append("Statistik Penjualan:\n")
        sb.append(" - Transaksi Selesai : ${report.transactionCount} Trx\n")
        sb.append(" - Rata-rata per Trx : ${CurrencyFormatter.formatRupiah(report.averageTransactionValue)}\n")
        sb.append("Rincian per Metode Bayar:\n")
        sb.append(" - Tunai    : ${CurrencyFormatter.formatRupiah(report.cashRevenue)}\n")
        sb.append(" - QRIS     : ${CurrencyFormatter.formatRupiah(report.qrisRevenue)}\n")
        sb.append(" - Transfer : ${CurrencyFormatter.formatRupiah(report.transferRevenue)}\n")
        sb.append(" - Kasbon   : ${CurrencyFormatter.formatRupiah(report.kasbonRevenue)}\n")
        sb.append("================================\n")
        sb.append("${_receiptFooter.value}\n")

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, sb.toString())
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Bagikan Laporan Laba Rugi")
        context.startActivity(shareIntent)
    }
}
