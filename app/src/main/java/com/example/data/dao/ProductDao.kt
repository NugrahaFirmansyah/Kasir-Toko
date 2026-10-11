package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Product
import com.example.data.model.StockMutation
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE isAvailable = 1 ORDER BY name ASC")
    fun getAvailableProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): Product?

    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): Product?

    @Query("SELECT * FROM products WHERE currentStock <= minStockAlert ORDER BY currentStock ASC")
    fun getLowStockProducts(): Flow<List<Product>>

    @Query("SELECT COUNT(*) FROM products WHERE currentStock <= minStockAlert")
    fun getLowStockCount(): Flow<Int>

    @Query("SELECT * FROM products WHERE category = :category ORDER BY name ASC")
    fun getProductsByCategory(category: String): Flow<List<Product>>

    @Query("""
        SELECT * FROM products 
        WHERE name LIKE '%' || :query || '%' 
           OR barcode LIKE '%' || :query || '%' 
           OR category LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    fun searchProducts(query: String): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>): List<Long>

    @Update
    suspend fun updateProduct(product: Product): Int

    @Delete
    suspend fun deleteProduct(product: Product): Int

    @Query("UPDATE products SET currentStock = :newStock, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStock(id: Long, newStock: Int, updatedAt: Long = System.currentTimeMillis()): Int

    @Query("UPDATE products SET currentStock = currentStock + :addedStock, costPrice = CASE WHEN :newCostPrice > 0 THEN :newCostPrice ELSE costPrice END, updatedAt = :updatedAt WHERE id = :id")
    suspend fun restockProduct(id: Long, addedStock: Int, newCostPrice: Long, updatedAt: Long = System.currentTimeMillis()): Int

    // Stock Mutations (Pencatatan Otomatis)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockMutation(mutation: StockMutation): Long

    @Query("SELECT * FROM stock_mutations ORDER BY timestamp DESC")
    fun getAllStockMutations(): Flow<List<StockMutation>>

    @Query("SELECT * FROM stock_mutations ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentStockMutations(limit: Int = 100): Flow<List<StockMutation>>

    @Query("SELECT * FROM stock_mutations WHERE productId = :productId ORDER BY timestamp DESC")
    fun getMutationsForProduct(productId: Long): Flow<List<StockMutation>>
}
