package com.example.soundsafe

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
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
    private var currentDbLevel by mutableStateOf("0.0")
    private val soundLog = mutableStateListOf<SoundRecord>()
    private var isAutoMediaEnabled by mutableStateOf(false)
    private var isAutoRingtoneEnabled by mutableStateOf(false)
    private var isRecording by mutableStateOf(false)

    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            updateUiData()
            handler.postDelayed(this, 2000)
        }
    }

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

        setContent {
            SoundSafeApp(
                currentDbLevel = currentDbLevel,
                soundLog = soundLog,
                isRecording = isRecording,
                onToggleRecording = { toggleRecording() },
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
        handler.post(updateRunnable)

        val filter = IntentFilter().apply {
            addAction(SoundMonitoringService.ACTION_AUTO_MEDIA_DISABLED)
            addAction(SoundMonitoringService.ACTION_AUTO_RING_DISABLED)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(autoVolumeReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(autoVolumeReceiver, filter)
        }

        // Re-sync states on resume in case they changed in background
        val prefs = getSharedPreferences("soundsafe_prefs", Context.MODE_PRIVATE)
        isAutoMediaEnabled = prefs.getBoolean("auto_media_enabled", false)
        isAutoRingtoneEnabled = prefs.getBoolean("auto_ring_enabled", false)
        isRecording = SoundMonitoringService.isRecording
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateRunnable)
        unregisterReceiver(autoVolumeReceiver)
    }

    private fun updateUiData() {
        val measurements = SoundMeasurementStore.getMeasurements()
        if (measurements.isNotEmpty()) {
            currentDbLevel = "%.1f".format(measurements.last().smoothedDecibels)
        }

        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val newLog = measurements.asReversed().map {
            SoundRecord(
                time = timeFormat.format(Date(it.timestamp)),
                dbLevel = "%.1f".format(it.smoothedDecibels)
            )
        }

        // Update list efficiently
        soundLog.clear()
        soundLog.addAll(newLog)
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
        if (SoundMonitoringService.isServiceRunning) {
            isRecording = SoundMonitoringService.isRecording
            return
        }

        val intent = Intent(this, SoundMonitoringService::class.java)
        ContextCompat.startForegroundService(this, intent)
        isRecording = true
    }

    private fun toggleRecording() {
        val intent = Intent(this, SoundMonitoringService::class.java)
        if (isRecording) {
            intent.action = SoundMonitoringService.ACTION_STOP_RECORDING
        } else {
            intent.action = SoundMonitoringService.ACTION_RESUME_RECORDING
        }
        startService(intent)
        isRecording = !isRecording
    }
}
