package com.example.soundsafe.audio

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.annotation.RequiresPermission
import kotlin.concurrent.thread
import kotlin.math.log10
import kotlin.math.sqrt

class DecibelMeter(
    private val sampleDurationSeconds: Int = 2,
    private val sampleIntervalSeconds: Int = 45,
    private val onDecibelChanged: (Double) -> Unit
) {

    private var isRunning = false
    private var monitoringThread: Thread? = null

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

        monitoringThread = thread {

            while (isRunning) {

                sampleAudio()

                try {

                    Thread.sleep(
                        sampleIntervalSeconds * 1000L
                    )

                } catch (_: InterruptedException) {
                    break
                }
            }
        }
    }

    fun stop() {

        isRunning = false

        monitoringThread?.interrupt()
        monitoringThread = null
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private fun sampleAudio() {

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

        } finally {

            try {
                audioRecord.stop()
            } catch (_: Exception) {
            }

            audioRecord.release()
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
