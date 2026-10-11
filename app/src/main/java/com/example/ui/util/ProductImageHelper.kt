package com.example.ui.util

import androidx.annotation.DrawableRes
import com.example.R
import com.example.data.model.Product

object ProductImageHelper {

    val availableImagePresets = listOf(
        "Beras" to "prod_beras_1791740908510",
        "Minyak Goreng" to "prod_minyak_1791740934918",
        "Mie Instan" to "prod_indomie_1791740960808",
        "Telur Ayam" to "prod_telur_1791740989688",
        "Air Mineral Aqua" to "prod_aqua_1791741023954",
        "Kopi Renceng" to "prod_kopi_1791741057902",
        "Sabun Mandi / Cuci" to "prod_sabun_1791741096842",
        "Gula Pasir" to "prod_gula_1791745823374",
        "Tepung Terigu" to "prod_terigu_1791745845237",
        "Teh Botol / Teh Pucuk" to "prod_teh_1791745869516",
        "Kecap Manis" to "prod_kecap_1791745893147"
    )

    @DrawableRes
    val sampleProofDrawable: Int = R.drawable.sample_transfer_proof_1791745923126

    @DrawableRes
    fun getDrawableForProduct(product: Product): Int? {
        val preset = product.imageResName.trim()
        if (preset.isNotBlank()) {
            val resId = getDrawableByPresetName(preset)
            if (resId != null) return resId
        }

        // Auto-match based on product name keywords
        val lowerName = product.name.lowercase()
        return when {
            lowerName.contains("beras") -> R.drawable.prod_beras_1791740908510
            lowerName.contains("minyak") -> R.drawable.prod_minyak_1791740934918
            lowerName.contains("indomie") || lowerName.contains("mie") || lowerName.contains("sedaap") -> R.drawable.prod_indomie_1791740960808
            lowerName.contains("telur") -> R.drawable.prod_telur_1791740989688
            lowerName.contains("aqua") || lowerName.contains("mineral") || lowerName.contains("air") -> R.drawable.prod_aqua_1791741023954
            lowerName.contains("teh") -> R.drawable.prod_teh_1791745869516
            lowerName.contains("kopi") -> R.drawable.prod_kopi_1791741057902
            lowerName.contains("gula") -> R.drawable.prod_gula_1791745823374
            lowerName.contains("terigu") || lowerName.contains("tepung") -> R.drawable.prod_terigu_1791745845237
            lowerName.contains("kecap") -> R.drawable.prod_kecap_1791745893147
            lowerName.contains("sabun") || lowerName.contains("lifebuoy") || lowerName.contains("daia") || lowerName.contains("sunlight") || lowerName.contains("deterjen") -> R.drawable.prod_sabun_1791741096842
            else -> null
        }
    }

    @DrawableRes
    fun getDrawableByPresetName(name: String): Int? {
        return when (name) {
            "prod_beras_1791740908510" -> R.drawable.prod_beras_1791740908510
            "prod_minyak_1791740934918" -> R.drawable.prod_minyak_1791740934918
            "prod_indomie_1791740960808" -> R.drawable.prod_indomie_1791740960808
            "prod_telur_1791740989688" -> R.drawable.prod_telur_1791740989688
            "prod_aqua_1791741023954" -> R.drawable.prod_aqua_1791741023954
            "prod_teh_1791745869516" -> R.drawable.prod_teh_1791745869516
            "prod_kopi_1791741057902" -> R.drawable.prod_kopi_1791741057902
            "prod_gula_1791745823374" -> R.drawable.prod_gula_1791745823374
            "prod_terigu_1791745845237" -> R.drawable.prod_terigu_1791745845237
            "prod_kecap_1791745893147" -> R.drawable.prod_kecap_1791745893147
            "prod_sabun_1791741096842" -> R.drawable.prod_sabun_1791741096842
            else -> null
        }
    }
}
