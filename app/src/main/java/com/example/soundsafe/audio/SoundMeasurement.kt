package com.example.soundsafe.audio

data class SoundMeasurement(
    val timestamp: Long,
    val rawDecibels: Double,
    val smoothedDecibels: Double,
    val environment: SoundEnvironment
)
