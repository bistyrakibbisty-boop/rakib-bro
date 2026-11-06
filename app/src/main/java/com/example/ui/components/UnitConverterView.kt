package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConversionUnit
import com.example.model.UnitCategory
import com.example.model.UnitConverter

@Composable
fun UnitConverterView(
    selectedCategory: UnitCategory,
    availableUnits: List<ConversionUnit>,
    fromUnit: ConversionUnit,
    toUnit: ConversionUnit,
    inputValue: String,
    convertedValue: String,
    onCategorySelected: (UnitCategory) -> Unit,
    onFromUnitSelected: (ConversionUnit) -> Unit,
    onToUnitSelected: (ConversionUnit) -> Unit,
    onInputValueChanged: (String) -> Unit,
    onSwapUnits: () -> Unit,
    isHapticEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    var fromDropdownExpanded by remember { mutableStateOf(false) }
    var toDropdownExpanded by remember { mutableStateOf(false) }
    var rotationAngle by remember { mutableStateOf(0f) }

    val animatedRotation by animateFloatAsState(
        targetValue = rotationAngle,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "swap_rotation"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            // Category Chips (Horizontal Scroll)
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UnitConverter.categories.forEach { cat ->
                    val isSelected = cat == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategorySelected(cat) },
                        label = { Text(cat.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("chip_category_${cat.name}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // "From" Unit Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "FROM",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )

                        // From Unit Selector
                        Box {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clickable { fromDropdownExpanded = true }
                                    .testTag("btn_from_unit_dropdown")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "${fromUnit.name} (${fromUnit.symbol})",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }

                            DropdownMenu(
                                expanded = fromDropdownExpanded,
                                onDismissRequest = { fromDropdownExpanded = false }
                            ) {
                                availableUnits.forEach { unit ->
                                    DropdownMenuItem(
                                        text = { Text("${unit.name} (${unit.symbol})") },
                                        onClick = {
                                            onFromUnitSelected(unit)
                                            fromDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (inputValue.isEmpty()) "0" else inputValue,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Swap Button Row
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                FilledTonalIconButton(
                    onClick = {
                        rotationAngle += 180f
                        onSwapUnits()
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("btn_swap_units"),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Swap units",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.rotate(animatedRotation)
                    )
                }
            }

            // "To" Unit Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TO",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )

                        // To Unit Selector
                        Box {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clickable { toDropdownExpanded = true }
                                    .testTag("btn_to_unit_dropdown")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "${toUnit.name} (${toUnit.symbol})",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }

                            DropdownMenu(
                                expanded = toDropdownExpanded,
                                onDismissRequest = { toDropdownExpanded = false }
                            ) {
                                availableUnits.forEach { unit ->
                                    DropdownMenuItem(
                                        text = { Text("${unit.name} (${unit.symbol})") },
                                        onClick = {
                                            onToUnitSelected(unit)
                                            toDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = convertedValue,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Converter Quick Number Keypad
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val rows = listOf(
                listOf("7", "8", "9", "AC"),
                listOf("4", "5", "6", "⌫"),
                listOf("1", "2", "3", "±"),
                listOf("00", "0", ".", "")
            )

            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { key ->
                        if (key.isEmpty()) {
                            Spacer(modifier = Modifier.weight(1f))
                        } else {
                            val isFunc = key == "AC" || key == "⌫" || key == "±"
                            CalcButton(
                                text = key,
                                type = if (isFunc) KeypadButtonType.FUNCTION else KeypadButtonType.NUMBER,
                                onClick = {
                                    when (key) {
                                        "AC" -> onInputValueChanged("0")
                                        "⌫" -> {
                                            val current = inputValue
                                            if (current.length <= 1) onInputValueChanged("0")
                                            else onInputValueChanged(current.dropLast(1))
                                        }
                                        "±" -> {
                                            if (inputValue.startsWith("-")) onInputValueChanged(inputValue.drop(1))
                                            else if (inputValue != "0") onInputValueChanged("-$inputValue")
                                        }
                                        "." -> {
                                            if (!inputValue.contains(".")) onInputValueChanged("$inputValue.")
                                        }
                                        "00" -> {
                                            if (inputValue != "0") onInputValueChanged(inputValue + "00")
                                        }
                                        else -> {
                                            if (inputValue == "0") onInputValueChanged(key)
                                            else onInputValueChanged(inputValue + key)
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1.8f),
                                testTag = "btn_conv_$key",
                                isHapticEnabled = isHapticEnabled
                            )
                        }
                    }
                }
            }
        }
    }
}
