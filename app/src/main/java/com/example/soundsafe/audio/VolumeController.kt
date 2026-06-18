package com.example.soundsafe.audio

import android.content.Context
import android.database.ContentObserver
import android.media.AudioManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log

class VolumeController(private val context: Context, private val onAutoStreamDisabled: (Int) -> Unit) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val prefs = context.getSharedPreferences("soundsafe_prefs", Context.MODE_PRIVATE)

    private var lastProgrammaticChangeTime = 0L
    private val PROGRAMMATIC_CHANGE_WINDOW_MS = 1000L

    var isAutoMediaEnabled: Boolean
        get() = prefs.getBoolean("auto_media_enabled", false)
        set(value) {
            prefs.edit().putBoolean("auto_media_enabled", value).apply()
            Log.d("VolumeController", "Auto-media enabled: $value")
        }

    var isAutoRingEnabled: Boolean
        get() = prefs.getBoolean("auto_ring_enabled", false)
        set(value) {
            prefs.edit().putBoolean("auto_ring_enabled", value).apply()
            Log.d("VolumeController", "Auto-ring enabled: $value")
        }

    private val volumeObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            super.onChange(selfChange, uri)

            val uriString = uri?.toString() ?: ""
            val timeSinceLastChange = System.currentTimeMillis() - lastProgrammaticChangeTime
            val isWithinWindow = timeSinceLastChange < PROGRAMMATIC_CHANGE_WINDOW_MS

            if (uriString.contains("volume") || uriString.contains("ring") || uriString.contains("music")) {
                if (uriString.contains("music")) {
                    if (isAutoMediaEnabled && !isWithinWindow) {
                        Log.d("VolumeController", "Manual Media volume change detected, disabling auto-media")
                        isAutoMediaEnabled = false
                        onAutoStreamDisabled(AudioManager.STREAM_MUSIC)
                    }
                } else if (uriString.contains("ring")) {
                    if (isAutoRingEnabled && !isWithinWindow) {
                        Log.d("VolumeController", "Manual Ringtone volume change detected, disabling auto-ring")
                        isAutoRingEnabled = false
                        onAutoStreamDisabled(AudioManager.STREAM_RING)
                    }
                }
            }
        }
    }

    init {
        context.contentResolver.registerContentObserver(
            Settings.System.CONTENT_URI,
            true,
            volumeObserver
        )
    }

    fun unregister() {
        context.contentResolver.unregisterContentObserver(volumeObserver)
    }

    fun adjustVolume(decibels: Double) {
        // Define mapping range: 40dB -> 20%, 90dB -> 100%
        val minDb = 40.0
        val maxDb = 90.0
        val minVol = 0.2
        val maxVol = 1.0

        // Linear mapping: percentage = minVol + (decibels - minDb) * (maxVol - minVol) / (maxDb - minDb)
        val percentage = when {
            decibels <= minDb -> minVol
            decibels >= maxDb -> maxVol
            else -> minVol + (decibels - minDb) * (maxVol - minVol) / (maxDb - minDb)
        }

        if (isAutoMediaEnabled) {
            Log.d("VolumeController", "Adjusting Media volume for %.1f dB: %.0f%%".format(decibels, percentage * 100))
            setStreamVolume(AudioManager.STREAM_MUSIC, percentage)
        }

        if (isAutoRingEnabled) {
            Log.d("VolumeController", "Adjusting Ringtone volume for %.1f dB: %.0f%%".format(decibels, percentage * 100))
            setStreamVolume(AudioManager.STREAM_RING, percentage)
        }
    }

    private fun setStreamVolume(streamType: Int, percentage: Double) {
        try {
            val maxVolume = audioManager.getStreamMaxVolume(streamType)
            val targetVolume = (maxVolume * percentage).toInt()

            val currentVolume = audioManager.getStreamVolume(streamType)
            if (currentVolume == targetVolume) {
                return
            }

            lastProgrammaticChangeTime = System.currentTimeMillis()
            audioManager.setStreamVolume(streamType, targetVolume, 0)
        } catch (e: SecurityException) {
            Log.e("VolumeController", "SecurityException when setting volume for stream $streamType", e)
        } catch (e: Exception) {
            Log.e("VolumeController", "Error setting volume for stream $streamType", e)
        }
    }
}
