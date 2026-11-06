package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CalculatorDisplay
import com.example.ui.components.CalculatorKeypad
import com.example.ui.components.HistoryBottomSheet
import com.example.ui.components.ScientificKeypad
import com.example.ui.components.TipCalculatorView
import com.example.ui.components.UnitConverterView
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.MintSecondary
import com.example.viewmodel.AppMode
import com.example.viewmodel.CalculatorViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainCalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val currentMode by viewModel.currentMode.collectAsState()
    val expression by viewModel.expression.collectAsState()
    val previewResult by viewModel.previewResult.collectAsState()
    val isEvaluated by viewModel.isEvaluated.collectAsState()
    val isRadian by viewModel.isRadian.collectAsState()
    val isScientificExpanded by viewModel.isScientificExpanded.collectAsState()
    val memoryValue by viewModel.memoryValue.collectAsState()
    val isHapticEnabled by viewModel.isHapticEnabled.collectAsState()
    val historyList by viewModel.historyList.collectAsState()
    val filterFavoritesOnly by viewModel.filterFavoritesOnly.collectAsState()

    // Unit Converter State
    val converterCategory by viewModel.converterCategory.collectAsState()
    val availableUnits by viewModel.availableUnits.collectAsState()
    val fromUnit by viewModel.fromUnit.collectAsState()
    val toUnit by viewModel.toUnit.collectAsState()
    val converterInput by viewModel.converterInput.collectAsState()
    val converterResult by viewModel.converterResult.collectAsState()

    // Tip Calculator State
    val tipBillInput by viewModel.tipBillInput.collectAsState()
    val tipPercent by viewModel.tipPercent.collectAsState()
    val splitCount by viewModel.splitCount.collectAsState()
    val roundUpTotal by viewModel.roundUpTotal.collectAsState()
    val tipCalculation by viewModel.tipCalculation.collectAsState()

    var showHistorySheet by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    // Back handler: return to CALCULATOR mode if on other tabs
    BackHandler(enabled = currentMode != AppMode.CALCULATOR || showHistorySheet) {
        if (showHistorySheet) {
            showHistorySheet = false
        } else {
            viewModel.setMode(AppMode.CALCULATOR)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            Surface(
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                NavigationBar(
                    modifier = Modifier.navigationBarsPadding(),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    NavigationBarItem(
                        selected = currentMode == AppMode.CALCULATOR,
                        onClick = { viewModel.setMode(AppMode.CALCULATOR) },
                        icon = { Icon(Icons.Default.Calculate, contentDescription = "Calculator") },
                        label = { Text("Calculator") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_calculator")
                    )
                    NavigationBarItem(
                        selected = currentMode == AppMode.CONVERTER,
                        onClick = { viewModel.setMode(AppMode.CONVERTER) },
                        icon = { Icon(Icons.Default.Transform, contentDescription = "Converter") },
                        label = { Text("Converter") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_converter")
                    )
                    NavigationBarItem(
                        selected = currentMode == AppMode.TIP,
                        onClick = { viewModel.setMode(AppMode.TIP) },
                        icon = { Icon(Icons.Default.Paid, contentDescription = "Tip & Split") },
                        label = { Text("Tip & Split") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_tip")
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .statusBarsPadding()
        ) {
            // App Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title and Mode indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = when (currentMode) {
                            AppMode.CALCULATOR -> "Calculator"
                            AppMode.CONVERTER -> "Unit Converter"
                            AppMode.TIP -> "Tip & Bill Split"
                        },
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Top right action buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Scientific Toggle (only in Calculator mode)
                    if (currentMode == AppMode.CALCULATOR) {
                        FilledTonalIconButton(
                            onClick = { viewModel.toggleScientific() },
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("btn_toggle_scientific"),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = if (isScientificExpanded) MintSecondary.copy(alpha = 0.25f)
                                else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isScientificExpanded) MintSecondary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Functions,
                                contentDescription = "Scientific Functions",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // History Icon with count badge
                    BadgedBox(
                        badge = {
                            if (historyList.isNotEmpty()) {
                                Badge(
                                    containerColor = AmberPrimary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ) {
                                    Text("${historyList.size}")
                                }
                            }
                        }
                    ) {
                        IconButton(
                            onClick = { showHistorySheet = true },
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("btn_open_history")
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Calculation History",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    // Overflow Menu
                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("btn_overflow_menu")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (isHapticEnabled) "Haptic Feedback: ON" else "Haptic Feedback: OFF") },
                                onClick = {
                                    viewModel.toggleHaptic()
                                    menuExpanded = false
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Vibration,
                                        contentDescription = null,
                                        tint = if (isHapticEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (isRadian) "Mode: Radians (RAD)" else "Mode: Degrees (DEG)") },
                                onClick = {
                                    viewModel.toggleRadian()
                                    menuExpanded = false
                                }
                            )
                            if (historyList.isNotEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Clear All History") },
                                    onClick = {
                                        viewModel.clearAllHistory()
                                        menuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Main Body Content with smooth fade transition
            AnimatedContent(
                targetState = currentMode,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_mode_transition",
                modifier = Modifier.weight(1f)
            ) { mode ->
                when (mode) {
                    AppMode.CALCULATOR -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Calculator Display
                            CalculatorDisplay(
                                expression = expression,
                                previewResult = previewResult,
                                isEvaluated = isEvaluated,
                                isRadian = isRadian,
                                hasMemory = memoryValue != null,
                                memoryValue = memoryValue,
                                onBackspace = { viewModel.onBackspace() },
                                onClearAll = { viewModel.onClear() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Expandable Scientific Keypad
                            ScientificKeypad(
                                isExpanded = isScientificExpanded,
                                isRadian = isRadian,
                                onToggleRadian = { viewModel.toggleRadian() },
                                onFunction = { viewModel.onFunction(it) },
                                onConstant = { viewModel.onConstant(it) },
                                onFactorial = { viewModel.onFactorial() },
                                onPower = { viewModel.onPower() },
                                onSquare = { viewModel.onSquare() },
                                onInverse = { viewModel.onInverse() },
                                isHapticEnabled = isHapticEnabled
                            )

                            // Main Keypad
                            CalculatorKeypad(
                                onDigit = { viewModel.onDigit(it) },
                                onDecimal = { viewModel.onDecimal() },
                                onOperator = { viewModel.onOperator(it) },
                                onParenthesis = { viewModel.onParenthesis() },
                                onClear = { viewModel.onClear() },
                                onNegate = { viewModel.onNegate() },
                                onPercentage = { viewModel.onPercentage() },
                                onEquals = { viewModel.onEquals() },
                                onMemoryClear = { viewModel.onMemoryClear() },
                                onMemoryRecall = { viewModel.onMemoryRecall() },
                                onMemoryAdd = { viewModel.onMemoryAdd() },
                                onMemorySubtract = { viewModel.onMemorySubtract() },
                                onMemoryStore = { viewModel.onMemoryStore() },
                                hasMemory = memoryValue != null,
                                isHapticEnabled = isHapticEnabled,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                    }
                    AppMode.CONVERTER -> {
                        UnitConverterView(
                            selectedCategory = converterCategory,
                            availableUnits = availableUnits,
                            fromUnit = fromUnit,
                            toUnit = toUnit,
                            inputValue = converterInput,
                            convertedValue = converterResult,
                            onCategorySelected = { viewModel.setConverterCategory(it) },
                            onFromUnitSelected = { viewModel.setFromUnit(it) },
                            onToUnitSelected = { viewModel.setToUnit(it) },
                            onInputValueChanged = { viewModel.setConverterInput(it) },
                            onSwapUnits = { viewModel.swapUnits() },
                            isHapticEnabled = isHapticEnabled
                        )
                    }
                    AppMode.TIP -> {
                        TipCalculatorView(
                            billInput = tipBillInput,
                            tipPercent = tipPercent,
                            splitCount = splitCount,
                            roundUpTotal = roundUpTotal,
                            tipCalculation = tipCalculation,
                            onBillInputChanged = { viewModel.setTipBillInput(it) },
                            onTipPercentChanged = { viewModel.setTipPercent(it) },
                            onSplitCountChanged = { viewModel.setSplitCount(it) },
                            onToggleRoundUp = { viewModel.toggleRoundUpTotal() }
                        )
                    }
                }
            }
        }
    }

    // Calculation History Bottom Sheet
    if (showHistorySheet) {
        HistoryBottomSheet(
            sheetState = sheetState,
            historyList = historyList,
            filterFavoritesOnly = filterFavoritesOnly,
            onToggleFavoritesOnly = { viewModel.toggleFavoritesOnly() },
            onToggleFavorite = { viewModel.toggleHistoryFavorite(it) },
            onUpdateNote = { item, note -> viewModel.updateHistoryNote(item, note) },
            onDelete = { viewModel.deleteHistoryItem(it) },
            onClearAll = { viewModel.clearAllHistory() },
            onRestore = { item, asResult -> viewModel.restoreCalculation(item, asResult) },
            onDismiss = {
                coroutineScope.launch {
                    sheetState.hide()
                    showHistorySheet = false
                }
            }
        )
    }
}
