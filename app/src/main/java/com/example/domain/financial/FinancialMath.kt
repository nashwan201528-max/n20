package com.example.domain.financial

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.pow
import kotlin.math.roundToLong

object FinancialMath {

    private val symbols = DecimalFormatSymbols(Locale.US)
    val moneyFormat = DecimalFormat("#,##0.00", symbols)
    val rateFormat = DecimalFormat("0.0000", symbols)
    val percentFormat = DecimalFormat("0.0#", symbols)

    val supportedCurrencies: List<CurrencyInfo> = listOf(
        CurrencyInfo("USD", "$", "US Dollar", "🇺🇸", 1.0),
        CurrencyInfo("EUR", "€", "Euro", "🇪🇺", 0.922),
        CurrencyInfo("GBP", "£", "British Pound", "🇬🇧", 0.785),
        CurrencyInfo("JPY", "¥", "Japanese Yen", "🇯🇵", 154.20),
        CurrencyInfo("CAD", "C$", "Canadian Dollar", "🇨🇦", 1.378),
        CurrencyInfo("AUD", "A$", "Australian Dollar", "🇦🇺", 1.524),
        CurrencyInfo("CHF", "Fr", "Swiss Franc", "🇨🇭", 0.908),
        CurrencyInfo("CNY", "¥", "Chinese Yuan", "🇨🇳", 7.235),
        CurrencyInfo("INR", "₹", "Indian Rupee", "🇮🇳", 83.52),
        CurrencyInfo("AED", "د.إ", "UAE Dirham", "🇦🇪", 3.673),
        CurrencyInfo("SGD", "S$", "Singapore Dollar", "🇸🇬", 1.354),
        CurrencyInfo("NZD", "NZ$", "New Zealand Dollar", "🇳🇿", 1.662),
        CurrencyInfo("BRL", "R$", "Brazilian Real", "🇧🇷", 5.210),
        CurrencyInfo("MXN", "Mex$", "Mexican Peso", "🇲🇽", 16.95),
        CurrencyInfo("SAR", "﷼", "Saudi Riyal", "🇸🇦", 3.750),
        CurrencyInfo("KRW", "₩", "South Korean Won", "🇰🇷", 1378.0),
        CurrencyInfo("TRY", "₺", "Turkish Lira", "🇹🇷", 32.40),
        CurrencyInfo("ZAR", "R", "South African Rand", "🇿🇦", 18.65),
        CurrencyInfo("SEK", "kr", "Swedish Krona", "🇸🇪", 10.85),
        CurrencyInfo("NOK", "kr", "Norwegian Krone", "🇳🇴", 10.92),
        CurrencyInfo("HKD", "HK$", "Hong Kong Dollar", "🇭🇰", 7.825),
        CurrencyInfo("IDR", "Rp", "Indonesian Rupiah", "🇮🇩", 16250.0),
        CurrencyInfo("MYR", "RM", "Malaysian Ringgit", "🇲🇾", 4.745),
        CurrencyInfo("THB", "฿", "Thai Baht", "🇹🇭", 36.85),
        CurrencyInfo("PHP", "₱", "Philippine Peso", "🇵🇭", 57.60)
    )

    fun convertCurrency(
        amount: Double,
        from: CurrencyInfo,
        to: CurrencyInfo,
        updatedTimestamp: Long = System.currentTimeMillis()
    ): CurrencyConversionResult {
        val safeAmount = if (amount.isNaN() || amount < 0) 0.0 else amount
        // 1 FROM = (1 / from.rateToUSD) * to.rateToUSD in TO
        val directRate = (1.0 / from.rateToUSD) * to.rateToUSD
        val reciprocalRate = if (directRate > 0) 1.0 / directRate else 0.0
        val converted = safeAmount * directRate

        val timeStr = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(updatedTimestamp))

        return CurrencyConversionResult(
            from = from,
            to = to,
            amount = safeAmount,
            convertedAmount = converted,
            directRate = directRate,
            reciprocalRate = reciprocalRate,
            lastUpdatedText = timeStr,
            isIndicativeOffline = true
        )
    }

    fun calculateTax(
        amount: Double,
        ratePercent: Double,
        isTaxInclusive: Boolean
    ): TaxCalculationResult {
        val safeAmount = if (amount.isNaN() || amount < 0) 0.0 else amount
        val safeRate = if (ratePercent.isNaN() || ratePercent < 0) 0.0 else ratePercent

        return if (isTaxInclusive) {
            // Price already includes tax
            val net = safeAmount / (1.0 + safeRate / 100.0)
            val tax = safeAmount - net
            TaxCalculationResult(
                baseAmount = safeAmount,
                ratePercent = safeRate,
                isTaxInclusive = true,
                netAmount = net,
                taxAmount = tax,
                totalAmount = safeAmount
            )
        } else {
            // Tax will be added on top
            val tax = safeAmount * (safeRate / 100.0)
            val total = safeAmount + tax
            TaxCalculationResult(
                baseAmount = safeAmount,
                ratePercent = safeRate,
                isTaxInclusive = false,
                netAmount = safeAmount,
                taxAmount = tax,
                totalAmount = total
            )
        }
    }

    fun calculateLoan(
        principal: Double,
        annualRatePercent: Double,
        tenureMonths: Int
    ): LoanCalculationResult {
        val p = if (principal.isNaN() || principal < 0) 0.0 else principal
        val rAnnual = if (annualRatePercent.isNaN() || annualRatePercent < 0) 0.0 else annualRatePercent
        val n = tenureMonths.coerceAtLeast(1)

        val monthlyRate = (rAnnual / 100.0) / 12.0

        val emi = if (p <= 0.0) {
            0.0
        } else if (monthlyRate <= 0.0) {
            p / n
        } else {
            val factor = (1.0 + monthlyRate).pow(n.toDouble())
            (p * monthlyRate * factor) / (factor - 1.0)
        }

        val totalPayment = emi * n
        val totalInterest = (totalPayment - p).coerceAtLeast(0.0)

        // Generate up to 12 sample amortization rows for schedule preview
        val sampleList = mutableListOf<AmortizationMonth>()
        var balance = p
        val previewMonths = minOf(n, 12)
        for (m in 1..previewMonths) {
            val interestPart = balance * monthlyRate
            val principalPart = emi - interestPart
            balance = (balance - principalPart).coerceAtLeast(0.0)
            sampleList.add(
                AmortizationMonth(
                    month = m,
                    payment = emi,
                    principalPayment = principalPart,
                    interestPayment = interestPart,
                    remainingBalance = balance
                )
            )
        }

        return LoanCalculationResult(
            principal = p,
            annualRatePercent = rAnnual,
            tenureMonths = n,
            monthlyEMI = emi,
            totalPayment = totalPayment,
            totalInterest = totalInterest,
            sampleSchedule = sampleList
        )
    }

    fun calculateDiscount(
        originalPrice: Double,
        primaryDiscountPercent: Double,
        couponPercent: Double = 0.0
    ): DiscountCalculationResult {
        val price = if (originalPrice.isNaN() || originalPrice < 0) 0.0 else originalPrice
        val d1 = primaryDiscountPercent.coerceIn(0.0, 100.0)
        val d2 = couponPercent.coerceIn(0.0, 100.0)

        val priceAfterD1 = price * (1.0 - d1 / 100.0)
        val finalPrice = priceAfterD1 * (1.0 - d2 / 100.0)
        val saved = price - finalPrice

        return DiscountCalculationResult(
            originalPrice = price,
            primaryDiscountPercent = d1,
            couponPercent = d2,
            savedAmount = saved,
            finalPrice = finalPrice
        )
    }

    fun calculateTipAndSplit(
        billAmount: Double,
        tipPercent: Double,
        personCount: Int
    ): TipCalculationResult {
        val bill = if (billAmount.isNaN() || billAmount < 0) 0.0 else billAmount
        val tipRate = if (tipPercent.isNaN() || tipPercent < 0) 0.0 else tipPercent
        val people = personCount.coerceAtLeast(1)

        val tipAmount = bill * (tipRate / 100.0)
        val totalBill = bill + tipAmount
        val perPersonBill = bill / people
        val perPersonTip = tipAmount / people
        val perPersonTotal = totalBill / people

        return TipCalculationResult(
            billAmount = bill,
            tipPercent = tipRate,
            personCount = people,
            tipAmount = tipAmount,
            totalBill = totalBill,
            perPersonBill = perPersonBill,
            perPersonTip = perPersonTip,
            perPersonTotal = perPersonTotal
        )
    }

    fun calculateMarginAndMarkup(
        costPrice: Double,
        sellingPrice: Double
    ): MarginCalculationResult {
        val cost = if (costPrice.isNaN() || costPrice < 0) 0.0 else costPrice
        val sell = if (sellingPrice.isNaN() || sellingPrice < 0) 0.0 else sellingPrice

        val grossProfit = sell - cost
        val marginPercent = if (sell > 0.0) (grossProfit / sell) * 100.0 else 0.0
        val markupPercent = if (cost > 0.0) (grossProfit / cost) * 100.0 else 0.0

        return MarginCalculationResult(
            costPrice = cost,
            sellingPrice = sell,
            grossProfit = grossProfit,
            marginPercent = marginPercent,
            markupPercent = markupPercent
        )
    }
}
