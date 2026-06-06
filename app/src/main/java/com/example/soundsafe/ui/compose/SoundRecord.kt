package com.example.soundsafe.ui.compose

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SoundRecord(
    val timestamp: Long,
    val dbLevel: Double
) {
    val time: String
        get() = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
}
