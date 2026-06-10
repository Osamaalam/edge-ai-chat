package com.edgeai.chat.settings

data class AppSettings(
    val contextLength: Int = 2048,
    val temperature: Float = 0.7f,
    val topP: Float = 0.9f,
    val topK: Int = 40,
    val repeatPenalty: Float = 1.1f,
    val maxOutputTokens: Int = 512,
    val gpuLayers: Int = 0,
    val threadsCount: Int = Runtime.getRuntime().availableProcessors().coerceAtLeast(1),
    val lastModelPath: String = "",
    val lastMmprojPath: String = "",
    val showThinking: Boolean = true
)
