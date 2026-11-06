package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.KeypadEqualsBgDark
import com.example.ui.theme.KeypadFunctionBgDark
import com.example.ui.theme.KeypadFunctionBgLight
import com.example.ui.theme.KeypadNumberBgDark
import com.example.ui.theme.KeypadNumberBgLight
import com.example.ui.theme.KeypadOperatorBgDark
import com.example.ui.theme.MintSecondary

enum class KeypadButtonType {
    NUMBER,
    OPERATOR,
    FUNCTION,
    EQUALS,
    MEMORY
}

@Composable
fun CalcButton(
    text: String,
    type: KeypadButtonType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "btn_$text",
    isHapticEnabled: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "button_scale"
    )

    // Determine colors based on type and current theme
    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val (containerColor, contentColor) = when (type) {
        KeypadButtonType.NUMBER -> {
            if (isDark) Pair(KeypadNumberBgDark, MaterialTheme.colorScheme.onBackground)
            else Pair(KeypadNumberBgLight, MaterialTheme.colorScheme.onBackground)
        }
        KeypadButtonType.OPERATOR -> {
            Pair(AmberPrimary, Color.White)
        }
        KeypadButtonType.FUNCTION -> {
            if (isDark) Pair(KeypadFunctionBgDark, MintSecondary)
            else Pair(KeypadFunctionBgLight, MaterialTheme.colorScheme.onSurface)
        }
        KeypadButtonType.EQUALS -> {
            Pair(KeypadEqualsBgDark, Color.Black)
        }
        KeypadButtonType.MEMORY -> {
            if (isDark) Pair(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
            else Pair(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }

    Surface(
        modifier = modifier
            .scale(scale)
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .testTag(testTag)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    if (isHapticEnabled) {
                        try {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        } catch (_: Exception) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    }
                    onClick()
                }
            ),
        shape = if (type == KeypadButtonType.MEMORY) RoundedCornerShape(12.dp) else CircleShape,
        color = containerColor,
        tonalElevation = if (type == KeypadButtonType.OPERATOR || type == KeypadButtonType.EQUALS) 4.dp else 1.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(vertical = if (type == KeypadButtonType.MEMORY) 8.dp else 12.dp)
        ) {
            Text(
                text = text,
                style = if (type == KeypadButtonType.MEMORY) {
                    MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                } else {
                    MaterialTheme.typography.titleLarge.copy(
                        fontSize = when {
                            text.length > 2 -> 20.sp
                            text == "+" || text == "−" || text == "×" || text == "÷" || text == "=" -> 28.sp
                            else -> 24.sp
                        },
                        fontWeight = if (type == KeypadButtonType.OPERATOR || type == KeypadButtonType.EQUALS) FontWeight.Bold else FontWeight.Medium
                    )
                },
                color = contentColor
            )
        }
    }
}

@Composable
fun CalculatorKeypad(
    onDigit: (String) -> Unit,
    onDecimal: () -> Unit,
    onOperator: (String) -> Unit,
    onParenthesis: () -> Unit,
    onClear: () -> Unit,
    onNegate: () -> Unit,
    onPercentage: () -> Unit,
    onEquals: () -> Unit,
    onMemoryClear: () -> Unit,
    onMemoryRecall: () -> Unit,
    onMemoryAdd: () -> Unit,
    onMemorySubtract: () -> Unit,
    onMemoryStore: () -> Unit,
    hasMemory: Boolean,
    isHapticEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Row 0: Memory Pill Buttons (MC, MR, M+, M-, MS)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CalcButton(
                text = "MC",
                type = KeypadButtonType.MEMORY,
                onClick = onMemoryClear,
                modifier = Modifier.weight(1f),
                testTag = "btn_mc",
                isHapticEnabled = isHapticEnabled
            )
            CalcButton(
                text = "MR",
                type = KeypadButtonType.MEMORY,
                onClick = onMemoryRecall,
                modifier = Modifier.weight(1f),
                testTag = "btn_mr",
                isHapticEnabled = isHapticEnabled
            )
            CalcButton(
                text = "M+",
                type = KeypadButtonType.MEMORY,
                onClick = onMemoryAdd,
                modifier = Modifier.weight(1f),
                testTag = "btn_m_plus",
                isHapticEnabled = isHapticEnabled
            )
            CalcButton(
                text = "M-",
                type = KeypadButtonType.MEMORY,
                onClick = onMemorySubtract,
                modifier = Modifier.weight(1f),
                testTag = "btn_m_minus",
                isHapticEnabled = isHapticEnabled
            )
            CalcButton(
                text = "MS",
                type = KeypadButtonType.MEMORY,
                onClick = onMemoryStore,
                modifier = Modifier.weight(1f),
                testTag = "btn_ms",
                isHapticEnabled = isHapticEnabled
            )
        }

        // Row 1: AC, ±, %, ÷
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CalcButton(
                text = "AC",
                type = KeypadButtonType.FUNCTION,
                onClick = onClear,
                modifier = Modifier.weight(1f).aspectRatio(1.15f),
                testTag = "btn_ac",
                isHapticEnabled = isHapticEnabled
            )
            CalcButton(
                text = "±",
                type = KeypadButtonType.FUNCTION,
                onClick = onNegate,
                modifier = Modifier.weight(1f).aspectRatio(1.15f),
                testTag = "btn_negate",
                isHapticEnabled = isHapticEnabled
            )
            CalcButton(
                text = "%",
                type = KeypadButtonType.FUNCTION,
                onClick = onPercentage,
                modifier = Modifier.weight(1f).aspectRatio(1.15f),
                testTag = "btn_percent",
                isHapticEnabled = isHapticEnabled
            )
            CalcButton(
                text = "÷",
                type = KeypadButtonType.OPERATOR,
                onClick = { onOperator("÷") },
                modifier = Modifier.weight(1f).aspectRatio(1.15f),
                testTag = "btn_divide",
                isHapticEnabled = isHapticEnabled
            )
        }

        // Row 2: 7, 8, 9, ×
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CalcButton(
                text = "7",
                type = KeypadButtonType.NUMBER,
                onClick = { onDigit("7") },
                modifier = Modifier.weight(1f).aspectRatio(1.15f),
                testTag = "btn_7",
                isHapticEnabled = isHapticEnabled
            )
            CalcButton(
                text = "8",
                type = KeypadButtonType.NUMBER,
                onClick = { onDigit("8") },
                modifier = Modifier.weight(1f).aspectRatio(1.15f),
                testTag = "btn_8",
                isHapticEnabled = isHapticEnabled
            )
            CalcButton(
                text = "9",
                type = KeypadButtonType.NUMBER,
                onClick = { onDigit("9") },
                modifier = Modifier.weight(1f).aspectRatio(1.15f),
                testTag = "btn_9",
                isHapticEnabled = isHapticEnabled
            )
            CalcButton(
                text = "×",
                type = KeypadButtonType.OPERATOR,
                onClick = { onOperator("×") },
                modifier = Modifier.weight(1f).aspectRatio(1.15f),
                testTag = "btn_multiply",
                isHapticEnabled = isHapticEnabled
            )
        }

        // Row 3: 4, 5, 6, −
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CalcButton(
                text = "4",
                type = KeypadButtonType.NUMBER,
                onClick = { onDigit("4") },
                modifier = Modifier.weight(1f).aspectRatio(1.15f),
                testTag = "btn_4",
                isHapticEnabled = isHapticEnabled
            )
            CalcButton(
                text = "5",
                type = KeypadButtonType.NUMBER,
                onClick = { onDigit("5") },
                modifier = Modifier.weight(1f).aspectRatio(1.15f),
                testTag = "btn_5",
                isHapticEnabled = isHapticEnabled
            )
            CalcButton(
                text = "6",
                type = KeypadButtonType.NUMBER,
                onClick = { onDigit("6") },
                modifier = Modifier.weight(1f).aspectRatio(1.15f),
                testTag = "btn_6",
                isHapticEnabled = isHapticEnabled
            )
            CalcButton(
                text = "−",
                type = KeypadButtonType.OPERATOR,
                onClick = { onOperator("−") },
                modifier = Modifier.weight(1f).aspectRatio(1.15f),
                testTag = "btn_minus",
                isHapticEnabled = isHapticEnabled
            )
        }

        // Row 4: 1, 2, 3, +
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CalcButton(
                text = "1",
                type = KeypadButtonType.NUMBER,
                onClick = { onDigit("1") },
                modifier = Modifier.weight(1f).aspectRatio(1.15f),
                testTag = "btn_1",
                isHapticEnabled = isHapticEnabled
            )
            CalcButton(
                text = "2",
                type = KeypadButtonType.NUMBER,
                onClick = { onDigit("2") },
                modifier = Modifier.weight(1f).aspectRatio(1.15f),
                testTag = "btn_2",
                isHapticEnabled = isHapticEnabled
            )
            CalcButton(
                text = "3",
                type = KeypadButtonType.NUMBER,
                onClick = { onDigit("3") },
                modifier = Modifier.weight(1f).aspectRatio(1.15f),
                testTag = "btn_3",
                isHapticEnabled = isHapticEnabled
            )
            CalcButton(
                text = "+",
                type = KeypadButtonType.OPERATOR,
                onClick = { onOperator("+") },
                modifier = Modifier.weight(1f).aspectRatio(1.15f),
                testTag = "btn_plus",
                isHapticEnabled = isHapticEnabled
            )
        }

        // Row 5: (), 0, ., =
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CalcButton(
                text = "( )",
                type = KeypadButtonType.FUNCTION,
                onClick = onParenthesis,
                modifier = Modifier.weight(1f).aspectRatio(1.15f),
                testTag = "btn_parenthesis",
                isHapticEnabled = isHapticEnabled
            )
            CalcButton(
                text = "0",
                type = KeypadButtonType.NUMBER,
                onClick = { onDigit("0") },
                modifier = Modifier.weight(1f).aspectRatio(1.15f),
                testTag = "btn_0",
                isHapticEnabled = isHapticEnabled
            )
            CalcButton(
                text = ".",
                type = KeypadButtonType.NUMBER,
                onClick = onDecimal,
                modifier = Modifier.weight(1f).aspectRatio(1.15f),
                testTag = "btn_decimal",
                isHapticEnabled = isHapticEnabled
            )
            CalcButton(
                text = "=",
                type = KeypadButtonType.EQUALS,
                onClick = onEquals,
                modifier = Modifier.weight(1f).aspectRatio(1.15f),
                testTag = "btn_equals",
                isHapticEnabled = isHapticEnabled
            )
        }
    }
}
