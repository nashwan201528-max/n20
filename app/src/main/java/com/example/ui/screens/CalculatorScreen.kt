package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.calculator.CalculatorViewModel
import com.example.ui.components.CalculatorDisplay
import com.example.ui.components.CalculatorKey
import com.example.ui.components.KeyType

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Display Section
        CalculatorDisplay(
            expression = uiState.expression,
            livePreview = uiState.livePreview,
            committedResult = uiState.committedResult,
            errorMessage = uiState.errorMessage,
            isDegreeMode = uiState.isDegreeMode,
            hasMemory = uiState.hasMemory,
            modifier = Modifier.weight(1f, fill = false)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Control Toolbar: Mode selector & Memory keys
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Standard / Scientific Switcher
            FilterChip(
                selected = uiState.isScientificExpanded,
                onClick = { viewModel.toggleScientific() },
                label = {
                    Text(
                        text = if (uiState.isScientificExpanded) "Scientific" else "Standard",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (uiState.isScientificExpanded) Icons.Filled.Science else Icons.Outlined.Science,
                        contentDescription = "Toggle Scientific Mode",
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier.testTag("toggle_scientific_mode")
            )

            // Angle Mode (when in scientific mode)
            if (uiState.isScientificExpanded) {
                TextButton(
                    onClick = { viewModel.toggleAngleMode() },
                    modifier = Modifier.testTag("btn_toggle_angle")
                ) {
                    Text(
                        text = if (uiState.isDegreeMode) "DEG → RAD" else "RAD → DEG",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Memory action chips
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                AssistChip(
                    onClick = { viewModel.onMemoryClear() },
                    label = { Text("MC", fontSize = 11.sp) },
                    enabled = uiState.hasMemory,
                    modifier = Modifier.testTag("btn_memory_clear")
                )
                AssistChip(
                    onClick = { viewModel.onMemoryRecall() },
                    label = { Text("MR", fontSize = 11.sp) },
                    enabled = uiState.hasMemory,
                    modifier = Modifier.testTag("btn_memory_recall")
                )
                AssistChip(
                    onClick = { viewModel.onMemoryAdd() },
                    label = { Text("M+", fontSize = 11.sp) },
                    modifier = Modifier.testTag("btn_memory_add")
                )
                AssistChip(
                    onClick = { viewModel.onMemorySubtract() },
                    label = { Text("M-", fontSize = 11.sp) },
                    modifier = Modifier.testTag("btn_memory_sub")
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Scientific Keys Row (Collapsible)
        AnimatedVisibility(
            visible = uiState.isScientificExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CalculatorKey("sin", { viewModel.onFunction("sin") }, Modifier.weight(1f), KeyType.FUNCTION, fontSize = 15.sp, height = 48.dp)
                    CalculatorKey("cos", { viewModel.onFunction("cos") }, Modifier.weight(1f), KeyType.FUNCTION, fontSize = 15.sp, height = 48.dp)
                    CalculatorKey("tan", { viewModel.onFunction("tan") }, Modifier.weight(1f), KeyType.FUNCTION, fontSize = 15.sp, height = 48.dp)
                    CalculatorKey("ln", { viewModel.onFunction("ln") }, Modifier.weight(1f), KeyType.FUNCTION, fontSize = 15.sp, height = 48.dp)
                    CalculatorKey("log", { viewModel.onFunction("log") }, Modifier.weight(1f), KeyType.FUNCTION, fontSize = 15.sp, height = 48.dp)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CalculatorKey("√", { viewModel.onFunction("sqrt") }, Modifier.weight(1f), KeyType.FUNCTION, fontSize = 18.sp, height = 48.dp)
                    CalculatorKey("^", { viewModel.onOperator("^") }, Modifier.weight(1f), KeyType.FUNCTION, fontSize = 18.sp, height = 48.dp)
                    CalculatorKey("π", { viewModel.onConstant("π") }, Modifier.weight(1f), KeyType.FUNCTION, fontSize = 18.sp, height = 48.dp)
                    CalculatorKey("e", { viewModel.onConstant("e") }, Modifier.weight(1f), KeyType.FUNCTION, fontSize = 18.sp, height = 48.dp)
                    CalculatorKey("n!", { viewModel.onFunction("fact") }, Modifier.weight(1f), KeyType.FUNCTION, fontSize = 15.sp, height = 48.dp)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Standard 4x5 Keypad Grid
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Row 1: AC, (), %, ÷
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CalculatorKey("AC", { viewModel.onClear() }, Modifier.weight(1f), KeyType.CLEAR)
                CalculatorKey("( )", { viewModel.onParentheses() }, Modifier.weight(1f), KeyType.FUNCTION)
                CalculatorKey("%", { viewModel.onOperator("%") }, Modifier.weight(1f), KeyType.OPERATOR)
                CalculatorKey("÷", { viewModel.onOperator("÷") }, Modifier.weight(1f), KeyType.OPERATOR)
            }

            // Row 2: 7, 8, 9, ×
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CalculatorKey("7", { viewModel.onDigit("7") }, Modifier.weight(1f))
                CalculatorKey("8", { viewModel.onDigit("8") }, Modifier.weight(1f))
                CalculatorKey("9", { viewModel.onDigit("9") }, Modifier.weight(1f))
                CalculatorKey("×", { viewModel.onOperator("×") }, Modifier.weight(1f), KeyType.OPERATOR)
            }

            // Row 3: 4, 5, 6, -
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CalculatorKey("4", { viewModel.onDigit("4") }, Modifier.weight(1f))
                CalculatorKey("5", { viewModel.onDigit("5") }, Modifier.weight(1f))
                CalculatorKey("6", { viewModel.onDigit("6") }, Modifier.weight(1f))
                CalculatorKey("-", { viewModel.onOperator("-") }, Modifier.weight(1f), KeyType.OPERATOR)
            }

            // Row 4: 1, 2, 3, +
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CalculatorKey("1", { viewModel.onDigit("1") }, Modifier.weight(1f))
                CalculatorKey("2", { viewModel.onDigit("2") }, Modifier.weight(1f))
                CalculatorKey("3", { viewModel.onDigit("3") }, Modifier.weight(1f))
                CalculatorKey("+", { viewModel.onOperator("+") }, Modifier.weight(1f), KeyType.OPERATOR)
            }

            // Row 5: 0, ., ⌫, =
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CalculatorKey("0", { viewModel.onDigit("0") }, Modifier.weight(1f))
                CalculatorKey(".", { viewModel.onDecimal() }, Modifier.weight(1f))
                CalculatorKey("⌫", { viewModel.onBackspace() }, Modifier.weight(1f), KeyType.FUNCTION)
                CalculatorKey("=", { viewModel.onEquals() }, Modifier.weight(1f), KeyType.ACTION)
            }
        }
    }
}
