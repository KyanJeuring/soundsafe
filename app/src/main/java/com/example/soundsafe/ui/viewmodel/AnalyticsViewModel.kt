package com.example.soundsafe.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.soundsafe.database.AppDatabase
import com.example.soundsafe.database.Sound
import com.example.soundsafe.ui.compose.SoundRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val soundDao = AppDatabase.getDatabase(application).soundDao()

    private val _selectedTimeFrame = MutableStateFlow("Daily")
    val selectedTimeFrame: StateFlow<String> = _selectedTimeFrame

    private val rawSounds: StateFlow<List<Sound>> = _selectedTimeFrame
        .flatMapLatest { timeFrame ->
            val (start, end) = getRangeForTimeFrame(timeFrame)
            soundDao.getSoundsInRange(start, end)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val soundLog: StateFlow<List<SoundRecord>> = combine(rawSounds, _selectedTimeFrame) { sounds, timeFrame ->
        aggregateLogs(sounds, timeFrame)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val avgDb: StateFlow<Double> = rawSounds.map { log ->
        if (log.isNotEmpty()) log.map { it.decibels }.average() else 0.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val maxDb: StateFlow<Double> = rawSounds.map { log ->
        if (log.isNotEmpty()) log.maxOf { it.decibels } else 0.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val minDb: StateFlow<Double> = rawSounds.map { log ->
        if (log.isNotEmpty()) log.minOf { it.decibels } else 0.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun onTimeFrameSelected(timeFrame: String) {
        _selectedTimeFrame.value = timeFrame
    }

    private fun aggregateLogs(sounds: List<Sound>, timeFrame: String): List<SoundRecord> {
        if (sounds.isEmpty()) return emptyList()

        val interval = when (timeFrame) {
            "Daily" -> 15 * 60 * 1000L // 15 minutes
            "Weekly" -> 60 * 60 * 1000L // 1 hour
            "Monthly" -> 24 * 60 * 60 * 1000L // 1 day
            else -> 15 * 60 * 1000L
        }

        return sounds.groupBy { it.timestamp / interval }
            .map { (key, group) ->
                val avgDb = group.map { it.decibels }.average()
                SoundRecord(key * interval, avgDb)
            }
            .sortedBy { it.timestamp }
    }

    private fun getRangeForTimeFrame(timeFrame: String): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val end = calendar.timeInMillis

        when (timeFrame) {
            "Daily" -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            "Weekly" -> {
                // Ensure Monday is the start
                val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                val daysToSubtract = (dayOfWeek + 5) % 7 // Mon=0, Tue=1, ..., Sun=6
                calendar.add(Calendar.DAY_OF_YEAR, -daysToSubtract)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
            "Monthly" -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            }
        }
        val start = calendar.timeInMillis

        // For Daily/Weekly/Monthly, the requirement says "current", so we return from start of that period until now.
        // Actually, for consistency in X-axis, it might be better to have the "end" be the end of the period (e.g. end of today).
        val endOfPeriod = Calendar.getInstance()
        when (timeFrame) {
            "Daily" -> {
                endOfPeriod.timeInMillis = start
                endOfPeriod.add(Calendar.DAY_OF_MONTH, 1)
            }
            "Weekly" -> {
                endOfPeriod.timeInMillis = start
                endOfPeriod.add(Calendar.WEEK_OF_YEAR, 1)
            }
            "Monthly" -> {
                endOfPeriod.timeInMillis = start
                endOfPeriod.add(Calendar.MONTH, 1)
            }
        }

        return start to endOfPeriod.timeInMillis
    }
}
