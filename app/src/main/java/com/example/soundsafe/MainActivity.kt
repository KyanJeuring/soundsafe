package com.example.soundsafe

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.example.soundsafe.audio.SoundMeasurementStore
import com.example.soundsafe.audio.SoundMonitoringService
import com.example.soundsafe.ui.compose.SoundRecord
import com.example.soundsafe.ui.compose.SoundSafeApp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    // UI States
    private var selectedTimeFrame by mutableStateOf("Daily")
    private var isDarkModeEnabled by mutableStateOf(false)
    private var isAutoMediaEnabled by mutableStateOf(false)
    private var isAutoRingtoneEnabled by mutableStateOf(false)

    private val autoVolumeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                SoundMonitoringService.ACTION_AUTO_MEDIA_DISABLED -> {
                    isAutoMediaEnabled = false
                }
                SoundMonitoringService.ACTION_AUTO_RING_DISABLED -> {
                    isAutoRingtoneEnabled = false
                }
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.RECORD_AUDIO] == true) {
            startSoundService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load initial state from SharedPreferences
        val prefs = getSharedPreferences("soundsafe_prefs", Context.MODE_PRIVATE)
        isAutoMediaEnabled = prefs.getBoolean("auto_media_enabled", false)
        isAutoRingtoneEnabled = prefs.getBoolean("auto_ring_enabled", false)
        isDarkModeEnabled = prefs.getBoolean("dark_mode_enabled", false)

        setContent {
            val isRecording by SoundMonitoringService.isRecording.collectAsState()
            val measurements by SoundMeasurementStore.measurements.collectAsState()

            val currentDbLevel = remember(measurements) {
                if (measurements.isNotEmpty()) {
                    "%.1f".format(measurements.last().smoothedDecibels)
                } else "0.0"
            }

            val soundLog = remember(measurements) {
                val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                measurements.asReversed().map {
                    SoundRecord(
                        time = timeFormat.format(Date(it.timestamp)),
                        dbLevel = "%.1f".format(it.smoothedDecibels)
                    )
                }
            }

            SoundSafeApp(
                currentDbLevel = currentDbLevel,
                soundLog = soundLog,
                selectedTimeFrame = selectedTimeFrame,
                onTimeFrameSelected = { selectedTimeFrame = it },
                isDarkModeEnabled = isDarkModeEnabled,
                onThemeToggle = {
                    isDarkModeEnabled = it
                    prefs.edit().putBoolean("dark_mode_enabled", it).apply()
                },
                isRecording = isRecording,
                onToggleRecording = { toggleRecording(isRecording) },
                isAutoMediaEnabled = isAutoMediaEnabled,
                onAutoMediaToggle = {
                    isAutoMediaEnabled = it
                    prefs.edit().putBoolean("auto_media_enabled", it).apply()
                },
                isAutoRingtoneEnabled = isAutoRingtoneEnabled,
                onAutoRingtoneToggle = {
                    isAutoRingtoneEnabled = it
                    prefs.edit().putBoolean("auto_ring_enabled", it).apply()
                }
            )
        }

        checkPermissionsAndStartService()
    }

    override fun onResume() {
        super.onResume()

        val filter = IntentFilter().apply {
            addAction(SoundMonitoringService.ACTION_AUTO_MEDIA_DISABLED)
            addAction(SoundMonitoringService.ACTION_AUTO_RING_DISABLED)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(autoVolumeReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(autoVolumeReceiver, filter)
        }

        // Re-sync persistent settings on resume
        val prefs = getSharedPreferences("soundsafe_prefs", Context.MODE_PRIVATE)
        isAutoMediaEnabled = prefs.getBoolean("auto_media_enabled", false)
        isAutoRingtoneEnabled = prefs.getBoolean("auto_ring_enabled", false)
        isDarkModeEnabled = prefs.getBoolean("dark_mode_enabled", false)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(autoVolumeReceiver)
    }

    private fun checkPermissionsAndStartService() {
        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissionsToRequest.isEmpty()) {
            startSoundService()
        } else {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun startSoundService() {
        if (SoundMonitoringService.isServiceRunning.value) {
            return
        }

        val intent = Intent(this, SoundMonitoringService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }

    private fun toggleRecording(isCurrentlyRecording: Boolean) {
        val intent = Intent(this, SoundMonitoringService::class.java)
        if (isCurrentlyRecording) {
            intent.action = SoundMonitoringService.ACTION_STOP_RECORDING
        } else {
            intent.action = SoundMonitoringService.ACTION_RESUME_RECORDING
        }
        startService(intent)
    }
}
