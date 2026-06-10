package com.edgeai.chat.utils

import android.app.ActivityManager
import android.content.Context
import android.os.Environment
import android.os.StatFs
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.DecimalFormat

object FileUtils {

    fun getSystemRamInfo(context: Context): String {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        
        val df = DecimalFormat("#.##")
        val availableGb = memoryInfo.availMem.toDouble() / (1024 * 1024 * 1024)
        val totalGb = memoryInfo.totalMem.toDouble() / (1024 * 1024 * 1024)
        val usedGb = totalGb - availableGb
        
        return "RAM: ${df.format(usedGb)} / ${df.format(totalGb)} GB (Available: ${df.format(availableGb)} GB)"
    }

    fun getAvailableStorageInfo(): String {
        val stat = StatFs(Environment.getDataDirectory().path)
        val availableBytes = stat.availableBytes
        val df = DecimalFormat("#.##")
        val availableGb = availableBytes.toDouble() / (1024 * 1024 * 1024)
        return "Storage: ${df.format(availableGb)} GB free"
    }

    fun getFileName(path: String): String {
        return File(path).name
    }

    fun getFileSizeString(path: String): String {
        val file = File(path)
        if (!file.exists()) return "Unknown size"
        val bytes = file.length()
        val df = DecimalFormat("#.##")
        val kb = bytes.toDouble() / 1024
        val mb = kb / 1024
        val gb = mb / 1024
        return when {
            gb >= 1.0 -> "${df.format(gb)} GB"
            mb >= 1.0 -> "${df.format(mb)} MB"
            else -> "${df.format(kb)} KB"
        }
    }

    fun listAssetGgufFiles(context: Context): List<String> {
        return try {
            context.assets.list("")?.filter { it.endsWith(".gguf") } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun copyAssetToInternalStorage(context: Context, assetName: String, onProgress: (Float) -> Unit): String? {
        val targetFile = File(context.filesDir, assetName)
        
        // If file already exists and has a positive length, we don't need to copy it again.
        if (targetFile.exists() && targetFile.length() > 0) {
            onProgress(1.0f)
            return targetFile.absolutePath
        }

        try {
            val inputStream: InputStream = context.assets.open(assetName)
            val totalSize = inputStream.available().toLong()
            val outputStream = FileOutputStream(targetFile)
            val buffer = ByteArray(1024 * 64) // 64KB buffer for faster copies
            var bytesRead: Int
            var totalBytesCopied = 0L

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalBytesCopied += bytesRead
                onProgress(totalBytesCopied.toFloat() / totalSize)
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()
            return targetFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
