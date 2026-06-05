package com.example.soundsafe

import android.Manifest
import android.content.Intent
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

    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            updateUiData()
            handler.postDelayed(this, 2000)
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

        setContent {
            SoundSafeApp(
                currentDbLevel = currentDbLevel,
                soundLog = soundLog,
                isAutoMediaEnabled = isAutoMediaEnabled,
                onAutoMediaToggle = { isAutoMediaEnabled = it },
                isAutoRingtoneEnabled = isAutoRingtoneEnabled,
                onAutoRingtoneToggle = { isAutoRingtoneEnabled = it }
            )
        }

        checkPermissionsAndStartService()
    }

    override fun onResume() {
        super.onResume()
        handler.post(updateRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateRunnable)
    }

    private fun updateUiData() {
        val measurements = SoundMeasurementStore.getMeasurements()
        if (measurements.isNotEmpty()) {
            currentDbLevel = "%.1f".format(measurements.last().decibels)
        }

        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val newLog = measurements.asReversed().map {
            SoundRecord(
                time = timeFormat.format(Date(it.timestamp)),
                dbLevel = "%.1f".format(it.decibels)
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
        if (SoundMonitoringService.isServiceRunning) return

        val intent = Intent(this, SoundMonitoringService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }
}
