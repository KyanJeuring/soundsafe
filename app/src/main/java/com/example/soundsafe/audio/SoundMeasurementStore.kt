package com.example.soundsafe.audio

object SoundMeasurementStore {

    private val measurements = mutableListOf<SoundMeasurement>()

    fun addMeasurement(
        rawDecibels: Double,
        smoothedDecibels: Double,
        environment: SoundEnvironment
    ) {

        synchronized(measurements) {
            measurements.add(
                SoundMeasurement(
                    timestamp = System.currentTimeMillis(),
                    rawDecibels = rawDecibels,
                    smoothedDecibels = smoothedDecibels,
                    environment = environment
                )
            )
        }
    }

    fun getMeasurements(): List<SoundMeasurement> {
        return synchronized(measurements) {
            measurements.toList()
        }
    }
}
