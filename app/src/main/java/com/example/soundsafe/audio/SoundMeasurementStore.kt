package com.example.soundsafe.audio

object SoundMeasurementStore {

    private val measurements = mutableListOf<SoundMeasurement>()

    fun addMeasurement(decibels: Double) {

        measurements.add(
            SoundMeasurement(
                timestamp = System.currentTimeMillis(),
                decibels = decibels
            )
        )
    }

    fun getMeasurements(): List<SoundMeasurement> {
        return measurements.toList()
    }
}
