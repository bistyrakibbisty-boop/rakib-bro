package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ScientificKeypad(
    isExpanded: Boolean,
    isRadian: Boolean,
    onToggleRadian: () -> Unit,
    onFunction: (String) -> Unit,
    onConstant: (String) -> Unit,
    onFactorial: () -> Unit,
    onPower: () -> Unit,
    onSquare: () -> Unit,
    onInverse: () -> Unit,
    isHapticEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    var isInverseMode by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = isExpanded,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Row 1: 2nd, RAD/DEG, sin/asin, cos/acos, tan/atan
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CalcButton(
                        text = if (isInverseMode) "1st" else "2nd",
                        type = KeypadButtonType.FUNCTION,
                        onClick = { isInverseMode = !isInverseMode },
                        modifier = Modifier.weight(1f).aspectRatio(1.6f),
                        testTag = "btn_sc_2nd",
                        isHapticEnabled = isHapticEnabled
                    )
                    CalcButton(
                        text = if (isRadian) "RAD" else "DEG",
                        type = KeypadButtonType.FUNCTION,
                        onClick = onToggleRadian,
                        modifier = Modifier.weight(1f).aspectRatio(1.6f),
                        testTag = "btn_sc_rad_deg",
                        isHapticEnabled = isHapticEnabled
                    )
                    CalcButton(
                        text = if (isInverseMode) "sin⁻¹" else "sin",
                        type = KeypadButtonType.FUNCTION,
                        onClick = { onFunction(if (isInverseMode) "asin" else "sin") },
                        modifier = Modifier.weight(1f).aspectRatio(1.6f),
                        testTag = "btn_sc_sin",
                        isHapticEnabled = isHapticEnabled
                    )
                    CalcButton(
                        text = if (isInverseMode) "cos⁻¹" else "cos",
                        type = KeypadButtonType.FUNCTION,
                        onClick = { onFunction(if (isInverseMode) "acos" else "cos") },
                        modifier = Modifier.weight(1f).aspectRatio(1.6f),
                        testTag = "btn_sc_cos",
                        isHapticEnabled = isHapticEnabled
                    )
                    CalcButton(
                        text = if (isInverseMode) "tan⁻¹" else "tan",
                        type = KeypadButtonType.FUNCTION,
                        onClick = { onFunction(if (isInverseMode) "atan" else "tan") },
                        modifier = Modifier.weight(1f).aspectRatio(1.6f),
                        testTag = "btn_sc_tan",
                        isHapticEnabled = isHapticEnabled
                    )
                }

                // Row 2: ln, log, √, ∛, xʸ
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CalcButton(
                        text = "ln",
                        type = KeypadButtonType.FUNCTION,
                        onClick = { onFunction("ln") },
                        modifier = Modifier.weight(1f).aspectRatio(1.6f),
                        testTag = "btn_sc_ln",
                        isHapticEnabled = isHapticEnabled
                    )
                    CalcButton(
                        text = "log",
                        type = KeypadButtonType.FUNCTION,
                        onClick = { onFunction("log") },
                        modifier = Modifier.weight(1f).aspectRatio(1.6f),
                        testTag = "btn_sc_log",
                        isHapticEnabled = isHapticEnabled
                    )
                    CalcButton(
                        text = "√",
                        type = KeypadButtonType.FUNCTION,
                        onClick = { onFunction("sqrt") },
                        modifier = Modifier.weight(1f).aspectRatio(1.6f),
                        testTag = "btn_sc_sqrt",
                        isHapticEnabled = isHapticEnabled
                    )
                    CalcButton(
                        text = "∛",
                        type = KeypadButtonType.FUNCTION,
                        onClick = { onFunction("cbrt") },
                        modifier = Modifier.weight(1f).aspectRatio(1.6f),
                        testTag = "btn_sc_cbrt",
                        isHapticEnabled = isHapticEnabled
                    )
                    CalcButton(
                        text = "xʸ",
                        type = KeypadButtonType.FUNCTION,
                        onClick = onPower,
                        modifier = Modifier.weight(1f).aspectRatio(1.6f),
                        testTag = "btn_sc_power",
                        isHapticEnabled = isHapticEnabled
                    )
                }

                // Row 3: x², x!, 1/x, π, e
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CalcButton(
                        text = "x²",
                        type = KeypadButtonType.FUNCTION,
                        onClick = onSquare,
                        modifier = Modifier.weight(1f).aspectRatio(1.6f),
                        testTag = "btn_sc_square",
                        isHapticEnabled = isHapticEnabled
                    )
                    CalcButton(
                        text = "x!",
                        type = KeypadButtonType.FUNCTION,
                        onClick = onFactorial,
                        modifier = Modifier.weight(1f).aspectRatio(1.6f),
                        testTag = "btn_sc_fact",
                        isHapticEnabled = isHapticEnabled
                    )
                    CalcButton(
                        text = "1/x",
                        type = KeypadButtonType.FUNCTION,
                        onClick = onInverse,
                        modifier = Modifier.weight(1f).aspectRatio(1.6f),
                        testTag = "btn_sc_inverse",
                        isHapticEnabled = isHapticEnabled
                    )
                    CalcButton(
                        text = "π",
                        type = KeypadButtonType.FUNCTION,
                        onClick = { onConstant("π") },
                        modifier = Modifier.weight(1f).aspectRatio(1.6f),
                        testTag = "btn_sc_pi",
                        isHapticEnabled = isHapticEnabled
                    )
                    CalcButton(
                        text = "e",
                        type = KeypadButtonType.FUNCTION,
                        onClick = { onConstant("e") },
                        modifier = Modifier.weight(1f).aspectRatio(1.6f),
                        testTag = "btn_sc_e",
                        isHapticEnabled = isHapticEnabled
                    )
                }
            }
        }
    }
}
