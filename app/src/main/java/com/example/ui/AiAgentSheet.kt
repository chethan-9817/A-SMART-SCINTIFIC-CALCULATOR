package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AiSolverUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAgentSheet(
    aiState: AiSolverUiState,
    currentCalculatorExpr: String,
    onQueryChange: (String) -> Unit,
    onSolve: () -> Unit,
    onToggleCamouflage: () -> Unit,
    onSaveSolution: () -> Unit,
    onDismiss: () -> Unit,
    onOpenHtmlSheet: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var copiedNotice by remember { mutableStateOf(false) }

    val sampleQueries = listOf(
        "Derive ∫ x*cos(x) dx",
        "Roots of 2x² - 7x + 3 = 0",
        "Derivative of sin(3x)*e^(2x)",
        "Kinetic energy of 1200kg at 25m/s"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = if (aiState.isCamouflageMode) Color(0xFF1E2022) else MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (aiState.isCamouflageMode) Icons.Default.Memory else Icons.Default.AutoAwesome,
                        contentDescription = "AI Agent Icon",
                        tint = if (aiState.isCamouflageMode) Color.Gray else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (aiState.isCamouflageMode) "System Register Diagnostic" else "Genius STEM AI Agent",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (aiState.isCamouflageMode) Color.LightGray else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (aiState.isCamouflageMode) "Buffer State 0x88F0" else "Step-by-step solver & instant answers",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row {
                    // Camouflage / Stealth Mode Toggle
                    IconButton(
                        onClick = onToggleCamouflage,
                        modifier = Modifier.testTag("camouflage_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (aiState.isCamouflageMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle Camouflage Mode",
                            tint = if (aiState.isCamouflageMode) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close AI Agent")
                    }
                }
            }

            if (aiState.isCamouflageMode) {
                // Subtle disclaimer for stealth mode
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .background(Color(0xFF2B2D30), RoundedCornerShape(6.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "• Camouflage Active: Low-contrast display mode enabled",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Insert from Calculator Expression
            if (currentCalculatorExpr.isNotBlank()) {
                Surface(
                    onClick = { onQueryChange(currentCalculatorExpr) },
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Calculate,
                            contentDescription = "Use Calculator",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Solve Calculator Expr: $currentCalculatorExpr",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Input TextField
            OutlinedTextField(
                value = aiState.query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_query_input"),
                label = { Text(if (aiState.isCamouflageMode) "Input Query / Equation" else "Enter Problem (Math, Calculus, Physics, Words)") },
                placeholder = { Text("e.g. Find derivative of x*sin(x) or solve 3x + 12 = 0") },
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onSolve() }),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    if (aiState.query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear Input")
                        }
                    }
                }
            )

            // Sample Quick Queries
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                sampleQueries.take(2).forEach { sample ->
                    SuggestionChip(
                        onClick = { onQueryChange(sample) },
                        label = { Text(sample.take(22) + "...", fontSize = 11.sp) }
                    )
                }
            }

            // Solve Button
            Button(
                onClick = onSolve,
                enabled = aiState.query.isNotBlank() && !aiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("solve_ai_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (aiState.isCamouflageMode) Color(0xFF374151) else MaterialTheme.colorScheme.primary
                )
            ) {
                if (aiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Solving Problem with AI...")
                } else {
                    Icon(Icons.Default.Bolt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (aiState.isCamouflageMode) "Compute Buffer Register" else "Solve Step-by-Step",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Error Display
            if (aiState.errorMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = aiState.errorMessage,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Result Display
            aiState.result?.let { result ->
                Spacer(modifier = Modifier.height(16.dp))

                // Direct Quick Answer Card (Prominent for fast testing)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_result_answer_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (aiState.isCamouflageMode) Color(0xFF262A30) else MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⚡ FINAL ANSWER",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (aiState.isCamouflageMode) Color.LightGray else MaterialTheme.colorScheme.primary
                            )
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(result.quickAnswer))
                                    copiedNotice = true
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Copy Final Answer",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Text(
                            text = result.quickAnswer,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (aiState.isCamouflageMode) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        // Stealth Glance Hint
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Discreet Peek: ${result.stealthHint}",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = if (aiState.isCamouflageMode) Color.Gray else MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Step-by-Step Derivation Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (aiState.isCamouflageMode) Color(0xFF1E2024) else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "📝 STEP-BY-STEP WORKING",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (aiState.isCamouflageMode) Color.LightGray else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = result.steps,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp,
                            color = if (aiState.isCamouflageMode) Color(0xFFD1D5DB) else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (result.formulasUsed.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "📐 FORMULAS & THEOREMS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = result.formulasUsed,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Action Buttons under solution
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FilledTonalButton(
                        onClick = onSaveSolution,
                        enabled = !aiState.isSaved
                    ) {
                        Icon(
                            if (aiState.isSaved) Icons.Default.Check else Icons.Default.BookmarkAdd,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (aiState.isSaved) "Saved to Vault" else "Save Solution")
                    }

                    OutlinedButton(onClick = onOpenHtmlSheet) {
                        Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("HTML Cheat View")
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
