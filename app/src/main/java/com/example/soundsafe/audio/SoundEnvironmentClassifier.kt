package com.example.soundsafe.audio

class SoundEnvironmentClassifier(
    private val quietEnterDb: Double = QUIET_ENTER_DB,
    private val quietExitDb: Double = QUIET_EXIT_DB,
    private val loudEnterDb: Double = LOUD_ENTER_DB,
    private val loudExitDb: Double = LOUD_EXIT_DB
) {
    private var currentEnvironment: SoundEnvironment? = null

    init {
        require(quietEnterDb <= quietExitDb) {
            "Quiet enter threshold must be below quiet exit threshold"
        }
        require(loudExitDb <= loudEnterDb) {
            "Loud exit threshold must be below loud enter threshold"
        }
        require(quietExitDb < loudExitDb) {
            "Quiet and loud hysteresis ranges must not overlap"
        }
    }

    fun classify(decibels: Double): SoundEnvironment {
        val current =
            currentEnvironment ?: SoundEnvironment.fromDecibels(decibels)

        val next =
            when (current) {
                SoundEnvironment.QUIET -> classifyFromQuiet(decibels)
                SoundEnvironment.NORMAL -> classifyFromNormal(decibels)
                SoundEnvironment.LOUD -> classifyFromLoud(decibels)
            }

        currentEnvironment = next
        return next
    }

    fun reset() {
        currentEnvironment = null
    }

    private fun classifyFromQuiet(decibels: Double): SoundEnvironment =
        when {
            decibels >= loudEnterDb -> SoundEnvironment.LOUD
            decibels >= quietExitDb -> SoundEnvironment.NORMAL
            else -> SoundEnvironment.QUIET
        }

    private fun classifyFromNormal(decibels: Double): SoundEnvironment =
        when {
            decibels < quietEnterDb -> SoundEnvironment.QUIET
            decibels >= loudEnterDb -> SoundEnvironment.LOUD
            else -> SoundEnvironment.NORMAL
        }

    private fun classifyFromLoud(decibels: Double): SoundEnvironment =
        when {
            decibels < quietEnterDb -> SoundEnvironment.QUIET
            decibels < loudExitDb -> SoundEnvironment.NORMAL
            else -> SoundEnvironment.LOUD
        }

    companion object {
        const val QUIET_ENTER_DB = 48.0
        const val QUIET_EXIT_DB = 52.0
        const val LOUD_ENTER_DB = 78.0
        const val LOUD_EXIT_DB = 72.0
    }
}
