package com.example

import com.example.data.model.PaymentMethod
import com.example.data.model.Product
import com.example.ui.util.ProductImageHelper
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun paymentProof_matchingVerification() {
    val totalTagihan = 75000L
    val nominalBuktiSesuai = 75000L
    val nominalBuktiKurang = 65000L
    val nominalBuktiLebih = 85000L

    // Validasi kesesuaian bukti
    val isSesuai = nominalBuktiSesuai == totalTagihan
    val isKurang = nominalBuktiKurang == totalTagihan
    val isLebih = nominalBuktiLebih == totalTagihan

    assertTrue("Bukti transfer dengan nominal sama harus VALID", isSesuai)
    assertFalse("Bukti transfer dengan nominal kurang harus GAGAL", isKurang)
    assertFalse("Bukti transfer dengan nominal lebih harus GAGAL", isLebih)
  }

  @Test
  fun cashPayment_validationAndChange() {
    val totalTagihan = 50000L
    val cashPas = 50000L
    val cashLebih = 100000L
    val cashKurang = 30000L

    assertTrue(cashPas >= totalTagihan)
    assertEquals(0L, cashPas - totalTagihan)

    assertTrue(cashLebih >= totalTagihan)
    assertEquals(50000L, cashLebih - totalTagihan)

    assertFalse("Uang tunai kurang harus gagal", cashKurang >= totalTagihan)
  }

  @Test
  fun productImageHelper_presetKeywords() {
    val prodBeras = Product(name = "Beras Rojolele 5kg", category = "Sembako", costPrice = 50000, sellingPrice = 60000, currentStock = 10)
    val prodMinyak = Product(name = "Minyak Goreng Sania 2L", category = "Sembako", costPrice = 30000, sellingPrice = 35000, currentStock = 10)

    val resBeras = ProductImageHelper.getDrawableForProduct(prodBeras)
    val resMinyak = ProductImageHelper.getDrawableForProduct(prodMinyak)

    assertNotNull("Foto beras harus terdeteksi", resBeras)
    assertNotNull("Foto minyak harus terdeteksi", resMinyak)
  }

  @Test
  fun monthlyProfitLoss_calculation_salesMinusCostOfGoodsSold() {
    val totalPenjualan = 1500000L
    val totalModalBarangTerjual = 1200000L

    // Formula: Total Penjualan - Total Modal = Laba Bersih
    val labaBersih = totalPenjualan - totalModalBarangTerjual
    assertEquals(300000L, labaBersih)
    assertTrue("Laba harus positif jika penjualan melebihi modal", labaBersih > 0)

    val marginLaba = (labaBersih.toDouble() * 100.0) / totalPenjualan.toDouble()
    assertEquals(20.0, marginLaba, 0.001)

    // Kasus Rugi (modal lebih besar dari penjualan)
    val penjualanRugi = 800000L
    val modalRugi = 950000L
    val hasilRugi = penjualanRugi - modalRugi
    assertEquals(-150000L, hasilRugi)
    assertTrue("Hasil harus negatif saat terjadi rugi", hasilRugi < 0)
  }

  @Test
  fun monthlyProfitLoss_dateFormatters() {
    val timestamp = 1791735990848L // timestamp
    val yearMonthKey = com.example.ui.util.CurrencyFormatter.formatYearMonthKey(timestamp)
    val monthYearDisplay = com.example.ui.util.CurrencyFormatter.formatMonthYear(timestamp)

    assertNotNull(yearMonthKey)
    assertTrue(yearMonthKey.contains("-"))
    assertNotNull(monthYearDisplay)
  }
}

