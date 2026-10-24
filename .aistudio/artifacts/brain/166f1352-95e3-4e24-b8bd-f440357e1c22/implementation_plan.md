# Transaction & Financial Calculator Android Application

A comprehensive, professional-grade financial and daily transaction calculator built in Kotlin with Jetpack Compose and Material Design 3. The app serves students, working professionals, and everyday users with an all-in-one suite combining high-precision mathematical evaluation, advanced scientific functions, and dedicated financial transaction calculators—including currency conversion, tax/VAT, loan installments, discounts, and bill splitting.

---

### User Review & Critical Decisions

> [!IMPORTANT]
> **Key Architecture & Feature Decisions**:
> - **Navigation Strategy**: A persistent top/bottom Material Design 3 navigation bar with 4 primary workspaces: **Calculator** (with toggle between Standard & Scientific), **Transactions Hub** (Currency, Tax, Discount, Loan, Tip, Margin), **History Tape** (searchable audit log of calculations), and **Settings/Preferences**.
> - **Currency Conversion Implementation**: Integrated offline currency conversion engine supporting 20+ major global currencies (USD, EUR, GBP, JPY, CAD, AUD, CHF, CNY, INR, AED, SGD, etc.) with a mock/placeholder exchange-rate repository, exchange timestamp, reciprocal rates, one-tap swap, and a prominent badge explicitly clarifying *"Indicative offline rates for planning purposes"*.
> - **Expression Parsing Engine**: Shunting-Yard algorithm implementation supporting operator precedence, unary minus, implicit multiplication, parentheses balancing, real-time syntax validation, and graceful handling of division by zero without crashes.
> - **Local Data Persistence**: High-performance local Room database and Kotlin Coroutines/Flow for persisting calculation history, custom tax presets, and transaction notes.

---

### 1. Overview & Core Concept

- **What It Does**: Provides a unified, lightning-fast computing environment that transitions seamlessly between everyday arithmetic, scientific calculations, and practical commerce/financial equations.
- **Target Audience / Persona**:
  - *Students & Academics*: Require reliable scientific formulas, trigonometric functions, exponents, and clean parentheses handling.
  - *Professionals & Merchants*: Need quick calculations for VAT/GST inclusion/exclusion, profit margins, markups, and stacked promotional discounts.
  - *Everyday Users & Travelers*: Need instant bill splitting with tip adjustment and offline currency conversions with intuitive numpads.
- **Key Value**: Replaces 3–4 disjointed utility apps with a single cohesive, high-polish Material 3 application that validates inputs in real time and maintains an interactive audit tape.

---

### 2. User Experience & Visual Design

#### Key User Flows
1. **Standard & Scientific Calculation Flow**:
   - User inputs numbers and operators via a responsive haptic keypad.
   - Secondary live result preview evaluates expressions in real time as the user types.
   - Tapping `=` commits the calculation to the history tape and displays the full answer with thousand-separators.
   - Switching between **Standard** and **Scientific** expands functions (sin, cos, tan, ln, log, $\sqrt{}$, $x^y$, $\pi$, $e$, Rad/Deg mode) without clearing current input.
2. **Currency Conversion Flow**:
   - Quick card access to **Currency Converter**.
   - Dual currency selectors (source flag + currency code to target currency).
   - Instant two-way conversion with swap button, rate comparison card, and "Simulate Rate Update" action with last-updated timestamp and disclaimer badge.
3. **Transaction & Commerce Modules Flow**:
   - **Tax / VAT / GST**: Toggle between Tax-Exclusive (Add Tax) and Tax-Inclusive (Extract Tax) with customizable rates and regional presets (5%, 10%, 15%, 20%).
   - **Loan / EMI Installments**: Enter principal, annual interest rate, and term (months/years) to instantly see monthly payment, total interest, and total repayment breakdown.
   - **Discount & Promotions**: Enter list price, discount %, and secondary coupon/member discount to inspect exact savings and net payable price.
   - **Tip & Split Bill**: Set bill amount, select 10%/15%/18%/20%/custom tip, and adjust number of diners to view per-person breakdown.
   - **Margin & Markup**: Enter cost and selling price to compute gross margin % and markup %.
4. **History Tape & Receipt Export**:
   - View chronological history tape of all calculations across modes.
   - Tap any past item to load formula or result back into the active calculator display, copy to clipboard, add notes, or export calculation summary.

#### Visual Identity & Theme
- **Theme Concept**: *FinTech Slate & Teal* — Deep slate backgrounds (`#0F172A`), rich charcoal surface cards (`#1E293B`), accented by vivid financial teal (`#0D9488` / `#14B8A6`) for results/actions, amber accents for financial tools (`#F59E0B`), and clear crimson for destructive/clear keys (`#EF4444`).
- **Dynamic Theming**: Full support for Android Material You Dynamic Colors when enabled, with custom tuned fallback light/dark palettes.
- **Typography & Scale**: High-legibility monospaced figures for large numeric displays (`sp` scaled), crisp Material 3 typography hierarchy for labels and transaction summaries.
- **Layout Precision & Ergonomics**:
  - Minimum 48dp touch targets on all keypad buttons with tactile ripple feedback.
  - Large display screen featuring auto-shrinking text size for long expressions and horizontal scrollability.
  - Tablet & foldable responsive split layout (keypad on right, interactive tape on left).

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Mathematical Parser (Custom Shunting-Yard vs. External Library)**
  - *Chosen Approach*: Lightweight, zero-dependency Kotlin Dijkstra Shunting-Yard parser supporting arbitrary decimal precision (`BigDecimal`), parentheses validation, implicit multiplication (`2(3+4)` $\rightarrow 14$), and scientific functions.
  - *Why*: Eliminates fragile external library dependencies, avoids JVM engine overhead, and allows fine-grained error messages (e.g. distinguishing mismatched parenthesis from division-by-zero).
- **Decision 2: Currency Rates Handling**
  - *Chosen Approach*: Bundled offline exchange rate matrix for 20+ currencies with rate multiplier logic, simulated refresh animation, and an explicit visual disclaimer (`"Offline Indicative Rates"`).
  - *Why*: Provides reliable, instant calculation without requiring third-party API keys or failing in airplane mode, while matching user instructions for placeholder exchange rate functionality.
- **Decision 3: History & Presets Persistence**
  - *Chosen Approach*: Android Room database with DAO flow queries.
  - *Why*: Calculation history and user tax presets remain preserved across app relaunches and orientation changes without needing network connectivity.

---

### 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Jetpack Compose UI Layer                        │
├──────────────────┬──────────────────────┬──────────────────────────────┤
│  CalculatorView  │  FinancialHubView    │  HistoryTapeView & Dialogs   │
│  - Standard Pad  │  - Currency Converter│  - Filter & Search           │
│  - Scientific Pad│  - Tax / VAT Module  │  - Item Recall & Export      │
│  - Dynamic Screen│  - Loan EMI Module   │  - Custom Notes & Tags       │
│  - Live Preview  │  - Discount / Tip    │                              │
└────────┬─────────┴──────────┬───────────┴──────────────┬───────────────┘
         │                    │                          │
         ▼                    ▼                          ▼
┌────────────────────────────────────────────────────────────────────────┐
│                       ViewModel Layer (StateFlow)                      │
│ - CalculatorViewModel   - TransactionViewModel  - HistoryViewModel     │
└────────┬────────────────────┬──────────────────────────┬───────────────┘
         │                    │                          │
         ▼                    ▼                          ▼
┌────────────────────────────────────────────────────────────────────────┐
│                     Domain Engines & Utility Math                      │
│ - ExpressionEvaluator (Tokenization, Shunting-Yard, BigDecimal Math)  │
│ - FinancialMathUtil (EMI, Amortization, Tax Gross/Net, Margin, Tip)    │
│ - CurrencyEngine (Matrix conversions, reciprocal rates, formatting)   │
└────────┬───────────────────────────────────────────────┬───────────────┘
         │                                               │
         ▼                                               ▼
┌─────────────────────────────────┐       ┌──────────────────────────────┐
│       Room Local Database       │       │    Repository & DataStore    │
│  - CalculationHistoryEntity     │       │  - CurrencyRateRepository    │
│  - HistoryDao (Flow/Coroutines) │       │  - UserPreferencesDataStore  │
└─────────────────────────────────┘       └──────────────────────────────┘
```

#### Verification & Test Strategy
- **Core Math Unit Tests (`CalculatorEngineTest`)**:
  - PEMDAS precedence: `2 + 3 * 4 = 14`, `(2 + 3) * 4 = 20`.
  - Floating point precision: `0.1 + 0.2 = 0.3`.
  - Edge cases: Division by zero `5 / 0 -> "Cannot divide by zero"`, consecutive operators `5 + * 2 -> error prevention`, unbalanced parentheses `(5 + 2`.
  - Scientific evaluation: `sin(90 deg) = 1`, `sqrt(144) = 12`, `2^10 = 1024`.
- **Financial Logic Unit Tests (`FinancialMathTest`)**:
  - Loan EMI calculation against standard amortization benchmarks.
  - Tax calculation: inclusive vs exclusive rounding verification.
  - Stacked discount calculations (e.g. $100 with 20% off + extra 10% coupon = $72).
  - Tip & diner split distribution verification.
- **Currency Conversion Tests (`CurrencyConverterTest`)**:
  - Direct conversion, cross-rate calculation via base USD/EUR, and reciprocal consistency.
