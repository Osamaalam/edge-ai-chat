package com.edgeai.chat.settings

import android.content.Context
import android.content.SharedPreferences

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("edge_ai_chat_settings", Context.MODE_PRIVATE)

    fun getSettings(): AppSettings {
        val defaultThreads = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)
        return AppSettings(
            contextLength = prefs.getInt("context_length", 2048),
            temperature = prefs.getFloat("temperature", 0.7f),
            topP = prefs.getFloat("top_p", 0.9f),
            topK = prefs.getInt("top_k", 40),
            repeatPenalty = prefs.getFloat("repeat_penalty", 1.1f),
            maxOutputTokens = prefs.getInt("max_output_tokens", 512),
            gpuLayers = prefs.getInt("gpu_layers", 0),
            threadsCount = prefs.getInt("threads_count", defaultThreads),
            lastModelPath = prefs.getString("last_model_path", "") ?: "",
            lastMmprojPath = prefs.getString("last_mmproj_path", "") ?: ""
        )
    }

    fun saveSettings(settings: AppSettings) {
        prefs.edit().apply {
            putInt("context_length", settings.contextLength)
            putFloat("temperature", settings.temperature)
            putFloat("top_p", settings.topP)
            putInt("top_k", settings.topK)
            putFloat("repeat_penalty", settings.repeatPenalty)
            putInt("max_output_tokens", settings.maxOutputTokens)
            putInt("gpu_layers", settings.gpuLayers)
            putInt("threads_count", settings.threadsCount)
            putString("last_model_path", settings.lastModelPath)
            putString("last_mmproj_path", settings.lastMmprojPath)
            apply()
        }
    }
}
