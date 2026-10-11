package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.ProductDao
import com.example.data.dao.TransactionDao
import com.example.data.model.CustomerDebt
import com.example.data.model.MutationType
import com.example.data.model.PaymentMethod
import com.example.data.model.Product
import com.example.data.model.StockMutation
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionRecord
import com.example.data.model.TransactionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Product::class,
        StockMutation::class,
        TransactionRecord::class,
        TransactionItem::class,
        CustomerDebt::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pos_kelontong_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.productDao(), database.transactionDao())
                }
            }
        }

        private suspend fun populateInitialData(
            productDao: ProductDao,
            transactionDao: TransactionDao
        ) {
            val initialProducts = listOf(
                // Sembako
                Product(
                    barcode = "8991001",
                    name = "Beras Rojolele Super 5kg",
                    category = "Sembako",
                    unit = "Sak",
                    costPrice = 68000,
                    sellingPrice = 75000,
                    currentStock = 20,
                    minStockAlert = 5
                ),
                Product(
                    barcode = "8991002",
                    name = "Minyak Goreng Sania 2L",
                    category = "Sembako",
                    unit = "Pouch",
                    costPrice = 33000,
                    sellingPrice = 37000,
                    currentStock = 18,
                    minStockAlert = 6
                ),
                Product(
                    barcode = "8991003",
                    name = "Minyak Kita 1 Liter",
                    category = "Sembako",
                    unit = "Botol",
                    costPrice = 14500,
                    sellingPrice = 16000,
                    currentStock = 25,
                    minStockAlert = 8
                ),
                Product(
                    barcode = "8991004",
                    name = "Gula Pasir Gulaku Kuning 1kg",
                    category = "Sembako",
                    unit = "Bungkus",
                    costPrice = 16000,
                    sellingPrice = 18000,
                    currentStock = 30,
                    minStockAlert = 10
                ),
                Product(
                    barcode = "8991005",
                    name = "Telur Ayam Negeri 1kg",
                    category = "Sembako",
                    unit = "Kg",
                    costPrice = 27000,
                    sellingPrice = 30000,
                    currentStock = 15,
                    minStockAlert = 5
                ),
                Product(
                    barcode = "8991006",
                    name = "Tepung Terigu Segitiga Biru 1kg",
                    category = "Sembako",
                    unit = "Bungkus",
                    costPrice = 11000,
                    sellingPrice = 13000,
                    currentStock = 22,
                    minStockAlert = 5
                ),

                // Mie & Makanan
                Product(
                    barcode = "8991007",
                    name = "Indomie Goreng Spesial",
                    category = "Makanan",
                    unit = "Bungkus",
                    costPrice = 2900,
                    sellingPrice = 3500,
                    currentStock = 45,
                    minStockAlert = 15
                ),
                Product(
                    barcode = "8991008",
                    name = "Indomie Ayam Bawang",
                    category = "Makanan",
                    unit = "Bungkus",
                    costPrice = 2800,
                    sellingPrice = 3500,
                    currentStock = 40,
                    minStockAlert = 15
                ),
                Product(
                    barcode = "8991009",
                    name = "Sedaap Goreng Krispi",
                    category = "Makanan",
                    unit = "Bungkus",
                    costPrice = 2800,
                    sellingPrice = 3500,
                    currentStock = 35,
                    minStockAlert = 10
                ),

                // Minuman
                Product(
                    barcode = "8991010",
                    name = "Aqua Botol 600ml",
                    category = "Minuman",
                    unit = "Botol",
                    costPrice = 3000,
                    sellingPrice = 4000,
                    currentStock = 48,
                    minStockAlert = 12
                ),
                Product(
                    barcode = "8991011",
                    name = "Teh Pucuk Harum 350ml",
                    category = "Minuman",
                    unit = "Botol",
                    costPrice = 3200,
                    sellingPrice = 4000,
                    currentStock = 36,
                    minStockAlert = 10
                ),
                Product(
                    barcode = "8991012",
                    name = "Kopi Kapal Api Special Mix Renceng",
                    category = "Minuman",
                    unit = "Renceng",
                    costPrice = 12000,
                    sellingPrice = 14500,
                    currentStock = 14,
                    minStockAlert = 4
                ),
                Product(
                    barcode = "8991013",
                    name = "Susu Kental Manis Frisian Flag Cokelat 370g",
                    category = "Minuman",
                    unit = "Kaleng",
                    costPrice = 11500,
                    sellingPrice = 13500,
                    currentStock = 16,
                    minStockAlert = 5
                ),

                // Bumbu Dapur
                Product(
                    barcode = "8991014",
                    name = "Royco Rasa Ayam 230g",
                    category = "Bumbu",
                    unit = "Bungkus",
                    costPrice = 9000,
                    sellingPrice = 11000,
                    currentStock = 20,
                    minStockAlert = 5
                ),
                Product(
                    barcode = "8991015",
                    name = "Kecap Bango Manis 550ml",
                    category = "Bumbu",
                    unit = "Pouch",
                    costPrice = 21000,
                    sellingPrice = 24500,
                    currentStock = 12,
                    minStockAlert = 4
                ),
                Product(
                    barcode = "8991016",
                    name = "Garam Dapur Cap Kapal 250g",
                    category = "Bumbu",
                    unit = "Bungkus",
                    costPrice = 2500,
                    sellingPrice = 3500,
                    currentStock = 25,
                    minStockAlert = 5
                ),

                // Sabun & Kebutuhan Rumah (Beberapa stok dibuat sedikit untuk demonstrasi peringatan stok otomatis)
                Product(
                    barcode = "8991017",
                    name = "Deterjen Daia Putih 800g",
                    category = "Perlengkapan",
                    unit = "Bungkus",
                    costPrice = 16000,
                    sellingPrice = 18500,
                    currentStock = 3, // Menipis (<= minStockAlert 5)
                    minStockAlert = 5
                ),
                Product(
                    barcode = "8991018",
                    name = "Sabun Mandi Lifebuoy Total 10 110g",
                    category = "Perlengkapan",
                    unit = "Pcs",
                    costPrice = 4000,
                    sellingPrice = 5000,
                    currentStock = 2, // Menipis
                    minStockAlert = 5
                ),
                Product(
                    barcode = "8991019",
                    name = "Sunlight Pencuci Piring Jeruk Nipis 750ml",
                    category = "Perlengkapan",
                    unit = "Pouch",
                    costPrice = 13500,
                    sellingPrice = 16000,
                    currentStock = 0, // Habis untuk demo alert
                    minStockAlert = 4
                ),
                Product(
                    barcode = "8991020",
                    name = "Gas Elpiji 3kg (Isi Ulang)",
                    category = "Perlengkapan",
                    unit = "Tabung",
                    costPrice = 19000,
                    sellingPrice = 22000,
                    currentStock = 8,
                    minStockAlert = 3
                )
            )

            val insertedIds = productDao.insertProducts(initialProducts)

            // Catat mutasi stok awal (Initial Stock Movement Log)
            initialProducts.forEachIndexed { index, product ->
                val id = insertedIds.getOrElse(index) { (index + 1).toLong() }
                productDao.insertStockMutation(
                    StockMutation(
                        productId = id,
                        productName = product.name,
                        type = MutationType.RESTOCK,
                        changeQuantity = product.currentStock,
                        previousStock = 0,
                        resultingStock = product.currentStock,
                        referenceId = "INIT",
                        note = "Stok Awal Inventaris Toko",
                        timestamp = System.currentTimeMillis() - 86400000L
                    )
                )
            }

            // Tambahkan contoh transaksi perdana
            val sampleTrx = TransactionRecord(
                invoiceNumber = "TRX-20261011-001",
                subtotalAmount = 58000,
                discountAmount = 0,
                finalAmount = 58000,
                paidAmount = 60000,
                changeAmount = 2000,
                paymentMethod = PaymentMethod.TUNAI,
                customerName = "Pak RT Bambang",
                cashierName = "Bu Santi",
                note = "Belanja pagi",
                totalCostAmount = 50500,
                status = TransactionStatus.SELESAI,
                timestamp = System.currentTimeMillis() - 3600000L
            )
            val trxId = transactionDao.insertTransaction(sampleTrx)

            val sampleItems = listOf(
                TransactionItem(
                    transactionId = trxId,
                    productId = insertedIds.getOrElse(1) { 2L },
                    productName = "Minyak Goreng Sania 2L",
                    unit = "Pouch",
                    costPrice = 33000,
                    sellingPrice = 37000,
                    quantity = 1,
                    subtotal = 37000
                ),
                TransactionItem(
                    transactionId = trxId,
                    productId = insertedIds.getOrElse(6) { 7L },
                    productName = "Indomie Goreng Spesial",
                    unit = "Bungkus",
                    costPrice = 2900,
                    sellingPrice = 3500,
                    quantity = 6,
                    subtotal = 21000
                )
            )
            transactionDao.insertTransactionItems(sampleItems)

            // Catat contoh kasbon pelanggan
            val sampleDebtTrx = TransactionRecord(
                invoiceNumber = "TRX-20261011-002",
                subtotalAmount = 30000,
                discountAmount = 0,
                finalAmount = 30000,
                paidAmount = 0,
                changeAmount = 0,
                paymentMethod = PaymentMethod.KASBON,
                customerName = "Bu Siti (Blok C)",
                cashierName = "Bu Santi",
                note = "Kasbon sampai gajian hari Jumat",
                totalCostAmount = 27000,
                status = TransactionStatus.SELESAI,
                timestamp = System.currentTimeMillis() - 7200000L
            )
            val debtTrxId = transactionDao.insertTransaction(sampleDebtTrx)
            transactionDao.insertDebt(
                CustomerDebt(
                    transactionId = debtTrxId,
                    invoiceNumber = "TRX-20261011-002",
                    customerName = "Bu Siti (Blok C)",
                    totalDebt = 30000,
                    paidAmount = 0,
                    isSettled = false,
                    note = "Janji bayar hari Jumat",
                    createdAt = System.currentTimeMillis() - 7200000L
                )
            )
        }
    }
}
