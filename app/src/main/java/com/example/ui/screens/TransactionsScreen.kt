package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.financial.*
import com.example.ui.theme.*
import com.example.ui.transactions.TransactionTab
import com.example.ui.transactions.TransactionsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: TransactionsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var currencyPickerTarget by remember { mutableStateOf<String?>(null) } // "FROM" or "TO"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Confirmation Toast / Banner
        AnimatedVisibility(
            visible = uiState.savedConfirmationMessage != null,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            Surface(
                color = TealPrimary,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                        Text(
                            text = uiState.savedConfirmationMessage ?: "",
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                    IconButton(onClick = { viewModel.dismissConfirmation() }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
            }
        }

        // Horizontal Module Tabs
        ScrollableTabRow(
            selectedTabIndex = uiState.activeTab.ordinal,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth().testTag("transaction_tabs")
        ) {
            TransactionTab.values().forEach { tab ->
                Tab(
                    selected = uiState.activeTab == tab,
                    onClick = { viewModel.selectTab(tab) },
                    text = {
                        Text(
                            text = tab.title,
                            fontWeight = if (uiState.activeTab == tab) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                )
            }
        }

        // Content Area based on selected tab
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            when (uiState.activeTab) {
                TransactionTab.CURRENCY -> {
                    CurrencyModuleView(
                        state = uiState.currencyState,
                        onAmountChanged = viewModel::onCurrencyAmountChanged,
                        onSwap = viewModel::onSwapCurrencies,
                        onRefreshRates = viewModel::onRefreshExchangeRates,
                        onOpenPicker = { currencyPickerTarget = it },
                        onSave = viewModel::saveCurrencyToHistory
                    )
                }
                TransactionTab.UNITS -> {
                    UnitModuleView(
                        state = uiState.unitState,
                        onCategoryChanged = viewModel::onUnitCategoryChanged,
                        onInputChanged = viewModel::onUnitInputChanged,
                        onSelectFromUnit = viewModel::onSelectFromUnit,
                        onSelectToUnit = viewModel::onSelectToUnit,
                        onSwap = viewModel::onSwapUnits,
                        onSave = viewModel::saveUnitsToHistory
                    )
                }
                TransactionTab.TAX -> {
                    TaxModuleView(
                        state = uiState.taxState,
                        onAmountChanged = viewModel::onTaxAmountChanged,
                        onPresetSelected = viewModel::onTaxRatePresetSelected,
                        onCustomRateChanged = viewModel::onCustomTaxRateChanged,
                        onToggleInclusive = viewModel::onToggleTaxInclusive,
                        onSave = viewModel::saveTaxToHistory
                    )
                }
                TransactionTab.LOAN -> {
                    LoanModuleView(
                        state = uiState.loanState,
                        onPrincipalChanged = viewModel::onLoanPrincipalChanged,
                        onRateChanged = viewModel::onLoanInterestRateChanged,
                        onTenureChanged = viewModel::onLoanTenureChanged,
                        onToggleTenureUnit = viewModel::onToggleLoanTenureUnit,
                        onSave = viewModel::saveLoanToHistory
                    )
                }
                TransactionTab.DISCOUNT -> {
                    DiscountModuleView(
                        state = uiState.discountState,
                        onPriceChanged = viewModel::onDiscountPriceChanged,
                        onPrimaryDiscountChanged = viewModel::onPrimaryDiscountChanged,
                        onCouponDiscountChanged = viewModel::onCouponDiscountChanged,
                        onSave = viewModel::saveDiscountToHistory
                    )
                }
                TransactionTab.TIP -> {
                    TipModuleView(
                        state = uiState.tipState,
                        onBillAmountChanged = viewModel::onTipBillAmountChanged,
                        onPresetSelected = viewModel::onTipPercentSelected,
                        onCustomTipChanged = viewModel::onCustomTipPercentChanged,
                        onPersonCountChanged = viewModel::onPersonCountChanged,
                        onSave = viewModel::saveTipToHistory
                    )
                }
                TransactionTab.MARGIN -> {
                    MarginModuleView(
                        state = uiState.marginState,
                        onCostChanged = viewModel::onCostPriceChanged,
                        onSellingChanged = viewModel::onSellingPriceChanged,
                        onSave = viewModel::saveMarginToHistory
                    )
                }
            }
        }
    }

    // Currency Picker Dialog / BottomSheet
    if (currencyPickerTarget != null) {
        CurrencyPickerDialog(
            currencies = uiState.currencyState.availableCurrencies,
            onDismiss = { currencyPickerTarget = null },
            onSelect = { curr ->
                if (currencyPickerTarget == "FROM") {
                    viewModel.onSelectFromCurrency(curr)
                } else {
                    viewModel.onSelectToCurrency(curr)
                }
                currencyPickerTarget = null
            }
        )
    }
}

// ---------------------------------------------------------------------------
// 1. CURRENCY CONVERSION MODULE
// ---------------------------------------------------------------------------
@Composable
fun CurrencyModuleView(
    state: com.example.ui.transactions.CurrencyTabState,
    onAmountChanged: (String) -> Unit,
    onSwap: () -> Unit,
    onRefreshRates: () -> Unit,
    onOpenPicker: (String) -> Unit,
    onSave: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Indicative Offline Rates Banner (Prominent Disclaimer required by spec)
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().testTag("currency_disclaimer_card")
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Notice",
                    tint = AmberAccent,
                    modifier = Modifier.size(24.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Offline Indicative Rates",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Rates are reference placeholders and not live market rates. Ideal for estimates.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onRefreshRates,
                    enabled = !state.isUpdatingRates,
                    modifier = Modifier.testTag("btn_refresh_rates")
                ) {
                    if (state.isUpdatingRates) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = "Simulate Rate Update", tint = TealAccent)
                    }
                }
            }
        }

        // Amount Input Field
        OutlinedTextField(
            value = state.amountInput,
            onValueChange = onAmountChanged,
            label = { Text("Amount to Convert") },
            leadingIcon = {
                Text(
                    text = state.fromCurrency.symbol,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 12.dp)
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            ),
            modifier = Modifier.fillMaxWidth().testTag("input_currency_amount")
        )

        // Currency Selector Cards with Swap Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // FROM Currency Card
            Card(
                onClick = { onOpenPicker("FROM") },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.weight(1f).testTag("card_select_from_currency")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("From", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(state.fromCurrency.flag, fontSize = 22.sp)
                        Text(state.fromCurrency.code, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Text(
                        state.fromCurrency.name,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            // Swap Button
            IconButton(
                onClick = onSwap,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .testTag("btn_swap_currencies")
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = "Swap Currencies",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // TO Currency Card
            Card(
                onClick = { onOpenPicker("TO") },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.weight(1f).testTag("card_select_to_currency")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("To", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(state.toCurrency.flag, fontSize = 22.sp)
                        Text(state.toCurrency.code, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Text(
                        state.toCurrency.name,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }

        // Converted Result Display Card
        state.result?.let { res ->
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                ),
                modifier = Modifier.fillMaxWidth().testTag("currency_result_card")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Converted Result",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${res.to.symbol} ${FinancialMath.moneyFormat.format(res.convertedAmount)} ${res.to.code}",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "1 ${res.from.code} = ${FinancialMath.rateFormat.format(res.directRate)} ${res.to.code}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "1 ${res.to.code} = ${FinancialMath.rateFormat.format(res.reciprocalRate)} ${res.from.code}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "Updated: ${res.lastUpdatedText}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Save to History Button
        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth().height(52.dp).testTag("btn_save_currency_history"),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Conversion to History", fontWeight = FontWeight.SemiBold)
        }
    }
}

// ---------------------------------------------------------------------------
// 2. TAX / VAT MODULE
// ---------------------------------------------------------------------------
@Composable
fun TaxModuleView(
    state: com.example.ui.transactions.TaxTabState,
    onAmountChanged: (String) -> Unit,
    onPresetSelected: (Double) -> Unit,
    onCustomRateChanged: (String) -> Unit,
    onToggleInclusive: (Boolean) -> Unit,
    onSave: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Mode Selector: Tax Exclusive vs Tax Inclusive
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = !state.isTaxInclusive,
                onClick = { onToggleInclusive(false) },
                label = { Text("Tax Exclusive (Add)") },
                modifier = Modifier.weight(1f).testTag("chip_tax_exclusive")
            )
            FilterChip(
                selected = state.isTaxInclusive,
                onClick = { onToggleInclusive(true) },
                label = { Text("Tax Inclusive (Extract)") },
                modifier = Modifier.weight(1f).testTag("chip_tax_inclusive")
            )
        }

        // Amount Input
        OutlinedTextField(
            value = state.amountInput,
            onValueChange = onAmountChanged,
            label = { Text(if (state.isTaxInclusive) "Gross Amount (Tax Included)" else "Base Amount (Before Tax)") },
            prefix = { Text("$", fontWeight = FontWeight.Bold) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier.fillMaxWidth().testTag("input_tax_amount")
        )

        // Tax Rate Presets
        Text("Tax / VAT Rate", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(5.0, 10.0, 15.0, 20.0).forEach { preset ->
                FilterChip(
                    selected = state.selectedRatePercent == preset,
                    onClick = { onPresetSelected(preset) },
                    label = { Text("${preset.toInt()}%") },
                    modifier = Modifier.weight(1f).testTag("chip_tax_${preset.toInt()}")
                )
            }
        }

        // Custom Tax Rate Field
        OutlinedTextField(
            value = state.customRateInput,
            onValueChange = onCustomRateChanged,
            label = { Text("Custom Tax Rate %") },
            suffix = { Text("%") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_custom_tax_rate")
        )

        // Tax Result Breakdown Card
        state.result?.let { res ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth().testTag("tax_result_card")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Calculation Breakdown", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Net / Base Amount:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$${FinancialMath.moneyFormat.format(res.netAmount)}", fontWeight = FontWeight.SemiBold)
                    }

                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Tax (${res.ratePercent}%):", color = AmberAccent)
                        Text("$${FinancialMath.moneyFormat.format(res.taxAmount)}", fontWeight = FontWeight.Bold, color = AmberAccent)
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Total Payable:", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "$${FinancialMath.moneyFormat.format(res.totalAmount)}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TealAccent
                        )
                    }
                }
            }
        }

        // Save Button
        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth().height(52.dp).testTag("btn_save_tax_history"),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Tax Calculation to History", fontWeight = FontWeight.SemiBold)
        }
    }
}

// ---------------------------------------------------------------------------
// 3. LOAN & EMI MODULE
// ---------------------------------------------------------------------------
@Composable
fun LoanModuleView(
    state: com.example.ui.transactions.LoanTabState,
    onPrincipalChanged: (String) -> Unit,
    onRateChanged: (String) -> Unit,
    onTenureChanged: (String) -> Unit,
    onToggleTenureUnit: (Boolean) -> Unit,
    onSave: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Principal Input
        OutlinedTextField(
            value = state.principalInput,
            onValueChange = onPrincipalChanged,
            label = { Text("Loan Principal Amount") },
            prefix = { Text("$", fontWeight = FontWeight.Bold) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_loan_principal")
        )

        // Interest Rate Input
        OutlinedTextField(
            value = state.interestRateInput,
            onValueChange = onRateChanged,
            label = { Text("Annual Interest Rate %") },
            suffix = { Text("% per annum") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_loan_interest_rate")
        )

        // Tenure Row (Input + Years/Months toggle)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = state.tenureInput,
                onValueChange = onTenureChanged,
                label = { Text("Tenure") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f).testTag("input_loan_tenure")
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                FilterChip(
                    selected = state.isTenureInYears,
                    onClick = { onToggleTenureUnit(true) },
                    label = { Text("Years") },
                    modifier = Modifier.testTag("chip_tenure_years")
                )
                FilterChip(
                    selected = !state.isTenureInYears,
                    onClick = { onToggleTenureUnit(false) },
                    label = { Text("Months") },
                    modifier = Modifier.testTag("chip_tenure_months")
                )
            }
        }

        // Monthly EMI Result Card
        state.result?.let { res ->
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth().testTag("loan_result_card")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Monthly EMI Payment", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                    Text(
                        text = "$${FinancialMath.moneyFormat.format(res.monthlyEMI)}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Total Interest:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$${FinancialMath.moneyFormat.format(res.totalInterest)}", fontWeight = FontWeight.Bold, color = AmberAccent)
                    }
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Total Repayment:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$${FinancialMath.moneyFormat.format(res.totalPayment)}", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Amortization Preview Accordion
            Text("Amortization Schedule Preview (First 12 Months)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Mo", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(28.dp))
                        Text("Principal", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text("Interest", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text("Balance", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    res.sampleSchedule.forEach { item ->
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("${item.month}", fontSize = 11.sp, modifier = Modifier.width(28.dp))
                            Text("$${FinancialMath.moneyFormat.format(item.principalPayment)}", fontSize = 11.sp)
                            Text("$${FinancialMath.moneyFormat.format(item.interestPayment)}", fontSize = 11.sp, color = AmberAccent)
                            Text("$${FinancialMath.moneyFormat.format(item.remainingBalance)}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        // Save Button
        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth().height(52.dp).testTag("btn_save_loan_history"),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Loan Calculation to History", fontWeight = FontWeight.SemiBold)
        }
    }
}

// ---------------------------------------------------------------------------
// 4. DISCOUNT & PROMOTIONS MODULE
// ---------------------------------------------------------------------------
@Composable
fun DiscountModuleView(
    state: com.example.ui.transactions.DiscountTabState,
    onPriceChanged: (String) -> Unit,
    onPrimaryDiscountChanged: (String) -> Unit,
    onCouponDiscountChanged: (String) -> Unit,
    onSave: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        OutlinedTextField(
            value = state.originalPriceInput,
            onValueChange = onPriceChanged,
            label = { Text("Original List Price") },
            prefix = { Text("$", fontWeight = FontWeight.Bold) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_discount_original_price")
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = state.primaryDiscountPercentInput,
                onValueChange = onPrimaryDiscountChanged,
                label = { Text("Primary Discount %") },
                suffix = { Text("%") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f).testTag("input_primary_discount")
            )
            OutlinedTextField(
                value = state.couponDiscountPercentInput,
                onValueChange = onCouponDiscountChanged,
                label = { Text("Extra Coupon %") },
                suffix = { Text("%") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f).testTag("input_coupon_discount")
            )
        }

        state.result?.let { res ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth().testTag("discount_result_card")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Savings Summary", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Original Price:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$${FinancialMath.moneyFormat.format(res.originalPrice)}")
                    }

                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Total Discount Saved:", color = TealAccent, fontWeight = FontWeight.SemiBold)
                        Text("-$${FinancialMath.moneyFormat.format(res.savedAmount)}", fontWeight = FontWeight.Bold, color = TealAccent)
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Final Price to Pay:", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "$${FinancialMath.moneyFormat.format(res.finalPrice)}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary
                        )
                    }
                }
            }
        }

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth().height(52.dp).testTag("btn_save_discount_history"),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Discount to History", fontWeight = FontWeight.SemiBold)
        }
    }
}

// ---------------------------------------------------------------------------
// 5. TIP & BILL SPLITTER MODULE
// ---------------------------------------------------------------------------
@Composable
fun TipModuleView(
    state: com.example.ui.transactions.TipTabState,
    onBillAmountChanged: (String) -> Unit,
    onPresetSelected: (Double) -> Unit,
    onCustomTipChanged: (String) -> Unit,
    onPersonCountChanged: (Int) -> Unit,
    onSave: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        OutlinedTextField(
            value = state.billAmountInput,
            onValueChange = onBillAmountChanged,
            label = { Text("Total Bill Amount") },
            prefix = { Text("$", fontWeight = FontWeight.Bold) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_tip_bill_amount")
        )

        Text("Tip Percentage", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(10.0, 15.0, 18.0, 20.0).forEach { preset ->
                FilterChip(
                    selected = state.tipPercent == preset,
                    onClick = { onPresetSelected(preset) },
                    label = { Text("${preset.toInt()}%") },
                    modifier = Modifier.weight(1f).testTag("chip_tip_${preset.toInt()}")
                )
            }
        }

        // Stepper for number of people
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Split Between", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("${state.personCount} person(s)", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { onPersonCountChanged(state.personCount - 1) },
                    enabled = state.personCount > 1,
                    modifier = Modifier.testTag("btn_tip_decrease_people")
                ) {
                    Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Decrease Diners")
                }
                Text("${state.personCount}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                IconButton(
                    onClick = { onPersonCountChanged(state.personCount + 1) },
                    modifier = Modifier.testTag("btn_tip_increase_people")
                ) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = "Increase Diners")
                }
            }
        }

        state.result?.let { res ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth().testTag("tip_result_card")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Per Person Total", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                    Text(
                        text = "$${FinancialMath.moneyFormat.format(res.perPersonTotal)}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Per Person Tip:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$${FinancialMath.moneyFormat.format(res.perPersonTip)}", fontWeight = FontWeight.SemiBold, color = AmberAccent)
                    }
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Total Tip Amount:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$${FinancialMath.moneyFormat.format(res.tipAmount)}", fontWeight = FontWeight.SemiBold)
                    }
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Grand Total Bill:", fontWeight = FontWeight.Bold)
                        Text("$${FinancialMath.moneyFormat.format(res.totalBill)}", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth().height(52.dp).testTag("btn_save_tip_history"),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Tip Calculation to History", fontWeight = FontWeight.SemiBold)
        }
    }
}

// ---------------------------------------------------------------------------
// 6. PROFIT MARGIN & MARKUP MODULE
// ---------------------------------------------------------------------------
@Composable
fun MarginModuleView(
    state: com.example.ui.transactions.MarginTabState,
    onCostChanged: (String) -> Unit,
    onSellingChanged: (String) -> Unit,
    onSave: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        OutlinedTextField(
            value = state.costPriceInput,
            onValueChange = onCostChanged,
            label = { Text("Cost Price (COGS)") },
            prefix = { Text("$", fontWeight = FontWeight.Bold) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_margin_cost")
        )

        OutlinedTextField(
            value = state.sellingPriceInput,
            onValueChange = onSellingChanged,
            label = { Text("Selling Price (Revenue)") },
            prefix = { Text("$", fontWeight = FontWeight.Bold) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_margin_selling")
        )

        state.result?.let { res ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth().testTag("margin_result_card")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Profitability Metrics", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Gross Profit:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "$${FinancialMath.moneyFormat.format(res.grossProfit)}",
                            fontWeight = FontWeight.Bold,
                            color = if (res.grossProfit >= 0) TealAccent else CoralRed
                        )
                    }

                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Gross Profit Margin:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${FinancialMath.percentFormat.format(res.marginPercent)}%", fontWeight = FontWeight.Bold, color = TealAccent)
                    }

                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Markup Percentage:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${FinancialMath.percentFormat.format(res.markupPercent)}%", fontWeight = FontWeight.Bold, color = AmberAccent)
                    }
                }
            }
        }

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth().height(52.dp).testTag("btn_save_margin_history"),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Margin to History", fontWeight = FontWeight.SemiBold)
        }
    }
}

// ---------------------------------------------------------------------------
// CURRENCY PICKER MODAL
// ---------------------------------------------------------------------------
@Composable
fun CurrencyPickerDialog(
    currencies: List<CurrencyInfo>,
    onDismiss: () -> Unit,
    onSelect: (CurrencyInfo) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(searchQuery, currencies) {
        if (searchQuery.isBlank()) currencies
        else currencies.filter {
            it.code.contains(searchQuery, ignoreCase = true) ||
            it.name.contains(searchQuery, ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Select Currency", fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search code or country...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_search_currency")
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filtered) { curr ->
                    Surface(
                        onClick = { onSelect(curr) },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth().testTag("currency_option_${curr.code}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(curr.flag, fontSize = 24.sp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${curr.code} - ${curr.name}",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Symbol: ${curr.symbol}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// ---------------------------------------------------------------------------
// UNIT CONVERTER MODULE (Distance, Weight, Temperature)
// ---------------------------------------------------------------------------
@Composable
fun UnitModuleView(
    state: com.example.ui.transactions.UnitTabState,
    onCategoryChanged: (UnitCategory) -> Unit,
    onInputChanged: (String) -> Unit,
    onSelectFromUnit: (ConversionUnit) -> Unit,
    onSelectToUnit: (ConversionUnit) -> Unit,
    onSwap: () -> Unit,
    onSave: () -> Unit
) {
    val scrollState = rememberScrollState()
    var unitPickerTarget by remember { mutableStateOf<String?>(null) } // "FROM" or "TO"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Category Selector Chips: Distance, Weight, Temperature
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            UnitCategory.values().forEach { category ->
                FilterChip(
                    selected = state.selectedCategory == category,
                    onClick = { onCategoryChanged(category) },
                    label = {
                        Text(
                            text = "${category.iconName} ${category.displayName}",
                            fontSize = 12.sp,
                            fontWeight = if (state.selectedCategory == category) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.weight(1f).testTag("chip_unit_category_${category.name.lowercase()}")
                )
            }
        }

        // Amount Input Field
        OutlinedTextField(
            value = state.inputValue,
            onValueChange = onInputChanged,
            label = { Text("Value in ${state.fromUnit.name} (${state.fromUnit.symbol})") },
            suffix = {
                Text(
                    text = state.fromUnit.symbol,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            ),
            modifier = Modifier.fillMaxWidth().testTag("input_unit_value")
        )

        // From Unit & To Unit Selector Cards with Swap Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // FROM Unit Card
            Card(
                onClick = { unitPickerTarget = "FROM" },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.weight(1f).testTag("card_select_from_unit")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("From Unit", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = state.fromUnit.symbol,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = state.fromUnit.name,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            // Swap Button
            IconButton(
                onClick = onSwap,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .testTag("btn_swap_units")
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = "Swap Units",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // TO Unit Card
            Card(
                onClick = { unitPickerTarget = "TO" },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.weight(1f).testTag("card_select_to_unit")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("To Unit", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = state.toUnit.symbol,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = state.toUnit.name,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }

        // Converted Result Display Card
        state.result?.let { res ->
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                ),
                modifier = Modifier.fillMaxWidth().testTag("unit_result_card")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Converted Result",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${UnitConverter.formatUnitValue(res.outputValue)} ${res.toUnit.symbol}",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${res.toUnit.name} (${res.category.displayName})",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (res.formulaExplanation.isNotBlank()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Text(
                            text = "Conversion Formula: ${res.formulaExplanation}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TealAccent
                        )
                    }
                }
            }

            // Quick Equivalents Across Other Units in Category
            if (res.quickEquivalents.isNotEmpty()) {
                Text(
                    text = "Equivalent Values in ${res.category.displayName}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        res.quickEquivalents.forEach { (unit, eqVal) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = unit.name,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${UnitConverter.formatUnitValue(eqVal)} ${unit.symbol}",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Save Conversion to History Button
        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth().height(52.dp).testTag("btn_save_unit_history"),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Unit Conversion to History", fontWeight = FontWeight.SemiBold)
        }
    }

    // Unit Picker Dialog
    if (unitPickerTarget != null) {
        val units = remember(state.selectedCategory) {
            UnitConverter.getUnitsForCategory(state.selectedCategory)
        }
        UnitPickerDialog(
            title = if (unitPickerTarget == "FROM") "Select Source Unit" else "Select Target Unit",
            units = units,
            onDismiss = { unitPickerTarget = null },
            onSelect = { unit ->
                if (unitPickerTarget == "FROM") {
                    onSelectFromUnit(unit)
                } else {
                    onSelectToUnit(unit)
                }
                unitPickerTarget = null
            }
        )
    }
}

@Composable
fun UnitPickerDialog(
    title: String,
    units: List<ConversionUnit>,
    onDismiss: () -> Unit,
    onSelect: (ConversionUnit) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(title, fontWeight = FontWeight.Bold)
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 340.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(units) { unit ->
                    Surface(
                        onClick = { onSelect(unit) },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth().testTag("unit_option_${unit.code}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = unit.name,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = unit.symbol,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

