package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class MutationType {
    PENJUALAN,       // Berkurang otomatis saat kasir checkout
    RESTOCK,         // Bertambah saat kulakan / belanja stok baru
    PENYESUAIAN,     // Penyesuaian stok opname / barang rusak / kadaluarsa
    BATAL_TRANSAKSI  // Dikembalikan otomatis saat transaksi dibatalkan / void
}

@Entity(
    tableName = "stock_mutations",
    foreignKeys = [
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["productId"]),
        Index(value = ["timestamp"])
    ]
)
data class StockMutation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val productName: String,
    val type: MutationType,
    val changeQuantity: Int, // Negatif untuk penjualan, Positif untuk restock/retur
    val previousStock: Int,
    val resultingStock: Int,
    val referenceId: String = "", // e.g. "TRX-20261011-001"
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
