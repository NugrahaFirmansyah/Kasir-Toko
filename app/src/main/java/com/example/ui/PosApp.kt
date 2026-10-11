package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.CashierScreen
import com.example.ui.screens.CustomerDebtScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StockInventoryScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.KelontongAmber
import com.example.ui.theme.KelontongGreenContainer
import com.example.ui.theme.KelontongGreenDark
import com.example.ui.theme.StockRed
import com.example.ui.viewmodel.PosViewModel

enum class PosNavDestination(
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    CASHIER("Kasir", Icons.Default.PointOfSale, "nav_cashier"),
    STOCK("Stok", Icons.Default.Inventory, "nav_stock"),
    TRANSACTIONS("Transaksi", Icons.Default.ReceiptLong, "nav_transactions"),
    DEBTS("Kasbon", Icons.Default.AccountBalanceWallet, "nav_debts"),
    REPORTS("Laporan", Icons.Default.Assessment, "nav_reports"),
    SETTINGS("Toko", Icons.Default.Store, "nav_settings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosApp(
    viewModel: PosViewModel = viewModel()
) {
    var currentDestination by remember { mutableStateOf(PosNavDestination.CASHIER) }
    val storeName by viewModel.storeName.collectAsStateWithLifecycle()
    val lowStockCount by viewModel.lowStockCount.collectAsStateWithLifecycle()
    val unsettledDebtCount by viewModel.unsettledDebtCount.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    // Handle back button: if not on Cashier, go back to Cashier
    BackHandler(enabled = currentDestination != PosNavDestination.CASHIER) {
        currentDestination = PosNavDestination.CASHIER
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when (currentDestination) {
                            PosNavDestination.CASHIER -> storeName
                            PosNavDestination.STOCK -> "Inventaris & Stok Otomatis"
                            PosNavDestination.TRANSACTIONS -> "Riwayat Transaksi"
                            PosNavDestination.DEBTS -> "Buku Kasbon Pelanggan"
                            PosNavDestination.REPORTS -> "Laporan & Audit Mutasi"
                            PosNavDestination.SETTINGS -> "Pengaturan Toko"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        maxLines = 1
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                PosNavDestination.values().forEach { destination ->
                    val isSelected = currentDestination == destination

                    val badgeCount = when (destination) {
                        PosNavDestination.STOCK -> lowStockCount
                        PosNavDestination.DEBTS -> unsettledDebtCount
                        else -> 0
                    }

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentDestination = destination },
                        icon = {
                            if (badgeCount > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge(
                                            containerColor = if (destination == PosNavDestination.STOCK) StockRed else KelontongAmber
                                        ) {
                                            Text("$badgeCount", fontSize = 10.sp)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = destination.icon,
                                        contentDescription = destination.title,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = destination.title,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        },
                        label = {
                            Text(
                                text = destination.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = KelontongGreenDark,
                            selectedTextColor = KelontongGreenDark,
                            indicatorColor = KelontongGreenContainer
                        ),
                        modifier = Modifier.testTag(destination.testTag)
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentDestination) {
                PosNavDestination.CASHIER -> CashierScreen(
                    viewModel = viewModel,
                    onNavigateToStock = { currentDestination = PosNavDestination.STOCK }
                )
                PosNavDestination.STOCK -> StockInventoryScreen(
                    viewModel = viewModel
                )
                PosNavDestination.TRANSACTIONS -> TransactionsScreen(
                    viewModel = viewModel
                )
                PosNavDestination.DEBTS -> CustomerDebtScreen(
                    viewModel = viewModel
                )
                PosNavDestination.REPORTS -> ReportsScreen(
                    viewModel = viewModel
                )
                PosNavDestination.SETTINGS -> SettingsScreen(
                    viewModel = viewModel
                )
            }
        }
    }
}
