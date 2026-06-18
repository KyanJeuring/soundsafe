package com.example.soundsafe.audio

enum class SoundEnvironment(val displayName: String) {
    QUIET("Quiet"),
    NORMAL("Normal"),
    LOUD("Loud");

    companion object {
        fun fromDecibels(decibels: Double): SoundEnvironment =
            when {
                decibels < 50.0 -> QUIET
                decibels >= 50.0 && decibels < 75.0 -> NORMAL
                else -> LOUD
            }
    }
}
