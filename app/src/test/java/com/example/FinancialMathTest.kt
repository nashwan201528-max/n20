package com.example

import com.example.domain.financial.CurrencyInfo
import com.example.domain.financial.FinancialMath
import org.junit.Assert.*
import org.junit.Test

class FinancialMathTest {

    @Test
    fun testCurrencyConversionDirectAndReciprocal() {
        val usd = CurrencyInfo("USD", "$", "US Dollar", "🇺🇸", 1.0)
        val eur = CurrencyInfo("EUR", "€", "Euro", "🇪🇺", 0.92)

        val result = FinancialMath.convertCurrency(100.0, usd, eur)
        assertEquals(92.0, result.convertedAmount, 0.001)
        assertEquals(0.92, result.directRate, 0.001)
        assertEquals(1.0 / 0.92, result.reciprocalRate, 0.001)
        assertTrue(result.isIndicativeOffline)
    }

    @Test
    fun testTaxExclusiveCalculation() {
        // $1000 base with 15% tax added -> Tax = $150, Total = $1150
        val result = FinancialMath.calculateTax(amount = 1000.0, ratePercent = 15.0, isTaxInclusive = false)
        assertEquals(1000.0, result.baseAmount, 0.001)
        assertEquals(150.0, result.taxAmount, 0.001)
        assertEquals(1150.0, result.totalAmount, 0.001)
    }

    @Test
    fun testTaxInclusiveCalculation() {
        // $1150 gross with 15% inclusive tax -> Net = $1000, Tax = $150
        val result = FinancialMath.calculateTax(amount = 1150.0, ratePercent = 15.0, isTaxInclusive = true)
        assertEquals(1000.0, result.netAmount, 0.01)
        assertEquals(150.0, result.taxAmount, 0.01)
        assertEquals(1150.0, result.totalAmount, 0.01)
    }

    @Test
    fun testLoanEMICalculation() {
        // Principal: $10,000, Annual Rate: 12%, Tenure: 12 months
        // Monthly rate r = 0.01
        // EMI = 10000 * 0.01 * (1.01)^12 / ((1.01)^12 - 1) = 888.48788
        val result = FinancialMath.calculateLoan(principal = 10000.0, annualRatePercent = 12.0, tenureMonths = 12)
        assertEquals(888.49, result.monthlyEMI, 0.02)
        assertEquals(10661.85, result.totalPayment, 0.5)
        assertEquals(661.85, result.totalInterest, 0.5)
        assertEquals(12, result.sampleSchedule.size)
    }

    @Test
    fun testStackedDiscountCalculation() {
        // $100 item with 20% primary discount -> $80, plus extra 10% coupon -> $72
        // Total saved = $28
        val result = FinancialMath.calculateDiscount(originalPrice = 100.0, primaryDiscountPercent = 20.0, couponPercent = 10.0)
        assertEquals(72.0, result.finalPrice, 0.001)
        assertEquals(28.0, result.savedAmount, 0.001)
    }

    @Test
    fun testTipAndBillSplit() {
        // $100 bill with 20% tip split among 4 people
        // Total bill: $120 ($20 tip). Per person total: $30 ($5 tip)
        val result = FinancialMath.calculateTipAndSplit(billAmount = 100.0, tipPercent = 20.0, personCount = 4)
        assertEquals(20.0, result.tipAmount, 0.001)
        assertEquals(120.0, result.totalBill, 0.001)
        assertEquals(30.0, result.perPersonTotal, 0.001)
        assertEquals(5.0, result.perPersonTip, 0.001)
    }

    @Test
    fun testMarginAndMarkupCalculation() {
        // Cost: $50, Selling: $100 -> Profit = $50
        // Margin: 50/100 = 50%
        // Markup: 50/50 = 100%
        val result = FinancialMath.calculateMarginAndMarkup(costPrice = 50.0, sellingPrice = 100.0)
        assertEquals(50.0, result.grossProfit, 0.001)
        assertEquals(50.0, result.marginPercent, 0.001)
        assertEquals(100.0, result.markupPercent, 0.001)
    }
}
