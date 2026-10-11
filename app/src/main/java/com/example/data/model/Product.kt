package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val barcode: String = "",
    val name: String,
    val category: String, // Sembako, Minuman, Makanan Ringan, Bumbu Dapur, Perlengkapan, Lainnya
    val unit: String = "Pcs", // Pcs, Kg, Bungkus, Botol, Liter, Dus, Renceng, Butir
    val costPrice: Long, // Harga Beli / Modal (HPP)
    val sellingPrice: Long, // Harga Jual
    val currentStock: Int, // Sisa Stok Fisik (diupdate otomatis setiap transaksi/restock)
    val minStockAlert: Int = 5, // Batas peringatan stok menipis
    val imageResName: String = "", // Nama preset foto produk
    val isAvailable: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)
