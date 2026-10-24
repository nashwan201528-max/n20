package com.example.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.CalculationRecord
import com.example.data.HistoryRepository
import com.example.domain.financial.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class TransactionTab(val title: String) {
    CURRENCY("Currency"),
    UNITS("Unit Converter"),
    TAX("Tax / VAT"),
    LOAN("Loan / EMI"),
    DISCOUNT("Discount"),
    TIP("Tip & Split"),
    MARGIN("Margin")
}

data class CurrencyTabState(
    val availableCurrencies: List<CurrencyInfo> = FinancialMath.supportedCurrencies,
    val fromCurrency: CurrencyInfo = FinancialMath.supportedCurrencies.first { it.code == "USD" },
    val toCurrency: CurrencyInfo = FinancialMath.supportedCurrencies.first { it.code == "EUR" },
    val amountInput: String = "100",
    val result: CurrencyConversionResult? = null,
    val isUpdatingRates: Boolean = false,
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)

data class UnitTabState(
    val selectedCategory: UnitCategory = UnitCategory.DISTANCE,
    val fromUnit: ConversionUnit = UnitConverter.distanceUnits.first { it.code == "km" },
    val toUnit: ConversionUnit = UnitConverter.distanceUnits.first { it.code == "mi" },
    val inputValue: String = "10",
    val result: UnitConversionResult? = null
)

data class TaxTabState(
    val amountInput: String = "1000",
    val selectedRatePercent: Double = 15.0,
    val customRateInput: String = "15",
    val isTaxInclusive: Boolean = false,
    val result: TaxCalculationResult? = null
)

data class LoanTabState(
    val principalInput: String = "25000",
    val interestRateInput: String = "6.5",
    val tenureInput: String = "3",
    val isTenureInYears: Boolean = true,
    val result: LoanCalculationResult? = null
)

data class DiscountTabState(
    val originalPriceInput: String = "120",
    val primaryDiscountPercentInput: String = "20",
    val couponDiscountPercentInput: String = "10",
    val result: DiscountCalculationResult? = null
)

data class TipTabState(
    val billAmountInput: String = "85",
    val tipPercent: Double = 18.0,
    val customTipInput: String = "18",
    val personCount: Int = 2,
    val result: TipCalculationResult? = null
)

data class MarginTabState(
    val costPriceInput: String = "60",
    val sellingPriceInput: String = "100",
    val result: MarginCalculationResult? = null
)

data class TransactionsUiState(
    val activeTab: TransactionTab = TransactionTab.CURRENCY,
    val currencyState: CurrencyTabState = CurrencyTabState(),
    val unitState: UnitTabState = UnitTabState(),
    val taxState: TaxTabState = TaxTabState(),
    val loanState: LoanTabState = LoanTabState(),
    val discountState: DiscountTabState = DiscountTabState(),
    val tipState: TipTabState = TipTabState(),
    val marginState: MarginTabState = MarginTabState(),
    val savedConfirmationMessage: String? = null
)

class TransactionsViewModel(
    private val historyRepository: HistoryRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionsUiState())
    val uiState: StateFlow<TransactionsUiState> = _uiState.asStateFlow()

    init {
        recalculateCurrency()
        recalculateUnits()
        recalculateTax()
        recalculateLoan()
        recalculateDiscount()
        recalculateTip()
        recalculateMargin()
    }

    fun selectTab(tab: TransactionTab) {
        _uiState.update { it.copy(activeTab = tab, savedConfirmationMessage = null) }
    }

    // --- Currency Actions ---
    fun onCurrencyAmountChanged(input: String) {
        val sanitized = input.filter { it.isDigit() || it == '.' }
        _uiState.update { it.copy(currencyState = it.currencyState.copy(amountInput = sanitized)) }
        recalculateCurrency()
    }

    fun onSelectFromCurrency(currency: CurrencyInfo) {
        _uiState.update { it.copy(currencyState = it.currencyState.copy(fromCurrency = currency)) }
        recalculateCurrency()
    }

    fun onSelectToCurrency(currency: CurrencyInfo) {
        _uiState.update { it.copy(currencyState = it.currencyState.copy(toCurrency = currency)) }
        recalculateCurrency()
    }

    fun onSwapCurrencies() {
        _uiState.update {
            val curr = it.currencyState
            it.copy(
                currencyState = curr.copy(
                    fromCurrency = curr.toCurrency,
                    toCurrency = curr.fromCurrency
                )
            )
        }
        recalculateCurrency()
    }

    fun onRefreshExchangeRates() {
        viewModelScope.launch {
            _uiState.update { it.copy(currencyState = it.currencyState.copy(isUpdatingRates = true)) }
            delay(500) // Simulated network sync latency
            val now = System.currentTimeMillis()
            // Random tiny variation of 0.05% to simulate market jitter
            val updatedList = _uiState.value.currencyState.availableCurrencies.map { curr ->
                if (curr.code == "USD") curr
                else {
                    val variation = 1.0 + (Random.nextDouble(-0.005, 0.005))
                    curr.copy(rateToUSD = curr.rateToUSD * variation)
                }
            }
            val fromCode = _uiState.value.currencyState.fromCurrency.code
            val toCode = _uiState.value.currencyState.toCurrency.code
            val newFrom = updatedList.firstOrNull { it.code == fromCode } ?: _uiState.value.currencyState.fromCurrency
            val newTo = updatedList.firstOrNull { it.code == toCode } ?: _uiState.value.currencyState.toCurrency

            _uiState.update {
                it.copy(
                    currencyState = it.currencyState.copy(
                        availableCurrencies = updatedList,
                        fromCurrency = newFrom,
                        toCurrency = newTo,
                        isUpdatingRates = false,
                        lastUpdatedTimestamp = now
                    )
                )
            }
            recalculateCurrency()
        }
    }

    private fun recalculateCurrency() {
        val curr = _uiState.value.currencyState
        val amount = curr.amountInput.toDoubleOrNull() ?: 0.0
        val res = FinancialMath.convertCurrency(
            amount = amount,
            from = curr.fromCurrency,
            to = curr.toCurrency,
            updatedTimestamp = curr.lastUpdatedTimestamp
        )
        _uiState.update { it.copy(currencyState = curr.copy(result = res)) }
    }

    fun saveCurrencyToHistory() {
        val curr = _uiState.value.currencyState
        val res = curr.result ?: return
        val amountStr = FinancialMath.moneyFormat.format(res.amount)
        val convertedStr = FinancialMath.moneyFormat.format(res.convertedAmount)
        viewModelScope.launch {
            historyRepository?.saveRecord(
                CalculationRecord(
                    type = "CURRENCY",
                    title = "Currency Exchange",
                    expression = "${res.from.code} $amountStr → ${res.to.code}",
                    result = "${res.to.code} $convertedStr",
                    secondaryDetails = "1 ${res.from.code} = ${FinancialMath.rateFormat.format(res.directRate)} ${res.to.code} (Offline Rate)"
                )
            )
            flashConfirmation("Currency conversion saved to history!")
        }
    }

    // --- Unit Converter Actions ---
    fun onUnitCategoryChanged(category: UnitCategory) {
        val units = UnitConverter.getUnitsForCategory(category)
        val defaultFrom = units.getOrNull(0) ?: units.first()
        val defaultTo = units.getOrNull(1) ?: units.first()
        _uiState.update {
            it.copy(
                unitState = it.unitState.copy(
                    selectedCategory = category,
                    fromUnit = defaultFrom,
                    toUnit = defaultTo
                )
            )
        }
        recalculateUnits()
    }

    fun onUnitInputChanged(input: String) {
        val sanitized = input.filter { it.isDigit() || it == '.' || (it == '-' && input.startsWith("-")) }
        _uiState.update { it.copy(unitState = it.unitState.copy(inputValue = sanitized)) }
        recalculateUnits()
    }

    fun onSelectFromUnit(unit: ConversionUnit) {
        _uiState.update { it.copy(unitState = it.unitState.copy(fromUnit = unit)) }
        recalculateUnits()
    }

    fun onSelectToUnit(unit: ConversionUnit) {
        _uiState.update { it.copy(unitState = it.unitState.copy(toUnit = unit)) }
        recalculateUnits()
    }

    fun onSwapUnits() {
        _uiState.update {
            val u = it.unitState
            it.copy(
                unitState = u.copy(
                    fromUnit = u.toUnit,
                    toUnit = u.fromUnit
                )
            )
        }
        recalculateUnits()
    }

    private fun recalculateUnits() {
        val u = _uiState.value.unitState
        val value = u.inputValue.toDoubleOrNull() ?: 0.0
        val res = UnitConverter.convert(value = value, from = u.fromUnit, to = u.toUnit)
        _uiState.update { it.copy(unitState = u.copy(result = res)) }
    }

    fun saveUnitsToHistory() {
        val state = _uiState.value.unitState
        val res = state.result ?: return
        val inFormatted = UnitConverter.formatUnitValue(res.inputValue)
        val outFormatted = UnitConverter.formatUnitValue(res.outputValue)
        viewModelScope.launch {
            historyRepository?.saveRecord(
                CalculationRecord(
                    type = "UNITS",
                    title = "${res.category.displayName} Conversion",
                    expression = "$inFormatted ${res.fromUnit.symbol} → ${res.toUnit.symbol}",
                    result = "$outFormatted ${res.toUnit.symbol}",
                    secondaryDetails = res.formulaExplanation
                )
            )
            flashConfirmation("Unit conversion saved to history!")
        }
    }

    // --- Tax Actions ---
    fun onTaxAmountChanged(input: String) {
        val sanitized = input.filter { it.isDigit() || it == '.' }
        _uiState.update { it.copy(taxState = it.taxState.copy(amountInput = sanitized)) }
        recalculateTax()
    }

    fun onTaxRatePresetSelected(preset: Double) {
        _uiState.update {
            it.copy(
                taxState = it.taxState.copy(
                    selectedRatePercent = preset,
                    customRateInput = preset.toString()
                )
            )
        }
        recalculateTax()
    }

    fun onCustomTaxRateChanged(input: String) {
        val sanitized = input.filter { it.isDigit() || it == '.' }
        val rate = sanitized.toDoubleOrNull() ?: 0.0
        _uiState.update {
            it.copy(
                taxState = it.taxState.copy(
                    selectedRatePercent = rate,
                    customRateInput = sanitized
                )
            )
        }
        recalculateTax()
    }

    fun onToggleTaxInclusive(inclusive: Boolean) {
        _uiState.update { it.copy(taxState = it.taxState.copy(isTaxInclusive = inclusive)) }
        recalculateTax()
    }

    private fun recalculateTax() {
        val state = _uiState.value.taxState
        val amount = state.amountInput.toDoubleOrNull() ?: 0.0
        val res = FinancialMath.calculateTax(
            amount = amount,
            ratePercent = state.selectedRatePercent,
            isTaxInclusive = state.isTaxInclusive
        )
        _uiState.update { it.copy(taxState = state.copy(result = res)) }
    }

    fun saveTaxToHistory() {
        val state = _uiState.value.taxState
        val res = state.result ?: return
        val typeLabel = if (res.isTaxInclusive) "Tax Inclusive" else "Tax Exclusive"
        viewModelScope.launch {
            historyRepository?.saveRecord(
                CalculationRecord(
                    type = "TAX",
                    title = "Tax / VAT Calculation",
                    expression = "$typeLabel on $${FinancialMath.moneyFormat.format(res.baseAmount)} @ ${res.ratePercent}%",
                    result = "Total: $${FinancialMath.moneyFormat.format(res.totalAmount)}",
                    secondaryDetails = "Tax Amount: $${FinancialMath.moneyFormat.format(res.taxAmount)} | Net: $${FinancialMath.moneyFormat.format(res.netAmount)}"
                )
            )
            flashConfirmation("Tax calculation saved to history!")
        }
    }

    // --- Loan Actions ---
    fun onLoanPrincipalChanged(input: String) {
        val sanitized = input.filter { it.isDigit() || it == '.' }
        _uiState.update { it.copy(loanState = it.loanState.copy(principalInput = sanitized)) }
        recalculateLoan()
    }

    fun onLoanInterestRateChanged(input: String) {
        val sanitized = input.filter { it.isDigit() || it == '.' }
        _uiState.update { it.copy(loanState = it.loanState.copy(interestRateInput = sanitized)) }
        recalculateLoan()
    }

    fun onLoanTenureChanged(input: String) {
        val sanitized = input.filter { it.isDigit() }
        _uiState.update { it.copy(loanState = it.loanState.copy(tenureInput = sanitized)) }
        recalculateLoan()
    }

    fun onToggleLoanTenureUnit(isYears: Boolean) {
        _uiState.update { it.copy(loanState = it.loanState.copy(isTenureInYears = isYears)) }
        recalculateLoan()
    }

    private fun recalculateLoan() {
        val state = _uiState.value.loanState
        val principal = state.principalInput.toDoubleOrNull() ?: 0.0
        val rate = state.interestRateInput.toDoubleOrNull() ?: 0.0
        val rawTenure = state.tenureInput.toIntOrNull() ?: 1
        val months = if (state.isTenureInYears) rawTenure * 12 else rawTenure

        val res = FinancialMath.calculateLoan(
            principal = principal,
            annualRatePercent = rate,
            tenureMonths = months
        )
        _uiState.update { it.copy(loanState = state.copy(result = res)) }
    }

    fun saveLoanToHistory() {
        val state = _uiState.value.loanState
        val res = state.result ?: return
        viewModelScope.launch {
            historyRepository?.saveRecord(
                CalculationRecord(
                    type = "LOAN",
                    title = "Loan / EMI Estimate",
                    expression = "$${FinancialMath.moneyFormat.format(res.principal)} @ ${res.annualRatePercent}% for ${res.tenureMonths} mos",
                    result = "EMI: $${FinancialMath.moneyFormat.format(res.monthlyEMI)}/mo",
                    secondaryDetails = "Total Payment: $${FinancialMath.moneyFormat.format(res.totalPayment)} | Interest: $${FinancialMath.moneyFormat.format(res.totalInterest)}"
                )
            )
            flashConfirmation("Loan calculation saved to history!")
        }
    }

    // --- Discount Actions ---
    fun onDiscountPriceChanged(input: String) {
        val sanitized = input.filter { it.isDigit() || it == '.' }
        _uiState.update { it.copy(discountState = it.discountState.copy(originalPriceInput = sanitized)) }
        recalculateDiscount()
    }

    fun onPrimaryDiscountChanged(input: String) {
        val sanitized = input.filter { it.isDigit() || it == '.' }
        _uiState.update { it.copy(discountState = it.discountState.copy(primaryDiscountPercentInput = sanitized)) }
        recalculateDiscount()
    }

    fun onCouponDiscountChanged(input: String) {
        val sanitized = input.filter { it.isDigit() || it == '.' }
        _uiState.update { it.copy(discountState = it.discountState.copy(couponDiscountPercentInput = sanitized)) }
        recalculateDiscount()
    }

    private fun recalculateDiscount() {
        val state = _uiState.value.discountState
        val price = state.originalPriceInput.toDoubleOrNull() ?: 0.0
        val d1 = state.primaryDiscountPercentInput.toDoubleOrNull() ?: 0.0
        val d2 = state.couponDiscountPercentInput.toDoubleOrNull() ?: 0.0

        val res = FinancialMath.calculateDiscount(
            originalPrice = price,
            primaryDiscountPercent = d1,
            couponPercent = d2
        )
        _uiState.update { it.copy(discountState = state.copy(result = res)) }
    }

    fun saveDiscountToHistory() {
        val state = _uiState.value.discountState
        val res = state.result ?: return
        viewModelScope.launch {
            historyRepository?.saveRecord(
                CalculationRecord(
                    type = "DISCOUNT",
                    title = "Discount & Savings",
                    expression = "$${FinancialMath.moneyFormat.format(res.originalPrice)} - ${res.primaryDiscountPercent}%" +
                            if (res.couponPercent > 0) " - ${res.couponPercent}%" else "",
                    result = "Final Price: $${FinancialMath.moneyFormat.format(res.finalPrice)}",
                    secondaryDetails = "You Save: $${FinancialMath.moneyFormat.format(res.savedAmount)}"
                )
            )
            flashConfirmation("Discount saved to history!")
        }
    }

    // --- Tip Actions ---
    fun onTipBillAmountChanged(input: String) {
        val sanitized = input.filter { it.isDigit() || it == '.' }
        _uiState.update { it.copy(tipState = it.tipState.copy(billAmountInput = sanitized)) }
        recalculateTip()
    }

    fun onTipPercentSelected(preset: Double) {
        _uiState.update {
            it.copy(
                tipState = it.tipState.copy(
                    tipPercent = preset,
                    customTipInput = preset.toString()
                )
            )
        }
        recalculateTip()
    }

    fun onCustomTipPercentChanged(input: String) {
        val sanitized = input.filter { it.isDigit() || it == '.' }
        val rate = sanitized.toDoubleOrNull() ?: 0.0
        _uiState.update {
            it.copy(
                tipState = it.tipState.copy(
                    tipPercent = rate,
                    customTipInput = sanitized
                )
            )
        }
        recalculateTip()
    }

    fun onPersonCountChanged(count: Int) {
        _uiState.update { it.copy(tipState = it.tipState.copy(personCount = count.coerceAtLeast(1))) }
        recalculateTip()
    }

    private fun recalculateTip() {
        val state = _uiState.value.tipState
        val bill = state.billAmountInput.toDoubleOrNull() ?: 0.0
        val res = FinancialMath.calculateTipAndSplit(
            billAmount = bill,
            tipPercent = state.tipPercent,
            personCount = state.personCount
        )
        _uiState.update { it.copy(tipState = state.copy(result = res)) }
    }

    fun saveTipToHistory() {
        val state = _uiState.value.tipState
        val res = state.result ?: return
        viewModelScope.launch {
            historyRepository?.saveRecord(
                CalculationRecord(
                    type = "TIP",
                    title = "Bill & Tip Split",
                    expression = "$${FinancialMath.moneyFormat.format(res.billAmount)} + ${res.tipPercent}% tip split by ${res.personCount}",
                    result = "$${FinancialMath.moneyFormat.format(res.perPersonTotal)} / person",
                    secondaryDetails = "Total: $${FinancialMath.moneyFormat.format(res.totalBill)} (Tip: $${FinancialMath.moneyFormat.format(res.tipAmount)})"
                )
            )
            flashConfirmation("Tip calculation saved to history!")
        }
    }

    // --- Margin Actions ---
    fun onCostPriceChanged(input: String) {
        val sanitized = input.filter { it.isDigit() || it == '.' }
        _uiState.update { it.copy(marginState = it.marginState.copy(costPriceInput = sanitized)) }
        recalculateMargin()
    }

    fun onSellingPriceChanged(input: String) {
        val sanitized = input.filter { it.isDigit() || it == '.' }
        _uiState.update { it.copy(marginState = it.marginState.copy(sellingPriceInput = sanitized)) }
        recalculateMargin()
    }

    private fun recalculateMargin() {
        val state = _uiState.value.marginState
        val cost = state.costPriceInput.toDoubleOrNull() ?: 0.0
        val sell = state.sellingPriceInput.toDoubleOrNull() ?: 0.0
        val res = FinancialMath.calculateMarginAndMarkup(costPrice = cost, sellingPrice = sell)
        _uiState.update { it.copy(marginState = state.copy(result = res)) }
    }

    fun saveMarginToHistory() {
        val state = _uiState.value.marginState
        val res = state.result ?: return
        viewModelScope.launch {
            historyRepository?.saveRecord(
                CalculationRecord(
                    type = "MARGIN",
                    title = "Margin & Markup",
                    expression = "Cost $${FinancialMath.moneyFormat.format(res.costPrice)} vs Sell $${FinancialMath.moneyFormat.format(res.sellingPrice)}",
                    result = "Margin: ${FinancialMath.percentFormat.format(res.marginPercent)}%",
                    secondaryDetails = "Profit: $${FinancialMath.moneyFormat.format(res.grossProfit)} | Markup: ${FinancialMath.percentFormat.format(res.markupPercent)}%"
                )
            )
            flashConfirmation("Margin calculation saved to history!")
        }
    }

    private fun flashConfirmation(msg: String) {
        _uiState.update { it.copy(savedConfirmationMessage = msg) }
        viewModelScope.launch {
            delay(3000)
            _uiState.update { if (it.savedConfirmationMessage == msg) it.copy(savedConfirmationMessage = null) else it }
        }
    }

    fun dismissConfirmation() {
        _uiState.update { it.copy(savedConfirmationMessage = null) }
    }
}

class TransactionsViewModelFactory(
    private val historyRepository: HistoryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransactionsViewModel(historyRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
