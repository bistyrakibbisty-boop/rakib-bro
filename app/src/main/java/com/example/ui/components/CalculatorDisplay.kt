package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Badge
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MintSecondary

@Composable
fun CalculatorDisplay(
    expression: String,
    previewResult: String?,
    isEvaluated: Boolean,
    isRadian: Boolean,
    hasMemory: Boolean,
    memoryValue: Double?,
    onBackspace: () -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Auto-scroll to end when expression changes
    LaunchedEffect(expression) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("calculator_display_surface"),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End
        ) {
            // Badges row: DEG/RAD, Memory, Scientific Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // DEG/RAD indicator pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = if (isRadian) "RAD" else "DEG",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Memory indicator pill
                    if (hasMemory) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MintSecondary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "M",
                                style = MaterialTheme.typography.labelSmall,
                                color = MintSecondary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Action icons: Copy and Backspace
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (expression.isNotEmpty() || !previewResult.isNullOrEmpty()) {
                        FilledTonalIconButton(
                            onClick = {
                                val textToCopy = if (isEvaluated) expression else (previewResult ?: expression)
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Calculator", textToCopy)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied $textToCopy", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("btn_copy"),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy result",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(4.dp)
                            )
                        }

                        FilledTonalIconButton(
                            onClick = onBackspace,
                            modifier = Modifier.testTag("btn_backspace"),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Backspace,
                                contentDescription = "Backspace",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Primary Expression / Result display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = if (expression.isEmpty()) "0" else expression,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = when {
                            expression.length > 14 -> 32.sp
                            expression.length > 9 -> 42.sp
                            else -> 52.sp
                        },
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    textAlign = TextAlign.End,
                    modifier = Modifier.testTag("calculator_primary_text")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Secondary preview line (shows evaluated result preview)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = !previewResult.isNullOrEmpty() && !isEvaluated,
                    enter = fadeIn() + scaleIn(initialScale = 0.9f),
                    exit = fadeOut() + scaleOut()
                ) {
                    Text(
                        text = "= ${previewResult ?: ""}",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Normal
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End,
                        modifier = Modifier.testTag("calculator_preview_text")
                    )
                }
            }
        }
    }
}
