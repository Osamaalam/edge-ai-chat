package com.edgeai.chat.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.edgeai.chat.database.AppDatabase
import com.edgeai.chat.database.ChatMessage
import com.edgeai.chat.database.ChatSession
import com.edgeai.chat.llama.LlamaModel
import com.edgeai.chat.repository.ChatRepository
import com.edgeai.chat.settings.AppSettings
import com.edgeai.chat.settings.SettingsRepository
import com.edgeai.chat.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.Job
import java.io.FileOutputStream
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val llamaModel = LlamaModel()
    private val chatRepository = ChatRepository(db.chatDao(), llamaModel)
    private val settingsRepository = SettingsRepository(application)

    // State flows
    private val _sessions = MutableStateFlow<List<ChatSession>>(emptyList())
    val sessions: StateFlow<List<ChatSession>> = _sessions.asStateFlow()

    private val _dbMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    private val _streamingMessage = MutableStateFlow<ChatMessage?>(null)

    val messages: StateFlow<List<ChatMessage>> = combine(_dbMessages, _streamingMessage) { dbList, streaming ->
        if (streaming != null) {
            dbList + streaming
        } else {
            dbList
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentSessionId = MutableStateFlow<Long?>(null)
    val currentSessionId: StateFlow<Long?> = _currentSessionId.asStateFlow()

    private val _availableModels = MutableStateFlow<List<File>>(emptyList())
    val availableModels: StateFlow<List<File>> = _availableModels.asStateFlow()

    private val _availableMmprojs = MutableStateFlow<List<File>>(emptyList())
    val availableMmprojs: StateFlow<List<File>> = _availableMmprojs.asStateFlow()

    // Observable Compose states
    var inputMessage by mutableStateOf("")
    var selectedImageUri by mutableStateOf<Uri?>(null)
    var selectedImageBitmap by mutableStateOf<Bitmap?>(null)
    
    var isGenerating by mutableStateOf(false)
    var isModelLoading by mutableStateOf(false)
    var modelLoadingProgress by mutableStateOf(0.0f)
    var modelLoadingStatus by mutableStateOf("")

    var loadedModelName by mutableStateOf("No Model Loaded")
    var modelStats by mutableStateOf("")
    var systemStats by mutableStateOf("")

    var appSettings by mutableStateOf(AppSettings())
        private set

    init {
        appSettings = settingsRepository.getSettings()
        observeSessions()
        scanForModels()
        updateSystemStats()
        
        // Auto-load last model if available
        if (appSettings.lastModelPath.isNotEmpty() && File(appSettings.lastModelPath).exists()) {
            loadModel(appSettings.lastModelPath, appSettings.lastMmprojPath)
        }
    }

    private fun observeSessions() {
        viewModelScope.launch {
            chatRepository.getSessions().collect {
                _sessions.value = it
                // If there's no active session, create or set the first one
                if (_currentSessionId.value == null && it.isNotEmpty()) {
                    selectSession(it.first().id)
                }
            }
        }
    }

    private var messagesJob: Job? = null

    fun selectSession(sessionId: Long) {
        _currentSessionId.value = sessionId
        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            chatRepository.getMessages(sessionId).collect {
                _dbMessages.value = it
            }
        }
    }

    fun createNewSession() {
        viewModelScope.launch {
            val title = "Chat Session " + (sessions.value.size + 1)
            val newId = chatRepository.createSession(title)
            selectSession(newId)
        }
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            chatRepository.deleteSession(sessionId)
            if (_currentSessionId.value == sessionId) {
                _currentSessionId.value = null
                _dbMessages.value = emptyList()
                if (sessions.value.isNotEmpty()) {
                    selectSession(sessions.value.first().id)
                }
            }
        }
    }

    fun clearCurrentChat() {
        val sessionId = _currentSessionId.value ?: return
        viewModelScope.launch {
            chatRepository.deleteMessagesForSession(sessionId)
        }
    }

    fun scanForModels() {
        viewModelScope.launch(Dispatchers.IO) {
            val models = mutableListOf<File>()
            val mmprojs = mutableListOf<File>()

            // 1. Scan internal files directory
            val internalFiles = getApplication<Application>().filesDir.listFiles()
            internalFiles?.forEach {
                if (it.name.endsWith(".gguf")) {
                    if (it.name.contains("mmproj") || it.name.contains("clip")) {
                        mmprojs.add(it)
                    } else {
                        models.add(it)
                    }
                }
            }

            // 2. Scan app-specific downloads folder (Fully accessible on Android 11+ / API 37 under Scoped Storage)
            val appDownloadDir = getApplication<Application>().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            if (appDownloadDir != null && appDownloadDir.exists()) {
                scanDirRecursive(appDownloadDir, models, mmprojs)
            }

            // 3. Scan secondary storage/SD card app folder (GGUF models)
            val extDir = getApplication<Application>().getExternalFilesDir(null)
            if (extDir != null) {
                scanDirRecursive(extDir, models, mmprojs)
            }

            // 4. Fallback scanning public downloads folder (Usually empty or blocked on Android 11+ without MANAGE_EXTERNAL_STORAGE, but included just in case)
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (downloadDir.exists()) {
                scanDirRecursive(downloadDir, models, mmprojs)
            }

            _availableModels.value = models.sortedBy { it.name }
            _availableMmprojs.value = mmprojs.sortedBy { it.name }
        }
    }

    private fun scanDirRecursive(dir: File, models: MutableList<File>, mmprojs: MutableList<File>) {
        dir.listFiles()?.forEach {
            if (it.isDirectory) {
                // Avoid infinite recursive scanning in hidden/system folders
                if (!it.name.startsWith(".")) {
                    scanDirRecursive(it, models, mmprojs)
                }
            } else if (it.name.endsWith(".gguf")) {
                if (it.name.contains("mmproj") || it.name.contains("clip")) {
                    mmprojs.add(it)
                } else {
                    models.add(it)
                }
            }
        }
    }

    fun importModelFromUri(uri: Uri, fileName: String) {
        viewModelScope.launch {
            isModelLoading = true
            modelLoadingProgress = 0.0f
            modelLoadingStatus = "Importing model file: $fileName..."

            val success = withContext(Dispatchers.IO) {
                try {
                    val context = getApplication<Application>()
                    val contentResolver = context.contentResolver
                    val inputStream = contentResolver.openInputStream(uri) ?: return@withContext false
                    
                    // Get total size of the stream to track progress
                    val assetFileDescriptor = contentResolver.openAssetFileDescriptor(uri, "r")
                    val totalSize = assetFileDescriptor?.length ?: -1L
                    assetFileDescriptor?.close()

                    val targetFile = File(context.filesDir, fileName)
                    val outputStream = FileOutputStream(targetFile)
                    val buffer = ByteArray(1024 * 128) // 128KB buffer for faster copies
                    var bytesRead: Int
                    var totalBytesCopied = 0L

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        totalBytesCopied += bytesRead
                        if (totalSize > 0) {
                            val progress = totalBytesCopied.toFloat() / totalSize
                            withContext(Dispatchers.Main) {
                                modelLoadingProgress = progress
                            }
                        }
                    }

                    outputStream.flush()
                    outputStream.close()
                    inputStream.close()
                    true
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }

            if (success) {
                modelLoadingStatus = "Import successful! Rescanning..."
                scanForModels()
            } else {
                modelLoadingStatus = "Import failed."
            }
            isModelLoading = false
        }
    }

    fun loadModel(modelPath: String, mmprojPath: String = "") {
        viewModelScope.launch {
            isModelLoading = true
            modelLoadingProgress = 0.1f
            modelLoadingStatus = "Initializing model engine..."
            
            val success = chatRepository.loadModel(modelPath, mmprojPath, appSettings)
            
            if (success) {
                loadedModelName = FileUtils.getFileName(modelPath)
                modelStats = chatRepository.modelInfo
                
                // Save loaded path to settings
                appSettings = appSettings.copy(lastModelPath = modelPath, lastMmprojPath = mmprojPath)
                settingsRepository.saveSettings(appSettings)
                
                modelLoadingProgress = 1.0f
                modelLoadingStatus = "Model Loaded Successfully"
            } else {
                val errorDetails = if (appSettings.contextLength > 4096) {
                    " | Error: Context size ${appSettings.contextLength} too large for virtual memory"
                } else if (modelPath.contains("Qwen3.5") && !chatRepository.isModelLoaded()) {
                    " | Error: Unknown model architecture: 'qwen35'. Clean & Rebuild in Android Studio is required to fetch b3800 updates"
                } else {
                    " | Error: Architecture mismatch, unsupported quantization or memory exhaustion."
                }
                modelLoadingStatus = "Failed to Load Model$errorDetails"
            }
            isModelLoading = false
            updateSystemStats()
        }
    }

    fun loadModelFromAssets(assetName: String) {
        viewModelScope.launch {
            isModelLoading = true
            modelLoadingProgress = 0.0f
            modelLoadingStatus = "Copying model file from assets..."

            val localPath = withContext(Dispatchers.IO) {
                FileUtils.copyAssetToInternalStorage(getApplication(), assetName) { progress ->
                    modelLoadingProgress = progress
                }
            }

            if (localPath != null) {
                modelLoadingStatus = "Loading local copy of model..."
                loadModel(localPath)
            } else {
                modelLoadingStatus = "Failed to copy model from assets"
                isModelLoading = false
            }
        }
    }

    fun sendMessage() {
        val prompt = inputMessage.trim()
        if (prompt.isEmpty() && selectedImageBitmap == null) return

        val imgUri = selectedImageUri
        val imgBitmap = selectedImageBitmap

        inputMessage = ""
        selectedImageUri = null
        selectedImageBitmap = null

        viewModelScope.launch {
            var sessionId = _currentSessionId.value
            if (sessionId == null) {
                val title = "Chat Session " + (sessions.value.size + 1)
                sessionId = chatRepository.createSession(title)
                _currentSessionId.value = sessionId
                selectSession(sessionId)
            }

            // Construct full ChatML/Qwen template from chat history
            val history = messages.value
            val promptBuilder = java.lang.StringBuilder()
            
            // Add system prompt if any, or default one
            promptBuilder.append("<|im_start|>system\nYou are a helpful assistant named Eiman.")
            if (!appSettings.showThinking) {
                promptBuilder.append(" Respond directly and concisely to the prompt. Do NOT output thinking steps, reasoning blocks, or any <think> tags. Answer the question immediately.")
            }
            promptBuilder.append("<|im_end|>\n")
            
            // Append past history
            for (msg in history) {
                promptBuilder.append("<|im_start|>").append(msg.role).append("\n").append(msg.content).append("<|im_end|>\n")
            }
            
            // Append current prompt
            promptBuilder.append("<|im_start|>user\n").append(prompt).append("<|im_end|>\n")
            promptBuilder.append("<|im_start|>assistant\n")
            
            val formattedPrompt = promptBuilder.toString()

            // Save user message to database
            val imagePath = imgUri?.toString()
            chatRepository.addMessage(sessionId, "user", prompt, imagePath)

            // Setup temporary streaming message placeholder in memory
            isGenerating = true
            _streamingMessage.value = ChatMessage(
                id = -1, // Use negative ID for temporary memory placeholder
                sessionId = sessionId,
                role = "assistant",
                content = "..."
            )

            val tokenBuffer = StringBuilder()
            
            try {
                val fullResponse = chatRepository.generateResponse(sessionId, formattedPrompt, imgBitmap, appSettings) { token ->
                    tokenBuffer.append(token)
                    // Update streaming message in-memory (0 SQLite transactions!)
                    _streamingMessage.value = ChatMessage(
                        id = -1,
                        sessionId = sessionId,
                        role = "assistant",
                        content = tokenBuffer.toString()
                    )
                }
                
                // Clear the temporary memory placeholder once complete
                _streamingMessage.value = null
                
            } catch (e: Exception) {
                _streamingMessage.value = null
                // Save failure message into SQLite
                chatRepository.addMessage(
                    sessionId = sessionId,
                    role = "assistant",
                    content = "Error during inference: ${e.localizedMessage}"
                )
            } finally {
                isGenerating = false
                _streamingMessage.value = null
                updateSystemStats()
            }
        }
    }

    fun stopGeneration() {
        chatRepository.stopGeneration()
        isGenerating = false
    }

    fun updateSettings(newSettings: AppSettings) {
        appSettings = newSettings
        settingsRepository.saveSettings(newSettings)
        
        // Re-initialize context if model is loaded to apply new sampler options
        if (chatRepository.isModelLoaded()) {
            viewModelScope.launch(Dispatchers.IO) {
                llamaModel.initializeContext(
                    nCtx = newSettings.contextLength,
                    nThreads = newSettings.threadsCount,
                    temp = newSettings.temperature,
                    topP = newSettings.topP,
                    topK = newSettings.topK,
                    repeatPenalty = newSettings.repeatPenalty
                )
            }
        }
    }

    fun updateSystemStats() {
        viewModelScope.launch {
            val ram = FileUtils.getSystemRamInfo(getApplication())
            val storage = FileUtils.getAvailableStorageInfo()
            systemStats = "$ram\n$storage"
        }
    }

    fun setImage(uri: Uri?) {
        selectedImageUri = uri
        if (uri != null) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val inputStream = getApplication<Application>().contentResolver.openInputStream(uri)
                    val originalBitmap = BitmapFactory.decodeStream(inputStream)
                    
                    // Resize to a reasonable dimension (e.g., max 512px) to optimize model processing and prevent OutOfMemory
                    val maxDim = 512
                    val ratio = originalBitmap.width.toFloat() / originalBitmap.height.toFloat()
                    val (w, h) = if (ratio > 1) {
                        Pair(maxDim, (maxDim / ratio).toInt())
                    } else {
                        Pair((maxDim * ratio).toInt(), maxDim)
                    }
                    
                    selectedImageBitmap = Bitmap.createScaledBitmap(originalBitmap, w, h, true)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            selectedImageBitmap = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        llamaModel.unload()
    }
}
