package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.viewmodel.DiscoveryState
import com.example.viewmodel.PeerDevice
import com.example.viewmodel.QuickShareViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendScreen(
    viewModel: QuickShareViewModel,
    onBack: () -> Unit,
    onNavigateToTransfer: (PeerDevice, String, Long) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Videos", "Audio", "Images", "Files")
    
    var selectedFileName by remember { mutableStateOf("") }
    var selectedFileSize by remember { mutableLongStateOf(0L) }
    var showDevicePicker by remember { mutableStateOf(false) }

    val discoveryState by viewModel.discoveryState.collectAsState()
    val nearbyDevices by viewModel.nearbyDevices.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Files") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.background
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(10) { index ->
                        val fileName = "${tabs[selectedTab].dropLast(1)}_${index + 1}${getExtension(selectedTab)}"
                        val size = (10..500).random().toLong()
                        FileItem(
                            fileName = fileName,
                            sizeMb = size,
                            icon = getIconForTab(selectedTab),
                            onClick = {
                                selectedFileName = fileName
                                selectedFileSize = size
                                showDevicePicker = true
                                viewModel.startDiscovery()
                            }
                        )
                    }
                }
            }
        }
    }

    if (showDevicePicker) {
        ModalBottomSheet(
            onDismissRequest = { 
                showDevicePicker = false
                viewModel.stopDiscovery()
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    "Select Device to Send",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Sending: $selectedFileName ($selectedFileSize MB)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                if (discoveryState == DiscoveryState.SEARCHING) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                } else if (nearbyDevices.isNotEmpty()) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(nearbyDevices.size) { index ->
                            val device = nearbyDevices[index]
                            DeviceItem(
                                device = device,
                                onClick = {
                                    showDevicePicker = false
                                    viewModel.stopDiscovery()
                                    onNavigateToTransfer(device, selectedFileName, selectedFileSize)
                                }
                            )
                        }
                    }
                } else {
                    Text("No devices found.", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun FileItem(fileName: String, sizeMb: Long, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(fileName, fontWeight = FontWeight.Medium, maxLines = 1)
            Text("$sizeMb MB", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

fun getExtension(tabIndex: Int): String {
    return when(tabIndex) {
        0 -> ".mp4"
        1 -> ".mp3"
        2 -> ".jpg"
        else -> ".pdf"
    }
}

fun getIconForTab(tabIndex: Int): ImageVector {
    return when(tabIndex) {
        0 -> Icons.Default.VideoFile
        1 -> Icons.Default.Audiotrack
        2 -> Icons.Default.Photo
        else -> Icons.Default.InsertDriveFile
    }
}
