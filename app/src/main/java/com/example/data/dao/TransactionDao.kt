package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CustomerDebt
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionRecord
import com.example.data.model.TransactionStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionItems(items: List<TransactionItem>): List<Long>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionRecord>>

    @Query("SELECT * FROM transactions WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    fun getTransactionsSince(startTime: Long): Flow<List<TransactionRecord>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): TransactionRecord?

    @Query("SELECT * FROM transactions WHERE invoiceNumber = :invoiceNumber LIMIT 1")
    suspend fun getTransactionByInvoice(invoiceNumber: String): TransactionRecord?

    @Query("UPDATE transactions SET status = :status WHERE id = :id")
    suspend fun updateTransactionStatus(id: Long, status: TransactionStatus): Int

    @Query("SELECT * FROM transaction_items WHERE transactionId = :transactionId")
    fun getItemsForTransaction(transactionId: Long): Flow<List<TransactionItem>>

    @Query("SELECT * FROM transaction_items WHERE transactionId = :transactionId")
    suspend fun getItemsForTransactionSync(transactionId: Long): List<TransactionItem>

    // Kasbon / Customer Debts
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: CustomerDebt): Long

    @Update
    suspend fun updateDebt(debt: CustomerDebt): Int

    @Query("SELECT * FROM customer_debts ORDER BY createdAt DESC")
    fun getAllDebts(): Flow<List<CustomerDebt>>

    @Query("SELECT * FROM customer_debts WHERE isSettled = 0 ORDER BY createdAt DESC")
    fun getUnsettledDebts(): Flow<List<CustomerDebt>>

    @Query("UPDATE customer_debts SET paidAmount = :paidAmount, isSettled = :isSettled, settledAt = :settledAt WHERE id = :debtId")
    suspend fun settleDebt(debtId: Long, paidAmount: Long, isSettled: Boolean, settledAt: Long?): Int

    @Query("SELECT COUNT(*) FROM customer_debts WHERE isSettled = 0")
    fun getUnsettledDebtCount(): Flow<Int>
}
