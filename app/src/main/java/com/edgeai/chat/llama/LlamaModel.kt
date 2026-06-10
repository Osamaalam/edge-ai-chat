package com.edgeai.chat.llama

import android.graphics.Bitmap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.ByteArrayOutputStream

class LlamaModel {
    companion object {
        init {
            System.loadLibrary("llama-android")
        }
    }

    private var modelPtr: Long = 0
    private var contextPtr: Long = 0

    // Native JNI methods
    private external fun initBackend(): Boolean
    private external fun loadModel(modelPath: String, mmprojPath: String, nGpuLayers: Int): Long
    private external fun freeModel(modelPtr: Long)
    private external fun initContext(modelPtr: Long, nCtx: Int, nThreads: Int, temp: Float, topP: Float, topK: Int, repeatPenalty: Float): Long
    private external fun freeContext(contextPtr: Long)
    private external fun startCompletion(modelPtr: Long, prompt: String, imageBytes: ByteArray?): Boolean
    private external fun nextToken(modelPtr: Long): String?
    private external fun stopCompletion(modelPtr: Long)
    private external fun getSystemInfo(): String
    private external fun getModelInfo(modelPtr: Long): String

    init {
        initBackend()
    }

    fun isLoaded(): Boolean = modelPtr != 0L

    fun load(modelPath: String, mmprojPath: String = "", nGpuLayers: Int = 0): Boolean {
        if (modelPtr != 0L) {
            unload()
        }
        modelPtr = loadModel(modelPath, mmprojPath, nGpuLayers)
        return modelPtr != 0L
    }

    fun initializeContext(nCtx: Int, nThreads: Int, temp: Float, topP: Float, topK: Int, repeatPenalty: Float): Boolean {
        if (modelPtr == 0L) return false
        contextPtr = initContext(modelPtr, nCtx, nThreads, temp, topP, topK, repeatPenalty)
        return contextPtr != 0L
    }

    fun unload() {
        if (modelPtr != 0L) {
            freeModel(modelPtr)
            modelPtr = 0L
            contextPtr = 0L
        }
    }

    fun stop() {
        if (modelPtr != 0L) {
            stopCompletion(modelPtr)
        }
    }

    fun getSystemInformation(): String {
        return getSystemInfo()
    }

    fun getModelInformation(): String {
        if (modelPtr == 0L) return "No Model Loaded"
        return getModelInfo(modelPtr)
    }

    /**
     * Runs generation and streams back the generated tokens.
     */
    fun generate(prompt: String, bitmap: Bitmap? = null): Flow<String> = flow {
        if (modelPtr == 0L || contextPtr == 0L) {
            emit("Error: Model not loaded or context not initialized.")
            return@flow
        }

        val imageBytes = bitmap?.let {
            val stream = ByteArrayOutputStream()
            it.compress(Bitmap.CompressFormat.JPEG, 90, stream)
            stream.toByteArray()
        }

        val started = startCompletion(modelPtr, prompt, imageBytes)
        if (!started) {
            emit("Error: Failed to start generation.")
            return@flow
        }

        while (true) {
            val token = nextToken(modelPtr)
            if (token == null) {
                break
            }
            emit(token)
        }
    }
}
