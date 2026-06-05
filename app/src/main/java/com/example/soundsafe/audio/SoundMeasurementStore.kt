package com.example.soundsafe.audio

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SoundMeasurementStore {

    private val _measurements = MutableStateFlow<List<SoundMeasurement>>(emptyList())
    val measurements: StateFlow<List<SoundMeasurement>> = _measurements.asStateFlow()
    private const val MAX_MEASUREMENTS = 1440 // keep up to ~24 hours of one-minute samples

    fun addMeasurement(
        rawDecibels: Double,
        smoothedDecibels: Double,
        environment: SoundEnvironment
    ) {
        val newMeasurement = SoundMeasurement(
            timestamp = System.currentTimeMillis(),
            rawDecibels = rawDecibels,
            smoothedDecibels = smoothedDecibels,
            environment = environment
        )

        val currentList = _measurements.value
        val newList = (currentList + newMeasurement).let {
            if (it.size > MAX_MEASUREMENTS) it.drop(it.size - MAX_MEASUREMENTS) else it
        }
        _measurements.value = newList
    }

    fun getMeasurements(): List<SoundMeasurement> {
        return _measurements.value
    }
}
