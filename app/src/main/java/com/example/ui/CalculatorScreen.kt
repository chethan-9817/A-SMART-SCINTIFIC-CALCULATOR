package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActiveDialog
import com.example.model.CalculatorUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val aiState by viewModel.aiState.collectAsState()
    val history by viewModel.history.collectAsState()

    var isSciExpanded by remember { mutableStateOf(true) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.systemBars,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CalculatorTopBar(
                isStealthActive = uiState.isStealthHudVisible,
                onToggleStealth = { viewModel.toggleStealthHud() },
                onOpenAiAgent = { viewModel.openDialog(ActiveDialog.AI_AGENT) },
                onOpenHtml = { viewModel.openDialog(ActiveDialog.HTML_FORMULAS) },
                onOpenVault = { viewModel.openDialog(ActiveDialog.FORMULA_VAULT) },
                onOpenHistory = { viewModel.openDialog(ActiveDialog.HISTORY) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp)
        ) {
            // Stealth HUD Bar (Quick Covert Peek Mode)
            AnimatedVisibility(
                visible = uiState.isStealthHudVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { viewModel.openDialog(ActiveDialog.AI_AGENT) }
                        .testTag("stealth_hud_bar"),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF131B2A),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF10B981), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (uiState.stealthPeekAnswer.isNotBlank())
                                    "ST-PEEK: ${uiState.stealthPeekAnswer}"
                                else
                                    "ST-PEEK: READY (Tap for AI Solver)",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            text = "0x88",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF475569)
                        )
                    }
                }
            }

            // Display Screen Area
            CalculatorDisplay(
                uiState = uiState,
                onToggleAngleMode = { viewModel.toggleAngleMode() },
                onToggleSecondMode = { viewModel.toggleSecondMode() },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.35f)
            )

            // Scientific Expand / Collapse Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Memory Buttons
                    MemoryButton(text = "MC", enabled = uiState.hasMemory, onClick = { viewModel.memoryClear() })
                    MemoryButton(text = "MR", enabled = uiState.hasMemory, onClick = { viewModel.memoryRecall() })
                    MemoryButton(text = "M+", enabled = true, onClick = { viewModel.memoryAdd() })
                    MemoryButton(text = "M-", enabled = true, onClick = { viewModel.memorySubtract() })
                }

                FilledTonalButton(
                    onClick = { viewModel.openDialog(ActiveDialog.AI_AGENT) },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("ai_agent_chip_button"),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("AI Solver", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Keypad Grid Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.65f)
            ) {
                CalculatorKeypad(
                    uiState = uiState,
                    onInput = { viewModel.onInput(it) },
                    onClear = { viewModel.onClear() },
                    onBackspace = { viewModel.onBackspace() },
                    onEvaluate = { viewModel.onEvaluate() },
                    onToggleAngle = { viewModel.toggleAngleMode() },
                    onToggleSecond = { viewModel.toggleSecondMode() }
                )
            }
        }
    }

    // Modal Sheets based on active state
    when (uiState.activeDialog) {
        ActiveDialog.AI_AGENT -> {
            AiAgentSheet(
                aiState = aiState,
                currentCalculatorExpr = uiState.expression.ifBlank { uiState.displayResult },
                onQueryChange = { viewModel.updateAiQuery(it) },
                onSolve = { viewModel.solveWithAi() },
                onToggleCamouflage = { viewModel.toggleAiCamouflage() },
                onSaveSolution = { viewModel.saveCurrentSolution() },
                onDismiss = { viewModel.closeDialog() },
                onOpenHtmlSheet = { viewModel.openDialog(ActiveDialog.HTML_FORMULAS) }
            )
        }
        ActiveDialog.HTML_FORMULAS -> {
            HtmlFormulaSheetView(
                onDismiss = { viewModel.closeDialog() },
                onFormulaSelected = { viewModel.onInput(it) }
            )
        }
        ActiveDialog.HISTORY -> {
            HistorySheet(
                historyList = history,
                onSelectHistory = { viewModel.useHistoryItem(it) },
                onClearHistory = { viewModel.clearAllHistory() },
                onDismiss = { viewModel.closeDialog() }
            )
        }
        ActiveDialog.FORMULA_VAULT -> {
            FormulaVaultSheet(
                onInsertExpression = { viewModel.onInput(it) },
                onDismiss = { viewModel.closeDialog() }
            )
        }
        ActiveDialog.NONE -> Unit
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalculatorTopBar(
    isStealthActive: Boolean,
    onToggleStealth: () -> Unit,
    onOpenAiAgent: () -> Unit,
    onOpenHtml: () -> Unit,
    onOpenVault: () -> Unit,
    onOpenHistory: () -> Unit
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "OmniCalc",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "AI",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        },
        actions = {
            // Stealth HUD Peek toggle
            IconButton(
                onClick = onToggleStealth,
                modifier = Modifier.testTag("stealth_toggle_button")
            ) {
                Icon(
                    imageVector = if (isStealthActive) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = "Toggle Stealth Peek Mode",
                    tint = if (isStealthActive) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // HTML Formula Sheet Button
            IconButton(
                onClick = onOpenHtml,
                modifier = Modifier.testTag("html_formulas_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = "HTML Formula Sheets",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Formula Library Button
            IconButton(
                onClick = onOpenVault,
                modifier = Modifier.testTag("formula_vault_button")
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = "Formula Library",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // History Button
            IconButton(
                onClick = onOpenHistory,
                modifier = Modifier.testTag("history_button")
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "Calculation History",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
private fun CalculatorDisplay(
    uiState: CalculatorUiState,
    onToggleAngleMode: () -> Unit,
    onToggleSecondMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    LaunchedEffect(uiState.expression) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    Card(
        modifier = modifier.padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Mode Badges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // DEG/RAD Toggle
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onToggleAngleMode() },
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                    ) {
                        Text(
                            text = if (uiState.isDegreeMode) "DEG" else "RAD",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    if (uiState.isSecondMode) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "2nd",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    if (uiState.hasMemory) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Text(
                                text = "M",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }

                // Error message badge if any
                if (uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Expression Text (Horizontally scrollable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = uiState.expression.ifEmpty { "0" },
                    style = MaterialTheme.typography.headlineMedium,
                    fontFamily = FontFamily.Monospace,
                    color = if (uiState.expression.isEmpty()) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                    maxLines = 1
                )
            }

            // Results Row (Live Preview and Main Result)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Live preview result (ghost result as user types)
                Text(
                    text = if (uiState.previewResult.isNotEmpty() && uiState.expression.isNotEmpty())
                        "≈ ${uiState.previewResult}"
                    else "",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )

                // Main Display Result
                Text(
                    text = "= ${uiState.displayResult}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.End,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun MemoryButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
        modifier = Modifier
            .height(28.dp)
            .widthIn(min = 36.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Text(text = text, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun CalculatorKeypad(
    uiState: CalculatorUiState,
    onInput: (String) -> Unit,
    onClear: () -> Unit,
    onBackspace: () -> Unit,
    onEvaluate: () -> Unit,
    onToggleAngle: () -> Unit,
    onToggleSecond: () -> Unit
) {
    val is2nd = uiState.isSecondMode

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Scientific Row 1: 2nd, sin/asin, cos/acos, tan/atan, π
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalcKey(
                text = "2nd",
                type = KeyType.FUNCTION_ACTIVE,
                isToggled = is2nd,
                modifier = Modifier.weight(1f)
            ) { onToggleSecond() }

            CalcKey(
                text = if (is2nd) "asin" else "sin",
                type = KeyType.FUNCTION,
                modifier = Modifier.weight(1f)
            ) { onInput(if (is2nd) "asin(" else "sin(") }

            CalcKey(
                text = if (is2nd) "acos" else "cos",
                type = KeyType.FUNCTION,
                modifier = Modifier.weight(1f)
            ) { onInput(if (is2nd) "acos(" else "cos(") }

            CalcKey(
                text = if (is2nd) "atan" else "tan",
                type = KeyType.FUNCTION,
                modifier = Modifier.weight(1f)
            ) { onInput(if (is2nd) "atan(" else "tan(") }

            CalcKey(
                text = "π",
                type = KeyType.FUNCTION,
                modifier = Modifier.weight(1f)
            ) { onInput("π") }
        }

        // Scientific Row 2: ln/e^x, log/10^x, ^, √/x², ( , )
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalcKey(
                text = if (is2nd) "e^x" else "ln",
                type = KeyType.FUNCTION,
                modifier = Modifier.weight(1f)
            ) { onInput(if (is2nd) "e^(" else "ln(") }

            CalcKey(
                text = if (is2nd) "10^x" else "log",
                type = KeyType.FUNCTION,
                modifier = Modifier.weight(1f)
            ) { onInput(if (is2nd) "10^(" else "log(") }

            CalcKey(
                text = "^",
                type = KeyType.FUNCTION,
                modifier = Modifier.weight(1f)
            ) { onInput("^") }

            CalcKey(
                text = if (is2nd) "x²" else "√",
                type = KeyType.FUNCTION,
                modifier = Modifier.weight(1f)
            ) { onInput(if (is2nd) "^2" else "√(") }

            CalcKey(
                text = "(",
                type = KeyType.FUNCTION,
                modifier = Modifier.weight(1f)
            ) { onInput("(") }

            CalcKey(
                text = ")",
                type = KeyType.FUNCTION,
                modifier = Modifier.weight(1f)
            ) { onInput(")") }
        }

        // Row 3: C, ⌫, %, ÷
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalcKey(
                text = "C",
                type = KeyType.CLEAR,
                modifier = Modifier.weight(1f)
            ) { onClear() }

            CalcKey(
                icon = Icons.AutoMirrored.Filled.Backspace,
                text = "DEL",
                type = KeyType.CLEAR,
                modifier = Modifier.weight(1f)
            ) { onBackspace() }

            CalcKey(
                text = "%",
                type = KeyType.OPERATOR,
                modifier = Modifier.weight(1f)
            ) { onInput("%") }

            CalcKey(
                text = "e",
                type = KeyType.FUNCTION,
                modifier = Modifier.weight(1f)
            ) { onInput("e") }

            CalcKey(
                text = "÷",
                type = KeyType.OPERATOR,
                modifier = Modifier.weight(1f)
            ) { onInput("÷") }
        }

        // Row 4: 7, 8, 9, x!, ×
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalcKey(text = "7", type = KeyType.NUMBER, modifier = Modifier.weight(1f)) { onInput("7") }
            CalcKey(text = "8", type = KeyType.NUMBER, modifier = Modifier.weight(1f)) { onInput("8") }
            CalcKey(text = "9", type = KeyType.NUMBER, modifier = Modifier.weight(1f)) { onInput("9") }
            CalcKey(text = "x!", type = KeyType.FUNCTION, modifier = Modifier.weight(1f)) { onInput("!") }
            CalcKey(text = "×", type = KeyType.OPERATOR, modifier = Modifier.weight(1f)) { onInput("×") }
        }

        // Row 5: 4, 5, 6, 1/x, -
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalcKey(text = "4", type = KeyType.NUMBER, modifier = Modifier.weight(1f)) { onInput("4") }
            CalcKey(text = "5", type = KeyType.NUMBER, modifier = Modifier.weight(1f)) { onInput("5") }
            CalcKey(text = "6", type = KeyType.NUMBER, modifier = Modifier.weight(1f)) { onInput("6") }
            CalcKey(text = "1/x", type = KeyType.FUNCTION, modifier = Modifier.weight(1f)) { onInput("^(-1)") }
            CalcKey(text = "−", type = KeyType.OPERATOR, modifier = Modifier.weight(1f)) { onInput("−") }
        }

        // Row 6: 1, 2, 3, |x|, +
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalcKey(text = "1", type = KeyType.NUMBER, modifier = Modifier.weight(1f)) { onInput("1") }
            CalcKey(text = "2", type = KeyType.NUMBER, modifier = Modifier.weight(1f)) { onInput("2") }
            CalcKey(text = "3", type = KeyType.NUMBER, modifier = Modifier.weight(1f)) { onInput("3") }
            CalcKey(text = "|x|", type = KeyType.FUNCTION, modifier = Modifier.weight(1f)) { onInput("abs(") }
            CalcKey(text = "+", type = KeyType.OPERATOR, modifier = Modifier.weight(1f)) { onInput("+") }
        }

        // Row 7: 0, ., ±, =
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CalcKey(text = "0", type = KeyType.NUMBER, modifier = Modifier.weight(2f)) { onInput("0") }
            CalcKey(text = ".", type = KeyType.NUMBER, modifier = Modifier.weight(1f)) { onInput(".") }
            CalcKey(text = "(−)", type = KeyType.FUNCTION, modifier = Modifier.weight(1f)) { onInput("(-") }
            CalcKey(text = "=", type = KeyType.EQUALS, modifier = Modifier.weight(1f)) { onEvaluate() }
        }
    }
}

enum class KeyType {
    NUMBER,
    OPERATOR,
    FUNCTION,
    FUNCTION_ACTIVE,
    CLEAR,
    EQUALS
}

@Composable
private fun CalcKey(
    text: String,
    type: KeyType,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isToggled: Boolean = false,
    onClick: () -> Unit
) {
    val containerColor = when (type) {
        KeyType.NUMBER -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        KeyType.OPERATOR -> MaterialTheme.colorScheme.primaryContainer
        KeyType.FUNCTION -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        KeyType.FUNCTION_ACTIVE -> if (isToggled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant
        KeyType.CLEAR -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
        KeyType.EQUALS -> MaterialTheme.colorScheme.primary
    }

    val contentColor = when (type) {
        KeyType.NUMBER -> MaterialTheme.colorScheme.onSurface
        KeyType.OPERATOR -> MaterialTheme.colorScheme.onPrimaryContainer
        KeyType.FUNCTION -> MaterialTheme.colorScheme.onSurfaceVariant
        KeyType.FUNCTION_ACTIVE -> if (isToggled) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
        KeyType.CLEAR -> MaterialTheme.colorScheme.onErrorContainer
        KeyType.EQUALS -> MaterialTheme.colorScheme.onPrimary
    }

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxHeight()
            .testTag("key_$text"),
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        contentColor = contentColor
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = text,
                    fontSize = if (text.length > 2) 13.sp else 18.sp,
                    fontWeight = if (type == KeyType.EQUALS || type == KeyType.OPERATOR) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}
