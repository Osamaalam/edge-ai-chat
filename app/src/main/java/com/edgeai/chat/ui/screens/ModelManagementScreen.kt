package com.edgeai.chat.ui.screens

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edgeai.chat.utils.FileUtils
import com.edgeai.chat.viewmodel.ChatViewModel
import java.io.File

@Composable
fun ModelManagementScreen(
    viewModel: ChatViewModel,
    onBack: () -> Unit
) {
    val availableModels by viewModel.availableModels.collectAsState()
    val availableMmprojs by viewModel.availableMmprojs.collectAsState()
    val context = LocalContext.current
    
    var selectedModelFile by remember { mutableStateOf<File?>(
        if (viewModel.appSettings.lastModelPath.isNotEmpty()) File(viewModel.appSettings.lastModelPath) else null
    ) }
    var selectedMmprojFile by remember { mutableStateOf<File?>(
        if (viewModel.appSettings.lastMmprojPath.isNotEmpty()) File(viewModel.appSettings.lastMmprojPath) else null
    ) }

    // File picker contract to browse any file on local storage
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            // Retrieve actual filename from Uri metadata
            var displayName = "model.gguf"
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    displayName = cursor.getString(nameIndex)
                }
            }
            viewModel.importModelFromUri(uri, displayName)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Model Manager") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.scanForModels() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Rescan Files")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Loading Overlay
            if (viewModel.isModelLoading) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = viewModel.modelLoadingStatus,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = viewModel.modelLoadingProgress,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Current Loaded Model Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Current Active Model",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = viewModel.loadedModelName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    if (viewModel.modelStats.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = viewModel.modelStats,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Text(
                text = "Select LLM Model (.gguf)",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // LLM Models List
            if (availableModels.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.FolderOpen, contentDescription = "No models found", modifier = Modifier.size(48.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No models found in storage or internal files.", fontSize = 12.sp, color = Color.Gray)
                        Text("Place your .gguf models in your device's 'Download' directory and press Refresh above.", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.2f)
                        .padding(bottom = 16.dp)
                ) {
                    items(availableModels) { file ->
                        val isSelected = selectedModelFile?.absolutePath == file.absolutePath
                        ModelFileRow(
                            file = file,
                            isSelected = isSelected,
                            onSelect = {
                                selectedModelFile = if (isSelected) null else file
                            }
                        )
                    }
                }
            }

            Text(
                text = "Select Vision Projector Model (.gguf) - Optional",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // mmproj CLIP Models List
            if (availableMmprojs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.6f)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No CLIP models found. Optional for vision support.", fontSize = 11.sp, color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.8f)
                        .padding(bottom = 16.dp)
                ) {
                    items(availableMmprojs) { file ->
                        val isSelected = selectedMmprojFile?.absolutePath == file.absolutePath
                        ModelFileRow(
                            file = file,
                            isSelected = isSelected,
                            onSelect = {
                                selectedMmprojFile = if (isSelected) null else file
                            }
                        )
                    }
                }
            }

            // Row containing Import and Load Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Browse Local Storage Button
                OutlinedButton(
                    onClick = { filePickerLauncher.launch("*/*") },
                    enabled = !viewModel.isModelLoading,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Icon(Icons.Default.FileOpen, contentDescription = "Browse")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Import Model")
                }

                // Action Button to Load Selected Model
                Button(
                    onClick = {
                        selectedModelFile?.let { model ->
                            viewModel.loadModel(model.absolutePath, selectedMmprojFile?.absolutePath ?: "")
                        }
                    },
                    enabled = selectedModelFile != null && !viewModel.isModelLoading,
                    modifier = Modifier
                        .weight(1.2f)
                        .height(50.dp)
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = "Load")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Apply & Load")
                }
            }
        }
    }
}

@Composable
fun ModelFileRow(
    file: File,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val sizeString = FileUtils.getFileSizeString(file.absolutePath)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.InsertDriveFile,
                contentDescription = "File",
                tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Size: $sizeString | Path: .../${file.parentFile?.name}/${file.name}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
