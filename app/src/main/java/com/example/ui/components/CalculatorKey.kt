package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

enum class KeyType {
    NUMERIC,
    OPERATOR,
    ACTION,
    FUNCTION,
    CLEAR
}

@Composable
fun CalculatorKey(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: KeyType = KeyType.NUMERIC,
    fontSize: TextUnit = 22.sp,
    height: Dp = 62.dp,
    testTag: String = "key_${text.lowercase().replace(" ", "_")}"
) {
    val isDark = MaterialTheme.colorScheme.background == Slate950 ||
            MaterialTheme.colorScheme.surface == Slate900

    val (bgColor, contentColor) = when (type) {
        KeyType.NUMERIC -> {
            if (isDark) KeypadNumDark to Slate100
            else KeypadNumLight to Slate900
        }
        KeyType.OPERATOR -> {
            if (isDark) KeypadOpDark to TealAccent
            else KeypadOpLight to TealPrimary
        }
        KeyType.ACTION -> {
            if (isDark) KeypadActionTeal to Color.White
            else KeypadActionLight to Color.White
        }
        KeyType.FUNCTION -> {
            if (isDark) KeypadSpecialDark to Slate300
            else KeypadSpecialLight to Slate700
        }
        KeyType.CLEAR -> {
            if (isDark) KeypadClearRed to Color.White
            else CoralRed to Color.White
        }
    }

    Box(
        modifier = modifier
            .height(height)
            .padding(3.dp)
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = contentColor.copy(alpha = 0.3f)),
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = contentColor,
            fontSize = fontSize,
            fontWeight = if (type == KeyType.ACTION || type == KeyType.CLEAR) FontWeight.Bold else FontWeight.Medium
        )
    }
}
