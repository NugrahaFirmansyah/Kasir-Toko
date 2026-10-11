package com.example.ui.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CurrencyFormatter {
    private val rupiahFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }

    private val numberFormat: NumberFormat = NumberFormat.getNumberInstance(Locale("id", "ID"))

    fun formatRupiah(amount: Long): String {
        return try {
            rupiahFormat.format(amount).replace("Rp", "Rp ")
        } catch (e: Exception) {
            "Rp $amount"
        }
    }

    fun formatNumber(number: Long): String {
        return try {
            numberFormat.format(number)
        } catch (e: Exception) {
            number.toString()
        }
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        return sdf.format(Date(timestamp))
    }

    fun formatDateOnly(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        return sdf.format(Date(timestamp))
    }

    fun formatTimeOnly(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale("id", "ID"))
        return sdf.format(Date(timestamp))
    }

    fun formatMonthYear(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
        return sdf.format(Date(timestamp))
    }

    fun formatYearMonthKey(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun generateInvoiceNumber(): String {
        val datePart = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(Date())
        val randomSuffix = (100..999).random()
        return "TRX-$datePart-$randomSuffix"
    }
}
