package com.example.soundsafe.audio

class SoundSmoother(
    private val alpha: Double = DEFAULT_ALPHA
) {
    private var previousSmoothedDecibels: Double? = null

    init {
        require(alpha in 0.0..1.0) {
            "Alpha must be between 0.0 and 1.0"
        }
    }

    fun smooth(newDecibels: Double): Double {
        val previous = previousSmoothedDecibels
        val smoothed =
            if (previous == null) {
                newDecibels
            } else {
                alpha * newDecibels + (1 - alpha) * previous
            }

        previousSmoothedDecibels = smoothed
        return smoothed
    }

    fun reset() {
        previousSmoothedDecibels = null
    }

    companion object {
        const val DEFAULT_ALPHA = 0.25
    }
}
