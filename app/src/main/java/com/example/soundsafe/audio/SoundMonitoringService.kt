package com.example.soundsafe.audio

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import com.example.soundsafe.R

class SoundMonitoringService : Service() {

    private var decibelMeter: DecibelMeter? = null

    companion object {
        const val ACTION_STOP_RECORDING =
            "com.example.soundsafe.audio.action.STOP_RECORDING"
        const val ACTION_RESUME_RECORDING =
            "com.example.soundsafe.audio.action.RESUME_RECORDING"
        private const val NOTIFICATION_ID = 1

        @Volatile
        var isRecording: Boolean = false
            private set

        @Volatile
        var isServiceRunning: Boolean = false
            private set
    }

    override fun onCreate() {
        super.onCreate()

        isServiceRunning = true

        createNotificationChannel()

        decibelMeter = DecibelMeter(
            context = this,
            sampleDurationSeconds = 2,
            sampleIntervalSeconds = 58,
            onDecibelChanged = { db ->

                SoundMeasurementStore.addMeasurement(db)

                println(
                    "Sound level: %.1f dB SPL".format(db)
                )
            }
        )
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        when (intent?.action) {
            ACTION_STOP_RECORDING -> stopRecording()
            ACTION_RESUME_RECORDING -> resumeRecording()
            else -> resumeRecording()
        }

        return START_STICKY
    }

    override fun onDestroy() {

        decibelMeter?.stop()
        decibelMeter = null
        isRecording = false
        isServiceRunning = false

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotification(): Notification {

        val contentText =
            if (isRecording) {
                "Checking sound level in the background"
            } else {
                "Recording is paused"
            }

        return NotificationCompat.Builder(
            this,
            "sound_monitoring"
        )
            .setContentTitle(
                "SoundSafe is monitoring sound"
            )
            .setContentText(contentText)
            .setSmallIcon(
                R.drawable.ic_launcher_foreground
            )
            .setOngoing(true)
            .build()
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private fun resumeRecording() {

        decibelMeter?.start()
        isRecording = true
        startForeground(
            NOTIFICATION_ID,
            createNotification()
        )
    }

    private fun stopRecording() {

        decibelMeter?.stop()
        isRecording = false
        startForeground(
            NOTIFICATION_ID,
            createNotification()
        )
    }

    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                "sound_monitoring",
                "Sound monitoring",
                NotificationManager.IMPORTANCE_LOW
            )

            val manager =
                getSystemService(
                    NotificationManager::class.java
                )

            manager.createNotificationChannel(channel)
        }
    }
}
