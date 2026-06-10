package com.edgeai.chat.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edgeai.chat.settings.AppSettings
import com.edgeai.chat.viewmodel.ChatViewModel
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    viewModel: ChatViewModel,
    onBack: () -> Unit
) {
    val currentSettings = viewModel.appSettings
    val scrollState = rememberScrollState()

    var contextLength by remember { mutableStateOf(currentSettings.contextLength.toFloat()) }
    var temperature by remember { mutableStateOf(currentSettings.temperature) }
    var topP by remember { mutableStateOf(currentSettings.topP) }
    var topK by remember { mutableStateOf(currentSettings.topK.toFloat()) }
    var repeatPenalty by remember { mutableStateOf(currentSettings.repeatPenalty) }
    var maxOutputTokens by remember { mutableStateOf(currentSettings.maxOutputTokens.toFloat()) }
    var gpuLayers by remember { mutableStateOf(currentSettings.gpuLayers.toFloat()) }
    var threadsCount by remember { mutableStateOf(currentSettings.threadsCount.toFloat()) }
    var showThinking by remember { mutableStateOf(currentSettings.showThinking) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inference Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        // Reset to defaults
                        contextLength = 2048f
                        temperature = 0.7f
                        topP = 0.9f
                        topK = 40f
                        repeatPenalty = 1.1f
                        maxOutputTokens = 512f
                        gpuLayers = 0f
                        threadsCount = Runtime.getRuntime().availableProcessors().coerceAtLeast(1).toFloat()
                        showThinking = true
                    }) {
                        Icon(Icons.Default.Restore, contentDescription = "Restore Defaults")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Text(
                text = "Configure local llama.cpp backend parameters. Higher values might increase intelligence but will consume more RAM and slow down generation.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Threads Count
            SettingsSlider(
                title = "Threads Count",
                value = threadsCount,
                onValueChange = { threadsCount = it },
                valueRange = 1f..16f,
                steps = 15,
                displayText = "${threadsCount.toInt()} CPU cores"
            )

            // GPU Layers
            SettingsSlider(
                title = "GPU Offload Layers",
                value = gpuLayers,
                onValueChange = { gpuLayers = it },
                valueRange = 0f..32f,
                steps = 32,
                displayText = if (gpuLayers == 0f) "CPU Only (Safe fallback)" else "${gpuLayers.toInt()} Layers to GPU"
            )

            // Context Length
            SettingsSlider(
                title = "Context Length",
                value = contextLength,
                onValueChange = { contextLength = it },
                valueRange = 512f..8192f,
                steps = 15,
                displayText = "${contextLength.toInt()} tokens"
            )

            // Max Output Tokens
            SettingsSlider(
                title = "Max Output Tokens",
                value = maxOutputTokens,
                onValueChange = { maxOutputTokens = it },
                valueRange = 64f..2048f,
                steps = 31,
                displayText = "${maxOutputTokens.toInt()} max tokens"
            )

            // Temperature
            SettingsSlider(
                title = "Temperature",
                value = temperature,
                onValueChange = { temperature = it },
                valueRange = 0.0f..1.5f,
                steps = 15,
                displayText = String.format("%.2f", temperature)
            )

            // Top P
            SettingsSlider(
                title = "Top P",
                value = topP,
                onValueChange = { topP = it },
                valueRange = 0.0f..1.0f,
                steps = 20,
                displayText = String.format("%.2f", topP)
            )

            // Top K
            SettingsSlider(
                title = "Top K",
                value = topK,
                onValueChange = { topK = it },
                valueRange = 1f..100f,
                steps = 99,
                displayText = "${topK.toInt()}"
            )

            // Repeat Penalty
            SettingsSlider(
                title = "Repetition Penalty",
                value = repeatPenalty,
                onValueChange = { repeatPenalty = it },
                valueRange = 1.0f..1.5f,
                steps = 10,
                displayText = String.format("%.2f", repeatPenalty)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Show Thinking Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Enable AI Thinking",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "If disabled, the model will be forced to answer directly without reasoning, reducing token output but speeding up results.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Switch(
                    checked = showThinking,
                    onCheckedChange = { showThinking = it }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val updated = AppSettings(
                        contextLength = contextLength.roundToInt(),
                        temperature = temperature,
                        topP = topP,
                        topK = topK.roundToInt(),
                        repeatPenalty = repeatPenalty,
                        maxOutputTokens = maxOutputTokens.roundToInt(),
                        gpuLayers = gpuLayers.roundToInt(),
                        threadsCount = threadsCount.roundToInt(),
                        lastModelPath = currentSettings.lastModelPath,
                        lastMmprojPath = currentSettings.lastMmprojPath,
                        showThinking = showThinking
                    )
                    viewModel.updateSettings(updated)
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save Settings")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save & Apply Settings")
            }
        }
    }
}

@Composable
fun SettingsSlider(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    displayText: String
) {
    Column(modifier = Modifier.padding(vertical = 10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = displayText,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
