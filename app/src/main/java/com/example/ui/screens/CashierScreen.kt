package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PaymentMethod
import com.example.data.model.Product
import com.example.ui.components.QuantityStepper
import com.example.ui.components.StockBadge
import com.example.ui.util.ProductImageHelper
import com.example.ui.theme.KelontongAmber
import com.example.ui.theme.KelontongAmberContainer
import com.example.ui.theme.KelontongGreenContainer
import com.example.ui.theme.KelontongGreenDark
import com.example.ui.theme.KelontongGreenPrimary
import com.example.ui.theme.KelontongOnAmberContainer
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.StockRed
import com.example.ui.theme.StockSafeGreen
import com.example.ui.theme.StockWarningContainer
import com.example.ui.theme.StockWarningOrange
import com.example.ui.util.CurrencyFormatter
import com.example.ui.viewmodel.CompletedReceipt
import com.example.ui.viewmodel.PosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashierScreen(
    viewModel: PosViewModel,
    onNavigateToStock: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val products by viewModel.filteredPosProducts.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val cart by viewModel.cart.collectAsStateWithLifecycle()
    val cartItemsList by viewModel.cartItemsList.collectAsStateWithLifecycle()
    val cartTotalQty by viewModel.cartTotalQuantity.collectAsStateWithLifecycle()
    val cartSubtotal by viewModel.cartSubtotal.collectAsStateWithLifecycle()
    val cartFinalAmount by viewModel.cartFinalAmount.collectAsStateWithLifecycle()
    val discountAmount by viewModel.discountAmount.collectAsStateWithLifecycle()
    val isCartSheetVisible by viewModel.isCartSheetVisible.collectAsStateWithLifecycle()
    val isCheckoutDialogVisible by viewModel.isCheckoutDialogVisible.collectAsStateWithLifecycle()
    val activeReceipt by viewModel.activeReceipt.collectAsStateWithLifecycle()
    val lowStockCount by viewModel.lowStockCount.collectAsStateWithLifecycle()

    val categories = listOf("Semua", "Sembako", "Makanan", "Minuman", "Bumbu", "Perlengkapan")

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Search Bar & Low Stock Alert Strip
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Cari barang atau scan barcode...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Cari",
                            tint = Slate500
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Hapus Pencarian")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cashier_search_input")
                )

                if (lowStockCount > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = StockWarningContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToStock() }
                            .testTag("low_stock_banner")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = StockWarningOrange,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Perhatian: $lowStockCount produk stok menipis / habis!",
                                color = StockWarningOrange,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "Kelola Stok",
                                color = KelontongGreenDark,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Category Chips Row
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) KelontongGreenDark else Slate100,
                        modifier = Modifier
                            .clickable { viewModel.setSelectedCategory(category) }
                            .testTag("category_chip_$category")
                    ) {
                        Text(
                            text = category,
                            color = if (isSelected) Color.White else Slate700,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // Product Grid
            if (products.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PointOfSale,
                            contentDescription = null,
                            tint = Slate500,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tidak ada produk yang cocok",
                            fontWeight = FontWeight.SemiBold,
                            color = Slate700
                        )
                        Text(
                            text = "Coba ubah kata kunci pencarian atau kategori",
                            fontSize = 13.sp,
                            color = Slate500
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("cashier_product_grid")
                ) {
                    items(products, key = { it.id }) { product ->
                        val cartItem = cart[product.id]
                        val qtyInCart = cartItem?.quantity ?: 0
                        ProductCashierCard(
                            product = product,
                            quantityInCart = qtyInCart,
                            onAddToCart = { viewModel.addToCart(product) },
                            onDecrease = { viewModel.decreaseCartItem(product) }
                        )
                    }
                }
            }
        }

        // Bottom Bar Cart Summary (Floats above when items are in cart)
        if (cartTotalQty > 0) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = KelontongGreenDark,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "$cartTotalQty Barang di Keranjang",
                                fontSize = 12.sp,
                                color = Slate500,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = CurrencyFormatter.formatRupiah(cartFinalAmount),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = KelontongGreenDark
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.openCartSheet() },
                        colors = ButtonDefaults.buttonColors(containerColor = KelontongGreenDark),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("btn_view_cart")
                    ) {
                        Text(
                            text = "Bayar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }

        // Cart Bottom Sheet
        if (isCartSheetVisible) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.closeCartSheet() }
            ) {
                CartSheetContent(
                    items = cartItemsList,
                    subtotal = cartSubtotal,
                    discount = discountAmount,
                    finalAmount = cartFinalAmount,
                    onIncrease = { viewModel.addToCart(it.product) },
                    onDecrease = { viewModel.decreaseCartItem(it.product) },
                    onRemove = { viewModel.removeCartItem(it.product.id) },
                    onSetDiscount = { viewModel.setDiscount(it) },
                    onClearCart = { viewModel.clearCart() },
                    onCheckout = { viewModel.openCheckoutDialog() }
                )
            }
        }

        // Checkout Dialog
        if (isCheckoutDialogVisible) {
            CheckoutDialog(
                finalAmount = cartFinalAmount,
                onDismiss = { viewModel.closeCheckoutDialog() },
                onConfirm = { method, paid, custName, note ->
                    viewModel.processCheckout(method, paid, custName, note)
                }
            )
        }

        // Struk / Receipt Dialog
        if (activeReceipt != null) {
            ReceiptDialog(
                receipt = activeReceipt!!,
                storeName = viewModel.storeName.collectAsStateWithLifecycle().value,
                storeAddress = viewModel.storeAddress.collectAsStateWithLifecycle().value,
                storePhone = viewModel.storePhone.collectAsStateWithLifecycle().value,
                footerNote = viewModel.receiptFooter.collectAsStateWithLifecycle().value,
                onDismiss = { viewModel.closeReceiptDialog() },
                onShare = {
                    viewModel.shareReceipt(context, activeReceipt!!.transaction, activeReceipt!!.items)
                }
            )
        }
    }
}

@Composable
fun ProductCashierCard(
    product: Product,
    quantityInCart: Int,
    onAddToCart: () -> Unit,
    onDecrease: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOutOfStock = product.currentStock <= 0

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOutOfStock) Slate100 else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isOutOfStock) 0.dp else 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("product_card_${product.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Product Photo
            val photoRes = ProductImageHelper.getDrawableForProduct(product)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Slate100),
                contentAlignment = Alignment.Center
            ) {
                if (photoRes != null) {
                    Image(
                        painter = painterResource(id = photoRes),
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = Slate500,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Category & Barcode
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Slate100
                ) {
                    Text(
                        text = product.category,
                        fontSize = 10.sp,
                        color = Slate700,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (product.barcode.isNotBlank()) {
                    Text(
                        text = "#${product.barcode}",
                        fontSize = 10.sp,
                        color = Slate500
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Product Name
            Text(
                text = product.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = if (isOutOfStock) Slate500 else Slate800,
                modifier = Modifier.height(40.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Selling Price
            Text(
                text = CurrencyFormatter.formatRupiah(product.sellingPrice),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = if (isOutOfStock) Slate500 else KelontongGreenDark
            )
            Text(
                text = "per ${product.unit}",
                fontSize = 11.sp,
                color = Slate500
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Real-time Stock Badge
            StockBadge(
                currentStock = product.currentStock,
                minStockAlert = product.minStockAlert,
                unit = product.unit,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action: Add / Stepper
            if (isOutOfStock) {
                Surface(
                    color = Slate200,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Stok Habis",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate500,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else if (quantityInCart > 0) {
                QuantityStepper(
                    quantity = quantityInCart,
                    onIncrease = onAddToCart,
                    onDecrease = onDecrease,
                    enabledIncrease = quantityInCart < product.currentStock,
                    modifier = Modifier.fillMaxWidth(),
                    testTagPrefix = "card_stepper_${product.id}"
                )
            } else {
                Button(
                    onClick = onAddToCart,
                    colors = ButtonDefaults.buttonColors(containerColor = KelontongGreenDark),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_add_card_${product.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Tambah", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CartSheetContent(
    items: List<com.example.ui.viewmodel.CartItem>,
    subtotal: Long,
    discount: Long,
    finalAmount: Long,
    onIncrease: (com.example.ui.viewmodel.CartItem) -> Unit,
    onDecrease: (com.example.ui.viewmodel.CartItem) -> Unit,
    onRemove: (com.example.ui.viewmodel.CartItem) -> Unit,
    onSetDiscount: (Long) -> Unit,
    onClearCart: () -> Unit,
    onCheckout: () -> Unit
) {
    var discountInput by remember { mutableStateOf(if (discount > 0) discount.toString() else "") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Keranjang Belanja",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onClearCart) {
                Text("Kosongkan", color = StockRed)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Items list
        items.forEach { cartItem ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                val photoRes = ProductImageHelper.getDrawableForProduct(cartItem.product)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Slate100),
                    contentAlignment = Alignment.Center
                ) {
                    if (photoRes != null) {
                        Image(
                            painter = painterResource(id = photoRes),
                            contentDescription = cartItem.product.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = Slate500,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = cartItem.product.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${CurrencyFormatter.formatRupiah(cartItem.product.sellingPrice)} x ${cartItem.quantity} ${cartItem.product.unit}",
                        fontSize = 12.sp,
                        color = Slate500
                    )
                    Text(
                        text = CurrencyFormatter.formatRupiah(cartItem.subtotal),
                        fontWeight = FontWeight.Bold,
                        color = KelontongGreenDark,
                        fontSize = 14.sp
                    )
                }

                QuantityStepper(
                    quantity = cartItem.quantity,
                    onIncrease = { onIncrease(cartItem) },
                    onDecrease = { onDecrease(cartItem) },
                    enabledIncrease = cartItem.quantity < cartItem.product.currentStock,
                    testTagPrefix = "sheet_stepper_${cartItem.product.id}"
                )

                IconButton(onClick = { onRemove(cartItem) }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus",
                        tint = Slate500,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            HorizontalDivider(color = Slate200)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Diskon Input
        OutlinedTextField(
            value = discountInput,
            onValueChange = {
                discountInput = it
                val parsed = it.toLongOrNull() ?: 0L
                onSetDiscount(parsed)
            },
            label = { Text("Potongan / Diskon (Rp)") },
            placeholder = { Text("0") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Summary
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Subtotal", color = Slate500)
            Text(CurrencyFormatter.formatRupiah(subtotal), fontWeight = FontWeight.SemiBold)
        }

        if (discount > 0) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            ) {
                Text("Diskon", color = StockRed)
                Text("-${CurrencyFormatter.formatRupiah(discount)}", color = StockRed, fontWeight = FontWeight.SemiBold)
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Total Pembayaran", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(
                CurrencyFormatter.formatRupiah(finalAmount),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = KelontongGreenDark
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onCheckout,
            colors = ButtonDefaults.buttonColors(containerColor = KelontongGreenDark),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("btn_proceed_checkout")
        ) {
            Text(
                text = "Lanjut Pembayaran (${CurrencyFormatter.formatRupiah(finalAmount)})",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun CheckoutDialog(
    finalAmount: Long,
    onDismiss: () -> Unit,
    onConfirm: (PaymentMethod, Long, String, String) -> Unit
) {
    var selectedMethod by remember { mutableStateOf(PaymentMethod.TUNAI) }
    var paidAmountText by remember { mutableStateOf("") }
    var customerName by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val paidAmount = paidAmountText.toLongOrNull() ?: 0L
    val changeAmount = (paidAmount - finalAmount).coerceAtLeast(0L)
    val isCashValid = selectedMethod != PaymentMethod.TUNAI || paidAmount >= finalAmount
    val isKasbonValid = selectedMethod != PaymentMethod.KASBON || customerName.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Pembayaran Kasir",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
            ) {
                // Total to pay banner
                Surface(
                    color = KelontongGreenContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("Total Tagihan", fontSize = 12.sp, color = KelontongGreenDark)
                        Text(
                            text = CurrencyFormatter.formatRupiah(finalAmount),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = KelontongGreenDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Metode Pembayaran:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))

                // Payment Methods
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        PaymentMethod.TUNAI to "Tunai",
                        PaymentMethod.QRIS to "QRIS",
                        PaymentMethod.TRANSFER to "Transfer",
                        PaymentMethod.KASBON to "Kasbon"
                    ).forEach { (method, label) ->
                        val isSelected = selectedMethod == method
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) {
                                if (method == PaymentMethod.KASBON) KelontongAmberContainer else KelontongGreenDark
                            } else Slate100,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedMethod = method
                                    if (method == PaymentMethod.TUNAI && paidAmountText.isBlank()) {
                                        paidAmountText = finalAmount.toString()
                                    }
                                }
                                .testTag("pay_method_${method.name}")
                        ) {
                            Text(
                                text = label,
                                textAlign = TextAlign.Center,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) {
                                    if (method == PaymentMethod.KASBON) KelontongOnAmberContainer else Color.White
                                } else Slate700,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Cash specific input
                if (selectedMethod == PaymentMethod.TUNAI) {
                    Text("Uang Diterima (Rp):", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = paidAmountText,
                        onValueChange = { paidAmountText = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        placeholder = { Text("Contoh: 50000") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_cash_paid")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick money buttons
                    val quickAmounts = listOf(
                        finalAmount to "Uang Pas",
                        10000L to "10rb",
                        20000L to "20rb",
                        50000L to "50rb",
                        100000L to "100rb"
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(quickAmounts) { (amt, lbl) ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Slate200,
                                modifier = Modifier.clickable {
                                    paidAmountText = amt.toString()
                                }
                            ) {
                                Text(
                                    text = lbl,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Kembalian
                    Surface(
                        color = if (paidAmount >= finalAmount) StockSafeGreen.copy(alpha = 0.1f) else Slate100,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text("Kembalian:", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                            Text(
                                text = CurrencyFormatter.formatRupiah(changeAmount),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (paidAmount >= finalAmount) StockSafeGreen else Slate500
                            )
                        }
                    }
                }

                // Kasbon specific input
                if (selectedMethod == PaymentMethod.KASBON) {
                    Surface(
                        color = KelontongAmberContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Catatan: Transaksi ini akan dicatat ke Buku Kasbon & stok barang tetap dikurangi otomatis.",
                            color = KelontongOnAmberContainer,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = {
                        Text(if (selectedMethod == PaymentMethod.KASBON) "Nama Pelanggan (Wajib)" else "Nama Pelanggan (Opsional)")
                    },
                    placeholder = { Text("Contoh: Bu Siti / Pak Budi") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_customer_name")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Catatan Transaksi (Opsional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalPaid = when (selectedMethod) {
                        PaymentMethod.TUNAI -> paidAmount
                        PaymentMethod.KASBON -> 0L
                        else -> finalAmount
                    }
                    onConfirm(selectedMethod, finalPaid, customerName, note)
                },
                enabled = isCashValid && isKasbonValid,
                colors = ButtonDefaults.buttonColors(containerColor = KelontongGreenDark),
                modifier = Modifier.testTag("btn_confirm_checkout")
            ) {
                Text("Proses & Potong Stok")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun ReceiptDialog(
    receipt: CompletedReceipt,
    storeName: String,
    storeAddress: String,
    storePhone: String,
    footerNote: String,
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    val trx = receipt.transaction

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = StockSafeGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Transaksi Berhasil!", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Auto Stock Deducted Banner
                Surface(
                    color = KelontongGreenContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = KelontongGreenDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Stok barang otomatis berkurang di inventaris!",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = KelontongGreenDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Paper Struk Card
                Surface(
                    color = Slate100,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = storeName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = storeAddress,
                            fontSize = 11.sp,
                            color = Slate500,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Telp: $storePhone",
                            fontSize = 11.sp,
                            color = Slate500,
                            textAlign = TextAlign.Center
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Slate200
                        )

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("No: ${trx.invoiceNumber}", fontSize = 11.sp, color = Slate700)
                            Text(CurrencyFormatter.formatDate(trx.timestamp), fontSize = 11.sp, color = Slate700)
                        }

                        if (trx.customerName.isNotBlank()) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
                            ) {
                                Text("Pelanggan:", fontSize = 11.sp, color = Slate700)
                                Text(trx.customerName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
                        ) {
                            Text("Metode:", fontSize = 11.sp, color = Slate700)
                            Text(trx.paymentMethod.name, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Slate200
                        )

                        // Items
                        receipt.items.forEach { item ->
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.productName, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Text(
                                        "${item.quantity} ${item.unit} x ${CurrencyFormatter.formatRupiah(item.sellingPrice)}",
                                        fontSize = 10.sp,
                                        color = Slate500
                                    )
                                }
                                Text(
                                    CurrencyFormatter.formatRupiah(item.subtotal),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Slate200
                        )

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("TOTAL", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                CurrencyFormatter.formatRupiah(trx.finalAmount),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = KelontongGreenDark
                            )
                        }

                        if (trx.paymentMethod == PaymentMethod.TUNAI) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
                            ) {
                                Text("Bayar", fontSize = 11.sp, color = Slate500)
                                Text(CurrencyFormatter.formatRupiah(trx.paidAmount), fontSize = 11.sp)
                            }
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
                            ) {
                                Text("Kembalian", fontSize = 11.sp, color = Slate500)
                                Text(CurrencyFormatter.formatRupiah(trx.changeAmount), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = footerNote,
                            fontSize = 10.sp,
                            color = Slate500,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onShare,
                colors = ButtonDefaults.buttonColors(containerColor = KelontongGreenDark),
                modifier = Modifier.testTag("btn_share_receipt")
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Bagikan Struk")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Transaksi Baru")
            }
        }
    )
}
