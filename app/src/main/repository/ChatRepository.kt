package com.edgeai.chat.repository

import android.graphics.Bitmap
import com.edgeai.chat.database.ChatDao
import com.edgeai.chat.database.ChatMessage
import com.edgeai.chat.database.ChatSession
import com.edgeai.chat.llama.LlamaModel
import com.edgeai.chat.settings.AppSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ChatRepository(
    private val chatDao: ChatDao,
    private val llamaModel: LlamaModel
) {
    // Current load information
    var loadedModelPath: String = ""
        private set
    var loadedMmprojPath: String = ""
        private set
    var loadTimeMs: Long = 0L
        private set
    var modelInfo: String = "No Model Loaded"
        private set

    fun getSessions(): Flow<List<ChatSession>> = chatDao.getAllSessions()

    fun getMessages(sessionId: Long): Flow<List<ChatMessage>> = chatDao.getMessagesForSession(sessionId)

    suspend fun createSession(title: String): Long = withContext(Dispatchers.IO) {
        chatDao.insertSession(ChatSession(title = title))
    }

    suspend fun updateSessionTitle(sessionId: Long, newTitle: String) = withContext(Dispatchers.IO) {
        chatDao.insertSession(ChatSession(id = sessionId, title = newTitle))
    }

    suspend fun deleteSession(sessionId: Long) = withContext(Dispatchers.IO) {
        chatDao.deleteSession(ChatSession(id = sessionId, title = ""))
    }

    suspend fun addMessage(
        sessionId: Long,
        role: String,
        content: String,
        imagePath: String? = null,
        tokensPerSecond: Double = 0.0,
        generationTimeMs: Long = 0L
    ): Long = withContext(Dispatchers.IO) {
        chatDao.insertMessage(
            ChatMessage(
                sessionId = sessionId,
                role = role,
                content = content,
                imagePath = imagePath,
                tokensPerSecond = tokensPerSecond,
                generationTimeMs = generationTimeMs
            )
        )
    }

    suspend fun deleteMessagesForSession(sessionId: Long) = withContext(Dispatchers.IO) {
        chatDao.deleteMessagesForSession(sessionId)
    }

    fun isModelLoaded(): Boolean = llamaModel.isLoaded()

    fun stopGeneration() {
        llamaModel.stop()
    }

    suspend fun loadModel(
        modelPath: String,
        mmprojPath: String,
        settings: AppSettings
    ): Boolean = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val success = llamaModel.load(modelPath, mmprojPath, settings.gpuLayers)
        if (success) {
            val ctxSuccess = llamaModel.initializeContext(
                nCtx = settings.contextLength,
                nThreads = settings.threadsCount,
                temp = settings.temperature,
                topP = settings.topP,
                topK = settings.topK,
                repeatPenalty = settings.repeatPenalty
            )
            if (ctxSuccess) {
                loadedModelPath = modelPath
                loadedMmprojPath = mmprojPath
                loadTimeMs = System.currentTimeMillis() - startTime
                modelInfo = llamaModel.getModelInformation()
                return@withContext true
            }
        }
        return@withContext false
    }

    fun getSystemInfo(): String {
        return llamaModel.getSystemInformation()
    }

    suspend fun generateResponse(
        sessionId: Long,
        prompt: String,
        imageBitmap: Bitmap?,
        settings: AppSettings,
        onToken: suspend (String) -> Unit
    ): ChatMessage = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        var totalTokens = 0
        val fullResponse = StringBuilder()

        llamaModel.generate(prompt, imageBitmap).collect { token ->
            totalTokens++
            fullResponse.append(token)
            onToken(token)
        }

        val endTime = System.currentTimeMillis()
        val generationTimeMs = endTime - startTime
        val tokensPerSec = if (generationTimeMs > 0 && totalTokens > 0) {
            (totalTokens.toDouble() / (generationTimeMs.toDouble() / 1000.0))
        } else {
            0.0
        }

        val responseMessage = ChatMessage(
            sessionId = sessionId,
            role = "assistant",
            content = fullResponse.toString(),
            tokensPerSecond = tokensPerSec,
            generationTimeMs = generationTimeMs
        )
        
        chatDao.insertMessage(responseMessage)
        return@withContext responseMessage
    }
}
