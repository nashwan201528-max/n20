package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CalculationRecord
import com.example.ui.calculator.CalculatorViewModel
import com.example.ui.history.HistoryViewModel
import com.example.ui.theme.TealAccent
import com.example.ui.transactions.TransactionsViewModel

enum class MainDestination(val label: String, val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector, val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector) {
    CALCULATOR("Calculator", Icons.Filled.Calculate, Icons.Outlined.Calculate),
    TRANSACTIONS("Transactions", Icons.Filled.Payments, Icons.Outlined.Payments),
    HISTORY("History", Icons.Filled.History, Icons.Outlined.History)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    calculatorViewModel: CalculatorViewModel,
    transactionsViewModel: TransactionsViewModel,
    historyViewModel: HistoryViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentDestination by remember { mutableStateOf(MainDestination.CALCULATOR) }

    // Back handling: if in Transactions or History, Back press returns to Calculator
    BackHandler(enabled = currentDestination != MainDestination.CALCULATOR) {
        currentDestination = MainDestination.CALCULATOR
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        if (isWideScreen) {
            // Tablet / Foldable Landscape Layout: NavigationRail on side
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    header = {
                        IconButton(
                            onClick = onToggleTheme,
                            modifier = Modifier.testTag("btn_toggle_theme_rail")
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Theme",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    modifier = Modifier.testTag("main_navigation_rail")
                ) {
                    MainDestination.values().forEach { destination ->
                        NavigationRailItem(
                            selected = currentDestination == destination,
                            onClick = { currentDestination = destination },
                            icon = {
                                Icon(
                                    imageVector = if (currentDestination == destination) destination.selectedIcon else destination.unselectedIcon,
                                    contentDescription = destination.label
                                )
                            },
                            label = { Text(destination.label) },
                            modifier = Modifier.testTag("nav_${destination.name.lowercase()}")
                        )
                    }
                }

                VerticalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    ScreenContent(
                        destination = currentDestination,
                        calculatorViewModel = calculatorViewModel,
                        transactionsViewModel = transactionsViewModel,
                        historyViewModel = historyViewModel,
                        onNavigateToCalculator = { currentDestination = MainDestination.CALCULATOR }
                    )
                }
            }
        } else {
            // Standard Phone Portrait Layout: TopAppBar + Content + NavigationBar
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Calculate,
                                        contentDescription = null,
                                        tint = TealAccent
                                    )
                                    Text(
                                        text = currentDestination.label,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                }

                                IconButton(
                                    onClick = onToggleTheme,
                                    modifier = Modifier.testTag("btn_toggle_theme")
                                ) {
                                    Icon(
                                        imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                        contentDescription = "Toggle Theme"
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.testTag("main_bottom_nav")
                    ) {
                        MainDestination.values().forEach { destination ->
                            NavigationBarItem(
                                selected = currentDestination == destination,
                                onClick = { currentDestination = destination },
                                icon = {
                                    Icon(
                                        imageVector = if (currentDestination == destination) destination.selectedIcon else destination.unselectedIcon,
                                        contentDescription = destination.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = destination.label,
                                        fontWeight = if (currentDestination == destination) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                modifier = Modifier.testTag("nav_${destination.name.lowercase()}")
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    ScreenContent(
                        destination = currentDestination,
                        calculatorViewModel = calculatorViewModel,
                        transactionsViewModel = transactionsViewModel,
                        historyViewModel = historyViewModel,
                        onNavigateToCalculator = { currentDestination = MainDestination.CALCULATOR }
                    )
                }
            }
        }
    }
}

@Composable
private fun ScreenContent(
    destination: MainDestination,
    calculatorViewModel: CalculatorViewModel,
    transactionsViewModel: TransactionsViewModel,
    historyViewModel: HistoryViewModel,
    onNavigateToCalculator: () -> Unit
) {
    when (destination) {
        MainDestination.CALCULATOR -> {
            CalculatorScreen(viewModel = calculatorViewModel)
        }
        MainDestination.TRANSACTIONS -> {
            TransactionsScreen(viewModel = transactionsViewModel)
        }
        MainDestination.HISTORY -> {
            HistoryScreen(
                viewModel = historyViewModel,
                onReuseInCalculator = { record ->
                    calculatorViewModel.onRecallPastCalculation(record)
                    onNavigateToCalculator()
                }
            )
        }
    }
}
