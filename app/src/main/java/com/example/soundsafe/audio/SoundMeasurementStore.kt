package com.example.soundsafe.audio

object SoundMeasurementStore {

    private val lock = Any()
    private val measurements = ArrayList<SoundMeasurement>()
    private const val MAX_MEASUREMENTS = 1440 // keep up to ~24 hours of one-minute samples

    fun addMeasurement(decibels: Double) {
        synchronized(lock) {
            measurements.add(
                SoundMeasurement(
                    timestamp = System.currentTimeMillis(),
                    decibels = decibels
                )
            )

            if (measurements.size > MAX_MEASUREMENTS) {
                // remove oldest entries to bound memory
                val toRemove = measurements.size - MAX_MEASUREMENTS
                for (i in 0 until toRemove) {
                    measurements.removeAt(0)
                }
            }
        }
    }

    fun getMeasurements(): List<SoundMeasurement> {
        synchronized(lock) {
            return ArrayList(measurements)
        }
    }
}
