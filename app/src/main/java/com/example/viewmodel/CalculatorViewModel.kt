package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.CalculationEntity
import com.example.model.CalculatorEngine
import com.example.model.ConversionUnit
import com.example.model.EvaluationResult
import com.example.model.TipCalculation
import com.example.model.UnitCategory
import com.example.model.UnitConverter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppMode {
    CALCULATOR,
    CONVERTER,
    TIP
}

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.calculationDao()

    // Mode
    private val _currentMode = MutableStateFlow(AppMode.CALCULATOR)
    val currentMode: StateFlow<AppMode> = _currentMode.asStateFlow()

    // Calculator State
    private val _expression = MutableStateFlow("")
    val expression: StateFlow<String> = _expression.asStateFlow()

    private val _previewResult = MutableStateFlow<String?>(null)
    val previewResult: StateFlow<String?> = _previewResult.asStateFlow()

    private val _isEvaluated = MutableStateFlow(false)
    val isEvaluated: StateFlow<Boolean> = _isEvaluated.asStateFlow()

    private val _isRadian = MutableStateFlow(true)
    val isRadian: StateFlow<Boolean> = _isRadian.asStateFlow()

    private val _isScientificExpanded = MutableStateFlow(false)
    val isScientificExpanded: StateFlow<Boolean> = _isScientificExpanded.asStateFlow()

    private val _memoryValue = MutableStateFlow<Double?>(null)
    val memoryValue: StateFlow<Double?> = _memoryValue.asStateFlow()

    private val _isHapticEnabled = MutableStateFlow(true)
    val isHapticEnabled: StateFlow<Boolean> = _isHapticEnabled.asStateFlow()

    private val _isSoundEnabled = MutableStateFlow(false)
    val isSoundEnabled: StateFlow<Boolean> = _isSoundEnabled.asStateFlow()

    // History
    private val _filterFavoritesOnly = MutableStateFlow(false)
    val filterFavoritesOnly: StateFlow<Boolean> = _filterFavoritesOnly.asStateFlow()

    val historyList: StateFlow<List<CalculationEntity>> = combine(
        dao.getAllHistory(),
        _filterFavoritesOnly
    ) { list, favOnly ->
        if (favOnly) list.filter { it.isFavorite } else list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Unit Converter State
    private val _converterCategory = MutableStateFlow(UnitCategory.LENGTH)
    val converterCategory: StateFlow<UnitCategory> = _converterCategory.asStateFlow()

    private val _availableUnits = MutableStateFlow(UnitConverter.getUnitsForCategory(UnitCategory.LENGTH))
    val availableUnits: StateFlow<List<ConversionUnit>> = _availableUnits.asStateFlow()

    private val _fromUnit = MutableStateFlow(_availableUnits.value[0])
    val fromUnit: StateFlow<ConversionUnit> = _fromUnit.asStateFlow()

    private val _toUnit = MutableStateFlow(_availableUnits.value[1])
    val toUnit: StateFlow<ConversionUnit> = _toUnit.asStateFlow()

    private val _converterInput = MutableStateFlow("1")
    val converterInput: StateFlow<String> = _converterInput.asStateFlow()

    private val _converterResult = MutableStateFlow("1")
    val converterResult: StateFlow<String> = _converterResult.asStateFlow()

    // Tip Calculator State
    private val _tipBillInput = MutableStateFlow("50.00")
    val tipBillInput: StateFlow<String> = _tipBillInput.asStateFlow()

    private val _tipPercent = MutableStateFlow(15.0)
    val tipPercent: StateFlow<Double> = _tipPercent.asStateFlow()

    private val _splitCount = MutableStateFlow(2)
    val splitCount: StateFlow<Int> = _splitCount.asStateFlow()

    private val _roundUpTotal = MutableStateFlow(false)
    val roundUpTotal: StateFlow<Boolean> = _roundUpTotal.asStateFlow()

    val tipCalculation: StateFlow<TipCalculation> = combine(
        _tipBillInput,
        _tipPercent,
        _splitCount,
        _roundUpTotal
    ) { billStr, pct, split, roundUp ->
        val bill = billStr.toDoubleOrNull() ?: 0.0
        TipCalculation(
            billAmount = bill,
            tipPercentage = pct,
            splitCount = split.coerceAtLeast(1),
            roundUpTotal = roundUp
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        TipCalculation(50.0, 15.0, 2, false)
    )

    init {
        updateConverterCalculation()
    }

    fun setMode(mode: AppMode) {
        _currentMode.value = mode
    }

    fun toggleScientific() {
        _isScientificExpanded.value = !_isScientificExpanded.value
    }

    fun toggleRadian() {
        _isRadian.value = !_isRadian.value
        updatePreview()
    }

    fun toggleHaptic() {
        _isHapticEnabled.value = !_isHapticEnabled.value
    }

    fun toggleSound() {
        _isSoundEnabled.value = !_isSoundEnabled.value
    }

    // --- Calculator Actions ---

    fun onDigit(digit: String) {
        if (_isEvaluated.value) {
            _expression.value = digit
            _isEvaluated.value = false
        } else {
            // Avoid multiple leading zeros like 00
            if (_expression.value == "0" && digit != ".") {
                _expression.value = digit
            } else {
                _expression.value += digit
            }
        }
        updatePreview()
    }

    fun onDecimal() {
        if (_isEvaluated.value) {
            _expression.value = "0."
            _isEvaluated.value = false
        } else {
            val curr = _expression.value
            if (curr.isEmpty() || isOperator(curr.last()) || curr.last() == '(') {
                _expression.value += "0."
            } else {
                // Find last token to see if it already has a decimal
                val lastToken = curr.takeLastWhile { !isOperator(it) && it != '(' && it != ')' }
                if (!lastToken.contains('.')) {
                    _expression.value += "."
                }
            }
        }
        updatePreview()
    }

    fun onOperator(op: String) {
        val curr = _expression.value
        _isEvaluated.value = false

        if (curr.isEmpty()) {
            if (op == "−" || op == "-") {
                _expression.value = "−"
            }
            return
        }

        val lastChar = curr.last()
        if (isOperator(lastChar)) {
            // Replace last operator
            _expression.value = curr.dropLast(1) + op
        } else {
            _expression.value += op
        }
        updatePreview()
    }

    fun onParenthesis() {
        val curr = _expression.value
        if (_isEvaluated.value) {
            _expression.value = "("
            _isEvaluated.value = false
            updatePreview()
            return
        }

        val openCount = curr.count { it == '(' }
        val closeCount = curr.count { it == ')' }

        if (curr.isEmpty() || isOperator(curr.last()) || curr.last() == '(') {
            _expression.value += "("
        } else if (openCount > closeCount && (curr.last().isDigit() || curr.last() == ')' || curr.last() == 'π' || curr.last() == 'e')) {
            _expression.value += ")"
        } else {
            _expression.value += "×("
        }
        updatePreview()
    }

    fun onFunction(func: String) {
        if (_isEvaluated.value) {
            _expression.value = "$func("
            _isEvaluated.value = false
        } else {
            val curr = _expression.value
            if (curr.isNotEmpty() && (curr.last().isDigit() || curr.last() == ')' || curr.last() == 'π' || curr.last() == 'e')) {
                _expression.value += "×$func("
            } else {
                _expression.value += "$func("
            }
        }
        updatePreview()
    }

    fun onConstant(constant: String) {
        if (_isEvaluated.value) {
            _expression.value = constant
            _isEvaluated.value = false
        } else {
            val curr = _expression.value
            if (curr.isNotEmpty() && (curr.last().isDigit() || curr.last() == ')' || curr.last() == 'π' || curr.last() == 'e')) {
                _expression.value += "×$constant"
            } else {
                _expression.value += constant
            }
        }
        updatePreview()
    }

    fun onFactorial() {
        val curr = _expression.value
        if (curr.isNotEmpty() && (curr.last().isDigit() || curr.last() == ')')) {
            _expression.value += "!"
            updatePreview()
        }
    }

    fun onPercentage() {
        val curr = _expression.value
        if (curr.isNotEmpty() && (curr.last().isDigit() || curr.last() == ')')) {
            _expression.value += "%"
            updatePreview()
        }
    }

    fun onSquare() {
        val curr = _expression.value
        if (curr.isNotEmpty() && (curr.last().isDigit() || curr.last() == ')')) {
            _expression.value += "^2"
            updatePreview()
        }
    }

    fun onPower() {
        val curr = _expression.value
        if (curr.isNotEmpty() && (curr.last().isDigit() || curr.last() == ')')) {
            _expression.value += "^"
            updatePreview()
        }
    }

    fun onInverse() {
        // 1/x
        val curr = _expression.value
        if (curr.isNotEmpty()) {
            _expression.value = "1/($curr)"
            _isEvaluated.value = false
            updatePreview()
        }
    }

    fun onNegate() {
        val curr = _expression.value
        if (curr.isEmpty()) {
            _expression.value = "−"
            return
        }

        // If simple number, toggle sign
        val num = curr.toDoubleOrNull()
        if (num != null) {
            val negated = -num
            _expression.value = CalculatorEngine.formatNumber(negated).replace(",", "")
            updatePreview()
            return
        }

        // Otherwise wrap in - ( ... )
        if (curr.startsWith("−(") && curr.endsWith(")")) {
            _expression.value = curr.substring(2, curr.length - 1)
        } else {
            _expression.value = "−($curr)"
        }
        updatePreview()
    }

    fun onBackspace() {
        val curr = _expression.value
        if (curr.isNotEmpty()) {
            // Check for multicharacter function prefixes like sin(, cos(, sqrt(, etc.
            val funcs = listOf("sqrt(", "cbrt(", "asin(", "acos(", "atan(", "sin(", "cos(", "tan(", "ln(", "log(")
            val matchedFunc = funcs.find { curr.endsWith(it) }

            if (matchedFunc != null) {
                _expression.value = curr.dropLast(matchedFunc.length)
            } else {
                _expression.value = curr.dropLast(1)
            }
            _isEvaluated.value = false
            updatePreview()
        }
    }

    fun onClear() {
        _expression.value = ""
        _previewResult.value = null
        _isEvaluated.value = false
    }

    fun onEquals() {
        val expr = _expression.value.trim()
        if (expr.isEmpty()) return

        when (val res = CalculatorEngine.evaluate(expr, _isRadian.value)) {
            is EvaluationResult.Success -> {
                val formatted = res.formatted
                _previewResult.value = null
                _expression.value = formatted.replace(",", "")
                _isEvaluated.value = true

                // Save to history
                viewModelScope.launch {
                    dao.insert(
                        CalculationEntity(
                            expression = expr,
                            result = formatted
                        )
                    )
                }
            }
            is EvaluationResult.Error -> {
                _previewResult.value = res.message
            }
            EvaluationResult.Empty -> Unit
        }
    }

    private fun updatePreview() {
        val expr = _expression.value
        if (expr.isBlank()) {
            _previewResult.value = null
            return
        }
        _previewResult.value = CalculatorEngine.evaluatePreview(expr, _isRadian.value)
    }

    private fun isOperator(c: Char): Boolean {
        return c == '+' || c == '−' || c == '-' || c == '×' || c == '*' || c == '÷' || c == '/' || c == '^' || c == '%'
    }

    // --- Memory Operations ---

    fun onMemoryClear() {
        _memoryValue.value = null
    }

    fun onMemoryRecall() {
        val mem = _memoryValue.value ?: return
        val memStr = CalculatorEngine.formatNumber(mem).replace(",", "")
        if (_isEvaluated.value) {
            _expression.value = memStr
            _isEvaluated.value = false
        } else {
            val curr = _expression.value
            if (curr.isNotEmpty() && (curr.last().isDigit() || curr.last() == ')')) {
                _expression.value += "×$memStr"
            } else {
                _expression.value += memStr
            }
        }
        updatePreview()
    }

    fun onMemoryAdd() {
        val currVal = getCurrentEvaluationValue() ?: return
        _memoryValue.value = (_memoryValue.value ?: 0.0) + currVal
    }

    fun onMemorySubtract() {
        val currVal = getCurrentEvaluationValue() ?: return
        _memoryValue.value = (_memoryValue.value ?: 0.0) - currVal
    }

    fun onMemoryStore() {
        val currVal = getCurrentEvaluationValue() ?: return
        _memoryValue.value = currVal
    }

    private fun getCurrentEvaluationValue(): Double? {
        val expr = _expression.value
        if (expr.isBlank()) return null
        return when (val res = CalculatorEngine.evaluate(expr, _isRadian.value)) {
            is EvaluationResult.Success -> res.value
            else -> null
        }
    }

    // --- History Actions ---

    fun toggleFavoritesOnly() {
        _filterFavoritesOnly.value = !_filterFavoritesOnly.value
    }

    fun toggleHistoryFavorite(id: Long) {
        viewModelScope.launch {
            dao.toggleFavorite(id)
        }
    }

    fun updateHistoryNote(entity: CalculationEntity, note: String) {
        viewModelScope.launch {
            dao.update(entity.copy(note = note))
        }
    }

    fun deleteHistoryItem(entity: CalculationEntity) {
        viewModelScope.launch {
            dao.delete(entity)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            dao.clearAll()
        }
    }

    fun restoreCalculation(entity: CalculationEntity, restoreAsResult: Boolean = false) {
        if (restoreAsResult) {
            _expression.value = entity.result.replace(",", "")
            _isEvaluated.value = true
            _previewResult.value = null
        } else {
            _expression.value = entity.expression
            _isEvaluated.value = false
            updatePreview()
        }
    }

    // --- Unit Converter Actions ---

    fun setConverterCategory(cat: UnitCategory) {
        _converterCategory.value = cat
        val units = UnitConverter.getUnitsForCategory(cat)
        _availableUnits.value = units
        _fromUnit.value = units[0]
        _toUnit.value = if (units.size > 1) units[1] else units[0]
        updateConverterCalculation()
    }

    fun setFromUnit(unit: ConversionUnit) {
        _fromUnit.value = unit
        updateConverterCalculation()
    }

    fun setToUnit(unit: ConversionUnit) {
        _toUnit.value = unit
        updateConverterCalculation()
    }

    fun setConverterInput(input: String) {
        _converterInput.value = input
        updateConverterCalculation()
    }

    fun swapUnits() {
        val temp = _fromUnit.value
        _fromUnit.value = _toUnit.value
        _toUnit.value = temp
        updateConverterCalculation()
    }

    private fun updateConverterCalculation() {
        val inputNum = _converterInput.value.toDoubleOrNull()
        if (inputNum != null) {
            val converted = UnitConverter.convert(inputNum, _fromUnit.value, _toUnit.value)
            _converterResult.value = UnitConverter.formatResult(converted)
        } else {
            _converterResult.value = "—"
        }
    }

    // --- Tip Calculator Actions ---

    fun setTipBillInput(input: String) {
        _tipBillInput.value = input
    }

    fun setTipPercent(percent: Double) {
        _tipPercent.value = percent
    }

    fun setSplitCount(count: Int) {
        _splitCount.value = count.coerceAtLeast(1)
    }

    fun toggleRoundUpTotal() {
        _roundUpTotal.value = !_roundUpTotal.value
    }
}
