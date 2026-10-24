package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.calculator.ExpressionEvaluator
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CoralRed
import com.example.ui.theme.TealAccent

@Composable
fun CalculatorDisplay(
    expression: String,
    livePreview: String?,
    committedResult: String?,
    errorMessage: String?,
    isDegreeMode: Boolean,
    hasMemory: Boolean,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Auto scroll to end when expression changes
    LaunchedEffect(expression) {
        if (expression.isNotEmpty()) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.End
    ) {
        // Status row: Angle mode and memory indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.testTag("badge_angle_mode")
                ) {
                    Text(
                        text = if (isDegreeMode) "DEG" else "RAD",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                if (hasMemory) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AmberAccent.copy(alpha = 0.2f),
                        modifier = Modifier.testTag("badge_memory")
                    ) {
                        Text(
                            text = "M",
                            color = AmberAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Text(
                text = "Precision 64-bit",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Main Expression Display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            contentAlignment = Alignment.CenterEnd
        ) {
            val displayExpression = if (expression.isEmpty()) "0" else ExpressionEvaluator.sanitizeForDisplay(expression)
            Text(
                text = displayExpression,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = if (displayExpression.length > 12) 32.sp else 42.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End,
                maxLines = 1,
                modifier = Modifier.testTag("calculator_expression")
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Live Preview or Error Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            this@Column.AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CoralRed.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .testTag("calculator_error")
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = "Error",
                        tint = CoralRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = errorMessage ?: "",
                        color = CoralRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            this@Column.AnimatedVisibility(
                visible = errorMessage == null && livePreview != null && livePreview != expression,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = "= $livePreview",
                    color = TealAccent,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.End,
                    modifier = Modifier.testTag("calculator_live_preview")
                )
            }
        }
    }
}
