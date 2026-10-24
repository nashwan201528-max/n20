package com.example.domain.financial

data class CurrencyInfo(
    val code: String,
    val symbol: String,
    val name: String,
    val flag: String,
    val rateToUSD: Double // 1 USD = rateToUSD in this currency
)

data class CurrencyConversionResult(
    val from: CurrencyInfo,
    val to: CurrencyInfo,
    val amount: Double,
    val convertedAmount: Double,
    val directRate: Double,
    val reciprocalRate: Double,
    val lastUpdatedText: String,
    val isIndicativeOffline: Boolean = true
)

data class TaxCalculationResult(
    val baseAmount: Double,
    val ratePercent: Double,
    val isTaxInclusive: Boolean,
    val netAmount: Double,
    val taxAmount: Double,
    val totalAmount: Double
)

data class AmortizationMonth(
    val month: Int,
    val payment: Double,
    val principalPayment: Double,
    val interestPayment: Double,
    val remainingBalance: Double
)

data class LoanCalculationResult(
    val principal: Double,
    val annualRatePercent: Double,
    val tenureMonths: Int,
    val monthlyEMI: Double,
    val totalPayment: Double,
    val totalInterest: Double,
    val sampleSchedule: List<AmortizationMonth>
)

data class DiscountCalculationResult(
    val originalPrice: Double,
    val primaryDiscountPercent: Double,
    val couponPercent: Double,
    val savedAmount: Double,
    val finalPrice: Double
)

data class TipCalculationResult(
    val billAmount: Double,
    val tipPercent: Double,
    val personCount: Int,
    val tipAmount: Double,
    val totalBill: Double,
    val perPersonBill: Double,
    val perPersonTip: Double,
    val perPersonTotal: Double
)

data class MarginCalculationResult(
    val costPrice: Double,
    val sellingPrice: Double,
    val grossProfit: Double,
    val marginPercent: Double,
    val markupPercent: Double
)
