package com.example.soundsafe

import com.example.soundsafe.audio.SoundEnvironment
import com.example.soundsafe.audio.SoundEnvironmentClassifier
import com.example.soundsafe.audio.SoundSmoother
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun smoother_reducesSuddenSpikes() {
        val smoother = SoundSmoother(alpha = 0.25)

        assertEquals(50.0, smoother.smooth(50.0), 0.001)
        assertEquals(60.0, smoother.smooth(90.0), 0.001)
    }

    @Test
    fun classification_usesSmoothedThresholds() {
        assertEquals(SoundEnvironment.QUIET, SoundEnvironment.fromDecibels(45.0))
        assertEquals(SoundEnvironment.NORMAL, SoundEnvironment.fromDecibels(60.0))
        assertEquals(SoundEnvironment.LOUD, SoundEnvironment.fromDecibels(80.0))
    }

    @Test
    fun classifier_doesNotBounceAroundLoudThreshold() {
        val classifier = SoundEnvironmentClassifier()

        assertEquals(SoundEnvironment.NORMAL, classifier.classify(70.0))
        assertEquals(SoundEnvironment.NORMAL, classifier.classify(76.0))
        assertEquals(SoundEnvironment.LOUD, classifier.classify(78.0))
        assertEquals(SoundEnvironment.LOUD, classifier.classify(74.0))
        assertEquals(SoundEnvironment.NORMAL, classifier.classify(71.0))
    }

    @Test
    fun classifier_doesNotBounceAroundQuietThreshold() {
        val classifier = SoundEnvironmentClassifier()

        assertEquals(SoundEnvironment.QUIET, classifier.classify(45.0))
        assertEquals(SoundEnvironment.QUIET, classifier.classify(50.0))
        assertEquals(SoundEnvironment.NORMAL, classifier.classify(52.0))
        assertEquals(SoundEnvironment.NORMAL, classifier.classify(49.0))
        assertEquals(SoundEnvironment.QUIET, classifier.classify(47.0))
    }
}
