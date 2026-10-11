package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class PaymentMethod {
    TUNAI,       // Cash
    QRIS,        // QRIS
    TRANSFER,    // Transfer Bank
    KASBON       // Hutang / Kasbon Pelanggan
}

enum class TransactionStatus {
    SELESAI,     // Sukses & stok sudah dikurangi
    DIBATALKAN   // Dibatalkan/void & stok dikembalikan otomatis
}

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["invoiceNumber"], unique = true),
        Index(value = ["timestamp"])
    ]
)
data class TransactionRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceNumber: String,
    val subtotalAmount: Long,
    val discountAmount: Long = 0,
    val finalAmount: Long,
    val paidAmount: Long,
    val changeAmount: Long,
    val paymentMethod: PaymentMethod,
    val customerName: String = "",
    val cashierName: String = "Kasir",
    val note: String = "",
    val totalCostAmount: Long = 0, // Total HPP untuk kalkulasi laba bersih
    val status: TransactionStatus = TransactionStatus.SELESAI,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "transaction_items",
    foreignKeys = [
        ForeignKey(
            entity = TransactionRecord::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["transactionId"]),
        Index(value = ["productId"])
    ]
)
data class TransactionItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val transactionId: Long,
    val productId: Long,
    val productName: String,
    val unit: String,
    val costPrice: Long,
    val sellingPrice: Long,
    val quantity: Int,
    val subtotal: Long
)
