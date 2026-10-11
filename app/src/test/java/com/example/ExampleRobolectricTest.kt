package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.util.CurrencyFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Kasir Kelontong", appName)
    }

    @Test
    fun `currency formatter formats rupiah properly`() {
        val formatted = CurrencyFormatter.formatRupiah(50000L)
        assertTrue(formatted.contains("50.000"))
    }
}
