package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.database.AppDatabase
import com.example.data.model.CustomerDebt
import com.example.data.model.MutationType
import com.example.data.model.PaymentMethod
import com.example.data.model.Product
import com.example.data.model.StockMutation
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionRecord
import com.example.data.model.TransactionStatus
import kotlinx.coroutines.flow.Flow

class PosRepository(private val database: AppDatabase) {

    private val productDao = database.productDao()
    private val transactionDao = database.transactionDao()

    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val lowStockProducts: Flow<List<Product>> = productDao.getLowStockProducts()
    val lowStockCount: Flow<Int> = productDao.getLowStockCount()
    val allStockMutations: Flow<List<StockMutation>> = productDao.getAllStockMutations()
    val allTransactions: Flow<List<TransactionRecord>> = transactionDao.getAllTransactions()
    val allDebts: Flow<List<CustomerDebt>> = transactionDao.getAllDebts()
    val unsettledDebts: Flow<List<CustomerDebt>> = transactionDao.getUnsettledDebts()
    val unsettledDebtCount: Flow<Int> = transactionDao.getUnsettledDebtCount()

    fun searchProducts(query: String): Flow<List<Product>> = productDao.searchProducts(query)

    fun getProductsByCategory(category: String): Flow<List<Product>> = productDao.getProductsByCategory(category)

    fun getItemsForTransaction(transactionId: Long): Flow<List<TransactionItem>> =
        transactionDao.getItemsForTransaction(transactionId)

    suspend fun getProductById(id: Long): Product? = productDao.getProductById(id)

    suspend fun getProductByBarcode(barcode: String): Product? = productDao.getProductByBarcode(barcode)

    suspend fun insertProduct(product: Product): Long = productDao.insertProduct(product)

    suspend fun updateProduct(product: Product): Int = productDao.updateProduct(product)

    suspend fun deleteProduct(product: Product): Int = productDao.deleteProduct(product)

    /**
     * PROSES CHECKOUT KASIR DENGAN PENCATATAN STOK OTOMATIS:
     * 1. Simpan Transaksi Penjualan
     * 2. Simpan Item Detail Transaksi
     * 3. POTONG STOK FISIK SETIAP BARANG SECARA OTOMATIS
     * 4. CATAT BUKTI MUTASI STOK KELUAR (AUDIT TRAIL) UNTUK SETIAP BARANG
     * 5. Jika metode bayar KASBON, catat buku hutang pelanggan
     */
    suspend fun processCheckout(
        transaction: TransactionRecord,
        items: List<TransactionItem>,
        debtCustomerName: String? = null
    ): Long {
        return database.withTransaction {
            val transactionId = transactionDao.insertTransaction(transaction)

            val itemsWithTrxId = items.map { it.copy(transactionId = transactionId) }
            transactionDao.insertTransactionItems(itemsWithTrxId)

            // Pengurangan stok otomatis & audit log mutasi
            for (item in items) {
                val currentProduct = productDao.getProductById(item.productId)
                if (currentProduct != null) {
                    val prevStock = currentProduct.currentStock
                    val newStock = (prevStock - item.quantity).coerceAtLeast(0)

                    productDao.updateStock(
                        id = item.productId,
                        newStock = newStock,
                        updatedAt = System.currentTimeMillis()
                    )

                    // Catat mutasi penjualan otomatis
                    productDao.insertStockMutation(
                        StockMutation(
                            productId = item.productId,
                            productName = currentProduct.name,
                            type = MutationType.PENJUALAN,
                            changeQuantity = -item.quantity,
                            previousStock = prevStock,
                            resultingStock = newStock,
                            referenceId = transaction.invoiceNumber,
                            note = "Penjualan Kasir (#${transaction.invoiceNumber})",
                            timestamp = transaction.timestamp
                        )
                    )
                }
            }

            // Catat Kasbon jika metode pembayaran KASBON
            if (transaction.paymentMethod == PaymentMethod.KASBON && !debtCustomerName.isNullOrBlank()) {
                transactionDao.insertDebt(
                    CustomerDebt(
                        transactionId = transactionId,
                        invoiceNumber = transaction.invoiceNumber,
                        customerName = debtCustomerName.trim(),
                        totalDebt = transaction.finalAmount,
                        paidAmount = 0,
                        isSettled = false,
                        note = transaction.note,
                        createdAt = transaction.timestamp
                    )
                )
            }

            transactionId
        }
    }

    /**
     * RESTOCK / KULAKAN BARANG:
     * Menambah stok barang masuk dan mencatat histori mutasi secara otomatis
     */
    suspend fun restockProduct(
        productId: Long,
        addedQuantity: Int,
        newCostPrice: Long,
        note: String
    ) {
        database.withTransaction {
            val product = productDao.getProductById(productId) ?: return@withTransaction
            val prevStock = product.currentStock
            val newStock = prevStock + addedQuantity

            productDao.restockProduct(
                id = productId,
                addedStock = addedQuantity,
                newCostPrice = newCostPrice,
                updatedAt = System.currentTimeMillis()
            )

            productDao.insertStockMutation(
                StockMutation(
                    productId = productId,
                    productName = product.name,
                    type = MutationType.RESTOCK,
                    changeQuantity = addedQuantity,
                    previousStock = prevStock,
                    resultingStock = newStock,
                    referenceId = "RESTOCK-${System.currentTimeMillis() % 100000}",
                    note = if (note.isBlank()) "Kulakan / Tambah Stok" else note,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    /**
     * PENYESUAIAN STOK OPNAME / RUSAK / KADALUARSA:
     * Update stok manual dengan pencatatan alasan perubahan stok
     */
    suspend fun adjustStock(
        productId: Long,
        newStock: Int,
        reason: String
    ) {
        database.withTransaction {
            val product = productDao.getProductById(productId) ?: return@withTransaction
            val prevStock = product.currentStock
            val diff = newStock - prevStock

            productDao.updateStock(
                id = productId,
                newStock = newStock,
                updatedAt = System.currentTimeMillis()
            )

            productDao.insertStockMutation(
                StockMutation(
                    productId = productId,
                    productName = product.name,
                    type = MutationType.PENYESUAIAN,
                    changeQuantity = diff,
                    previousStock = prevStock,
                    resultingStock = newStock,
                    referenceId = "ADJUST",
                    note = if (reason.isBlank()) "Penyesuaian Stok Opname" else reason,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    /**
     * BATALKAN TRANSAKSI (VOID):
     * Otomatis mengembalikan stok yang sudah terjual kembali ke inventaris
     */
    suspend fun voidTransaction(transactionId: Long, reason: String) {
        database.withTransaction {
            val trx = transactionDao.getTransactionById(transactionId) ?: return@withTransaction
            if (trx.status == TransactionStatus.DIBATALKAN) return@withTransaction

            val items = transactionDao.getItemsForTransactionSync(transactionId)

            // Kembalikan stok untuk setiap item
            for (item in items) {
                val product = productDao.getProductById(item.productId)
                if (product != null) {
                    val prevStock = product.currentStock
                    val restoredStock = prevStock + item.quantity

                    productDao.updateStock(
                        id = item.productId,
                        newStock = restoredStock,
                        updatedAt = System.currentTimeMillis()
                    )

                    // Catat mutasi pengembalian stok
                    productDao.insertStockMutation(
                        StockMutation(
                            productId = item.productId,
                            productName = product.name,
                            type = MutationType.BATAL_TRANSAKSI,
                            changeQuantity = item.quantity,
                            previousStock = prevStock,
                            resultingStock = restoredStock,
                            referenceId = trx.invoiceNumber,
                            note = "Stok dikembalikan: Pembatalan #${trx.invoiceNumber} ($reason)",
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
            }

            transactionDao.updateTransactionStatus(transactionId, TransactionStatus.DIBATALKAN)
        }
    }

    /**
     * BAYAR KASBON / PELUNASAN HUTANG
     */
    suspend fun settleDebt(debtId: Long, paymentAmount: Long, isFullySettled: Boolean) {
        val now = System.currentTimeMillis()
        transactionDao.settleDebt(
            debtId = debtId,
            paidAmount = paymentAmount,
            isSettled = isFullySettled,
            settledAt = if (isFullySettled) now else null
        )
    }
}
