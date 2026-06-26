package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.TransferEntity
import com.example.data.TransferRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

enum class DiscoveryState { IDLE, SEARCHING, DEVICES_FOUND }
enum class TransferState { IDLE, TRANSFERRING, PAUSED, COMPLETED }

data class PeerDevice(
    val name: String,
    val address: String,
    val icon: Int = 0 // Mock icon identifier
)

class QuickShareViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TransferRepository
    
    init {
        val transferDao = AppDatabase.getDatabase(application).transferDao()
        repository = TransferRepository(transferDao)
    }

    val transferHistory: StateFlow<List<TransferEntity>> = repository.allTransfers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _discoveryState = MutableStateFlow(DiscoveryState.IDLE)
    val discoveryState = _discoveryState.asStateFlow()

    private val _nearbyDevices = MutableStateFlow<List<PeerDevice>>(emptyList())
    val nearbyDevices = _nearbyDevices.asStateFlow()
    
    private val _transferState = MutableStateFlow(TransferState.IDLE)
    val transferState = _transferState.asStateFlow()
    
    private val _transferProgress = MutableStateFlow(0f)
    val transferProgress = _transferProgress.asStateFlow()

    fun startDiscovery() {
        _discoveryState.value = DiscoveryState.SEARCHING
        _nearbyDevices.value = emptyList()
        
        viewModelScope.launch {
            // Mock discovery delay
            delay(2000)
            _nearbyDevices.value = listOf(
                PeerDevice("Galaxy S23", "192.168.49.2"),
                PeerDevice("Pixel 8", "192.168.49.5"),
                PeerDevice("Desktop PC", "192.168.49.12")
            )
            _discoveryState.value = DiscoveryState.DEVICES_FOUND
        }
    }

    fun stopDiscovery() {
        _discoveryState.value = DiscoveryState.IDLE
        _nearbyDevices.value = emptyList()
    }

    fun connectAndTransfer(device: PeerDevice, isSend: Boolean, fileName: String, fileSizeMb: Long) {
        _transferState.value = TransferState.TRANSFERRING
        _transferProgress.value = 0f
        
        viewModelScope.launch {
            for (i in 1..100) {
                if (_transferState.value == TransferState.PAUSED) {
                    while (_transferState.value == TransferState.PAUSED) {
                        delay(200)
                    }
                }
                if (_transferState.value == TransferState.IDLE) break
                
                delay(30) // Simulate fast transfer
                _transferProgress.value = i / 100f
            }
            
            if (_transferState.value == TransferState.TRANSFERRING) {
                _transferState.value = TransferState.COMPLETED
                _transferProgress.value = 1f
                
                // Add to history
                repository.insert(
                    TransferEntity(
                        fileName = fileName,
                        fileSize = fileSizeMb * 1024 * 1024, // to bytes
                        isSent = isSend
                    )
                )
            }
        }
    }
    
    fun pauseTransfer() {
        if (_transferState.value == TransferState.TRANSFERRING) {
            _transferState.value = TransferState.PAUSED
        }
    }
    
    fun resumeTransfer() {
        if (_transferState.value == TransferState.PAUSED) {
            _transferState.value = TransferState.TRANSFERRING
        }
    }
    
    fun cancelTransfer() {
        _transferState.value = TransferState.IDLE
        _transferProgress.value = 0f
    }
    
    fun resetTransferState() {
        _transferState.value = TransferState.IDLE
        _transferProgress.value = 0f
    }
    
    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}
