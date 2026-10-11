package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "customer_debts",
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
        Index(value = ["customerName"])
    ]
)
data class CustomerDebt(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val transactionId: Long,
    val invoiceNumber: String,
    val customerName: String,
    val totalDebt: Long,
    val paidAmount: Long = 0,
    val isSettled: Boolean = false,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val settledAt: Long? = null
)
