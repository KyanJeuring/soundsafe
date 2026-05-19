package com.example.soundsafe.audio

import android.Manifest
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.PowerManager
import android.os.Process
import androidx.annotation.RequiresPermission
import kotlin.math.log10
import kotlin.math.sqrt

class DecibelMeter(
    context: Context,
    private val sampleDurationSeconds: Int = 2,
    private val sampleIntervalSeconds: Int = 58,
    private val onDecibelChanged: (Double) -> Unit,
    private val wakeLockTimeoutMillis: Long = 30L * 60L * 1000L
) {

    private val appContext = context.applicationContext

    private var isRunning = false
    private var monitoringThread: Thread? = null
    private var wakeLock: PowerManager.WakeLock? = null

    // How many audio measurements the microphone takes per second.
    // 44100 Hz is standard CD-quality audio and works well for voice/sound detection.
    private val microphoneSampleRate = 44100

    // Converts the raw microphone value from dBFS into an estimated dB SPL value.
    // This is only an estimate and should be calibrated per device for accuracy.
    private val calibrationOffset = 90.0

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun start() {

        if (isRunning) return

        isRunning = true
        acquireWakeLock()

        monitoringThread = Thread(
            Runnable {
                try {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO)

                val bufferSize = AudioRecord.getMinBufferSize(
                    microphoneSampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                val audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    microphoneSampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize
                )

                val buffer = ShortArray(bufferSize)

                try {

                    audioRecord.startRecording()

                    while (isRunning) {

                        sampleAudio(
                            audioRecord,
                            buffer
                        )

                        try {

                            Thread.sleep(
                                sampleIntervalSeconds * 1000L
                            )

                        } catch (_: InterruptedException) {
                            break
                        }
                    }

                } finally {

                    try {
                        audioRecord.stop()
                    } catch (_: Exception) {
                    }

                    audioRecord.release()
                }

                } finally {
                    releaseWakeLock()
                }
            }
        , "DecibelMeter-Monitor")

        monitoringThread?.apply {
            isDaemon = false
            start()
        }

    }

    fun stop() {

        isRunning = false
        releaseWakeLock()

        monitoringThread?.interrupt()
        monitoringThread = null
    }

    private fun acquireWakeLock() {

        if (wakeLock?.isHeld == true) return

        val powerManager =
            appContext.getSystemService(Context.POWER_SERVICE)
                as PowerManager

        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "SoundSafe:DecibelMeter"
        ).apply {
            setReferenceCounted(false)
            acquire(wakeLockTimeoutMillis)
        }
    }

    private fun releaseWakeLock() {

        wakeLock?.let { lock ->

            if (lock.isHeld) {
                lock.release()
            }
        }

        wakeLock = null
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private fun sampleAudio(
        audioRecord: AudioRecord,
        buffer: ShortArray
    ) {

        val endTime =
            System.currentTimeMillis() +
                    (sampleDurationSeconds * 1000L)

        var totalDb = 0.0
        var samples = 0

        while (
            isRunning &&
            System.currentTimeMillis() < endTime
        ) {

            val read = audioRecord.read(
                buffer,
                0,
                buffer.size
            )

            if (read > 0) {

                totalDb += calculateDecibels(
                    buffer,
                    read
                )

                samples++
            }
        }

        if (samples > 0) {

            val averageDb = totalDb / samples

            onDecibelChanged(averageDb)
        }
    }

    private fun calculateDecibels(
        buffer: ShortArray,
        read: Int
    ): Double {

        var sum = 0.0

        for (i in 0 until read) {
            sum += buffer[i] * buffer[i]
        }

        val rms = sqrt(sum / read)

        if (rms <= 0) {
            return 0.0
        }

        val dbFs =
            20 * log10(rms / Short.MAX_VALUE)

        val estimatedDbSpl =
            dbFs + calibrationOffset

        return estimatedDbSpl.coerceAtLeast(0.0)
    }
}
