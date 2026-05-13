package com.example.soundsafe.audio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.soundsafe.R

class SoundMonitoringService : Service() {

    private var decibelMeter: DecibelMeter? = null

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        decibelMeter = DecibelMeter(
            sampleDurationSeconds = 2,
            sampleIntervalSeconds = 45
        ) { db ->

            SoundMeasurementStore.addMeasurement(db)

            println(
                "Sound level: %.1f dB SPL".format(db)
            )
        }
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        startForeground(
            1,
            createNotification()
        )

        decibelMeter?.start()

        return START_STICKY
    }

    override fun onDestroy() {

        decibelMeter?.stop()
        decibelMeter = null

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotification(): Notification {

        return NotificationCompat.Builder(
            this,
            "sound_monitoring"
        )
            .setContentTitle(
                "SoundSafe is monitoring sound"
            )
            .setContentText(
                "Checking sound level in the background"
            )
            .setSmallIcon(
                R.drawable.ic_launcher_foreground
            )
            .setOngoing(true)
            .build()
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
